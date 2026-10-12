package com.example.neonmerge.game

import kotlin.math.abs
import kotlin.random.Random

/**
 * Represents a row and column coordinate on the 4x4 game grid.
 */
data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE) {
            "Position (row=$row, col=$col) must be within 0 until $BOARD_SIZE"
        }
    }

    /**
     * Converts (row, col) to a flat 0..15 list index.
     */
    val index: Int
        get() = row * BOARD_SIZE + col

    /**
     * Checks if this position is orthogonally adjacent (up, down, left, right) to another position.
     * Manhattan distance must equal 1.
     */
    fun isOrthogonallyAdjacentTo(other: Position): Boolean {
        val rowDiff = abs(row - other.row)
        val colDiff = abs(col - other.col)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    companion object {
        const val BOARD_SIZE = 4

        fun fromIndex(index: Int): Position {
            require(index in 0 until (BOARD_SIZE * BOARD_SIZE)) {
                "Index ($index) out of bounds for ${BOARD_SIZE}x${BOARD_SIZE} board"
            }
            return Position(index / BOARD_SIZE, index % BOARD_SIZE)
        }
    }
}

/**
 * Strategy interface for spawning new tiles into empty board slots.
 */
fun interface TileSpawner {
    /**
     * Given the list of empty indices on the board, returns the target index and tile value (2 or 4),
     * or null if no tile should be spawned.
     */
    fun spawn(emptyIndices: List<Int>): Pair<Int, Int>?
}

/**
 * Default spawner selecting a random empty cell with 90% chance of 2 and 10% chance of 4.
 */
class DefaultTileSpawner(
    private val random: Random = Random.Default
) : TileSpawner {
    override fun spawn(emptyIndices: List<Int>): Pair<Int, Int>? {
        if (emptyIndices.isEmpty()) return null
        val targetIndex = emptyIndices[random.nextInt(emptyIndices.size)]
        val value = if (random.nextFloat() < 0.9f) 2 else 4
        return Pair(targetIndex, value)
    }
}

/**
 * Immutable representation of the Neon Merge game state.
 */
data class NeonMergeState(
    val board: List<Int?>,
    val score: Int = 0,
    val moveCount: Int = 0,
    val selectedPosition: Position? = null,
    val isGameOver: Boolean = false,
    val wasReshuffled: Boolean = false
) {
    init {
        require(board.size == TOTAL_TILES) {
            "Board must contain exactly $TOTAL_TILES tiles, found ${board.size}"
        }
    }

    operator fun get(position: Position): Int? = board[position.index]
    operator fun get(row: Int, col: Int): Int? = board[row * Position.BOARD_SIZE + col]

    companion object {
        const val TOTAL_TILES = Position.BOARD_SIZE * Position.BOARD_SIZE
    }
}

/**
 * Pure Kotlin game engine for Neon Merge.
 */
class NeonMergeEngine(
    private val random: Random = Random.Default,
    private val tileSpawner: TileSpawner = DefaultTileSpawner(random),
    private val autoReshuffleOnNoMoves: Boolean = true
) {
    /**
     * Creates a new game state.
     * If [customBoard] is provided, it is used directly; otherwise an initial board is generated.
     */
    fun startNewGame(
        customBoard: List<Int?>? = null,
        initialTileCount: Int = 4
    ): NeonMergeState {
        val initialBoard = customBoard ?: createInitialBoard(initialTileCount)
        // Use checkGameOver consistently for all boards:
        // - Full board with no adjacent matches → game over.
        // - Sparse board with no matching values and autoReshuffle enabled → game over.
        // - Sparse board with matching values → reshuffle possible → not game over.
        return NeonMergeState(
            board = initialBoard,
            score = 0,
            moveCount = 0,
            selectedPosition = null,
            isGameOver = checkGameOver(initialBoard),
            wasReshuffled = false
        )
    }

    /**
     * Helper to get all orthogonally adjacent positions for a given position.
     */
    fun getOrthogonalNeighbors(pos: Position): List<Position> {
        val neighbors = mutableListOf<Position>()
        if (pos.row > 0) neighbors.add(Position(pos.row - 1, pos.col))
        if (pos.row < Position.BOARD_SIZE - 1) neighbors.add(Position(pos.row + 1, pos.col))
        if (pos.col > 0) neighbors.add(Position(pos.row, pos.col - 1))
        if (pos.col < Position.BOARD_SIZE - 1) neighbors.add(Position(pos.row, pos.col + 1))
        return neighbors
    }

    /**
     * Generates a starting 4x4 board with [tileCount] tiles.
     * Guarantees at least one valid move: two equal, non-empty tiles placed orthogonally adjacent.
     */
    fun createInitialBoard(tileCount: Int = 4): List<Int?> {
        val clampedCount = tileCount.coerceIn(2, NeonMergeState.TOTAL_TILES)
        val board = MutableList<Int?>(NeonMergeState.TOTAL_TILES) { null }

        // Pick a random adjacent pair
        val pos1 = Position(random.nextInt(Position.BOARD_SIZE), random.nextInt(Position.BOARD_SIZE))
        val neighbors = getOrthogonalNeighbors(pos1)
        val pos2 = neighbors[random.nextInt(neighbors.size)]

        val initialValue = if (random.nextFloat() < 0.9f) 2 else 4
        board[pos1.index] = initialValue
        board[pos2.index] = initialValue

        // Place the remaining (clampedCount - 2) tiles in empty slots
        val remainingIndices = (0 until NeonMergeState.TOTAL_TILES)
            .filter { it != pos1.index && it != pos2.index }
            .shuffled(random)
            .take(clampedCount - 2)

        for (index in remainingIndices) {
            board[index] = if (random.nextFloat() < 0.9f) 2 else 4
        }
        return board.toList()
    }

    /**
     * Checks if the non-null tiles on the board contain at least one pair of matching values.
     */
    fun hasMatchingValues(board: List<Int?>): Boolean {
        val counts = mutableMapOf<Int, Int>()
        for (tile in board) {
            if (tile != null && tile > 0 && tile <= Int.MAX_VALUE / 2) {
                counts[tile] = (counts[tile] ?: 0) + 1
                if (counts[tile]!! >= 2) return true
            }
        }
        return false
    }

    /**
     * Reshuffles the existing non-null tiles on the board so that at least one pair of matching
     * tiles is placed in orthogonally adjacent cells, preserving all tile values.
     * Returns null if no two tiles share the same value or if reshuffling is not possible.
     */
    fun reshuffle(board: List<Int?>): List<Int?>? {
        if (board.size != NeonMergeState.TOTAL_TILES) return null
        val nonNullValues = board.filterNotNull()
        if (nonNullValues.size < 2) return null

        val counts = nonNullValues
            .filter { it > 0 && it <= Int.MAX_VALUE / 2 }
            .groupingBy { it }
            .eachCount()
        val matchingValue = counts.entries.firstOrNull { it.value >= 2 }?.key ?: return null

        val newBoard = MutableList<Int?>(NeonMergeState.TOTAL_TILES) { null }

        // Pick a random adjacent pair for the matching tiles
        val pos1 = Position(random.nextInt(Position.BOARD_SIZE), random.nextInt(Position.BOARD_SIZE))
        val neighbors = getOrthogonalNeighbors(pos1)
        val pos2 = neighbors[random.nextInt(neighbors.size)]

        newBoard[pos1.index] = matchingValue
        newBoard[pos2.index] = matchingValue

        // Remaining values to place
        val remainingValues = nonNullValues.toMutableList().apply {
            remove(matchingValue)
            remove(matchingValue)
        }

        val availableIndices = (0 until NeonMergeState.TOTAL_TILES)
            .filter { it != pos1.index && it != pos2.index }
            .shuffled(random)

        for (i in remainingValues.indices) {
            newBoard[availableIndices[i]] = remainingValues[i]
        }

        return newBoard.toList()
    }

    /**
     * Handles selecting a tile at [target].
     */
    fun selectTile(state: NeonMergeState, target: Position): NeonMergeState {
        if (state.isGameOver) {
            return state
        }

        val currentSelected = state.selectedPosition

        // Case 1: First selection
        if (currentSelected == null) {
            val targetValue = state[target]
            return if (targetValue != null) {
                state.copy(selectedPosition = target, wasReshuffled = false)
            } else {
                state
            }
        }

        // Case 2: Selecting the same tile twice -> unselect
        if (currentSelected == target) {
            return state.copy(selectedPosition = null, wasReshuffled = false)
        }

        val targetValue = state[target]

        // Case 3: Non-adjacent pair -> invalid merge, preserve board/score/moves
        if (!currentSelected.isOrthogonallyAdjacentTo(target)) {
            return state.copy(
                selectedPosition = if (targetValue != null) target else null,
                wasReshuffled = false
            )
        }

        // Case 4: Adjacent tiles
        val sourceValue = state[currentSelected]
        // Guard against integer overflow: only merge if doubling fits within positive Int
        val canMerge = sourceValue != null && targetValue != null &&
            sourceValue == targetValue && sourceValue > 0 && sourceValue <= Int.MAX_VALUE / 2

        return if (canMerge) {
            val mergedValue = sourceValue * 2
            val boardAfterMerge = state.board.toMutableList().apply {
                this[currentSelected.index] = null
                this[target.index] = mergedValue
            }

            // Spawn exactly one new tile into an empty slot if available
            val emptyIndices = boardAfterMerge.indices.filter { boardAfterMerge[it] == null }
            if (emptyIndices.isNotEmpty()) {
                val spawnResult = tileSpawner.spawn(emptyIndices)
                if (spawnResult != null) {
                    val (spawnIndex, spawnVal) = spawnResult
                    // Validate index and value before placing
                    if (spawnIndex in boardAfterMerge.indices &&
                        boardAfterMerge[spawnIndex] == null &&
                        spawnVal > 0 && (spawnVal and (spawnVal - 1) == 0)  // positive power of two
                    ) {
                        boardAfterMerge[spawnIndex] = spawnVal
                    }
                }
            }

            var finalBoard = boardAfterMerge.toList()
            val newScore = if (Int.MAX_VALUE - mergedValue < state.score) {
                Int.MAX_VALUE
            } else {
                state.score + mergedValue
            }
            val newMoves = state.moveCount + 1
            var didReshuffle = false

            // Before leaving the player stuck, detect whether any adjacent matching pair remains.
            // If none exists and reshuffle is enabled and possible, reshuffle existing tiles fairly.
            if (!hasValidMerges(finalBoard) && autoReshuffleOnNoMoves && !isBoardFull(finalBoard)) {
                val reshuffled = reshuffle(finalBoard)
                if (reshuffled != null) {
                    finalBoard = reshuffled
                    didReshuffle = true
                }
            }

            // Recalculate game over
            val gameOver = checkGameOver(finalBoard)

            state.copy(
                board = finalBoard,
                score = newScore,
                moveCount = newMoves,
                selectedPosition = null,
                isGameOver = gameOver,
                wasReshuffled = didReshuffle
            )
        } else {
            // Adjacent but non-matching -> invalid merge, preserve board/score/moves
            state.copy(
                selectedPosition = if (targetValue != null) target else null,
                wasReshuffled = false
            )
        }
    }

    /**
     * Checks if all 16 board cells contain non-null tile values.
     */
    fun isBoardFull(board: List<Int?>): Boolean {
        return board.size == NeonMergeState.TOTAL_TILES && board.none { it == null }
    }

    /**
     * Checks whether any orthogonally adjacent pair of cells contains matching non-null values.
     */
    fun hasValidMerges(board: List<Int?>): Boolean {
        if (board.size != Position.BOARD_SIZE * Position.BOARD_SIZE) return false
        for (row in 0 until Position.BOARD_SIZE) {
            for (col in 0 until Position.BOARD_SIZE) {
                val current = board[row * Position.BOARD_SIZE + col] ?: continue
                if (current <= 0 || current > Int.MAX_VALUE / 2) continue

                // Check right neighbor
                if (col + 1 < Position.BOARD_SIZE) {
                    val right = board[row * Position.BOARD_SIZE + (col + 1)]
                    if (current == right) return true
                }

                // Check bottom neighbor
                if (row + 1 < Position.BOARD_SIZE) {
                    val down = board[(row + 1) * Position.BOARD_SIZE + col]
                    if (current == down) return true
                }
            }
        }
        return false
    }

    /**
     * Determines whether the game is over.
     * A game is over if:
     * 1. The board is full and has no valid adjacent matching pairs, OR
     * 2. No valid adjacent matching pair remains AND no two non-null tiles share the same value
     *    (meaning no valid move is possible even with reshuffling).
     */
    fun checkGameOver(board: List<Int?>): Boolean {
        if (board.size != NeonMergeState.TOTAL_TILES) return true
        if (hasValidMerges(board)) return false
        if (isBoardFull(board)) return true
        // If not full and has matching values, reshuffling can allow play to continue
        return if (autoReshuffleOnNoMoves) !hasMatchingValues(board) else true
    }

    /**
     * Restores a valid initial board, score zero, move count zero, and no selected tile.
     */
    fun reset(
        initialBoard: List<Int?>? = null,
        initialTileCount: Int = 4
    ): NeonMergeState {
        return startNewGame(
            customBoard = initialBoard,
            initialTileCount = initialTileCount
        )
    }
}
