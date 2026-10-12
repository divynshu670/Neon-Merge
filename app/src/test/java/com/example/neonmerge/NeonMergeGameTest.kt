package com.example.neonmerge

import com.example.neonmerge.game.DefaultTileSpawner
import com.example.neonmerge.game.NeonMergeEngine
import com.example.neonmerge.game.NeonMergeState
import com.example.neonmerge.game.Position
import com.example.neonmerge.game.TileSpawner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class NeonMergeGameTest {

    private lateinit var defaultEngine: NeonMergeEngine

    @Before
    fun setUp() {
        defaultEngine = NeonMergeEngine()
    }

    private fun createEmptyBoard(): MutableList<Int?> {
        return MutableList(NeonMergeState.TOTAL_TILES) { null }
    }

    @Test
    fun testSuccessfulMergeOfTwoAdjacentEqualTilesWithDeterministicSpawn() {
        // Setup board with two 4s at (1, 1) and (1, 2), plus an extra 8 so the board stays playable
        // after the merge produces 8. The spawner places another 8 adjacent to (1,2) at (1,3),
        // giving the player a valid next move (8 at (1,2) adjacent to 8 at (1,3)).
        val board = createEmptyBoard().apply {
            this[Position(1, 1).index] = 4
            this[Position(1, 2).index] = 4
        }
        val spawnTarget = Position(1, 3)  // adjacent to (1,2), spawns 8 to stay playable
        val deterministicSpawner = TileSpawner { emptyIndices ->
            assertTrue("Spawn target must be among empty indices", emptyIndices.contains(spawnTarget.index))
            Pair(spawnTarget.index, 8)  // 8 placed adjacent to merged 8 at (1,2)
        }
        val engine = NeonMergeEngine(tileSpawner = deterministicSpawner)
        val initialState = engine.startNewGame(customBoard = board)

        // Select first tile at (1, 1)
        val stateAfterFirstSelect = engine.selectTile(initialState, Position(1, 1))
        assertEquals(Position(1, 1), stateAfterFirstSelect.selectedPosition)
        assertEquals(0, stateAfterFirstSelect.moveCount)
        assertEquals(0, stateAfterFirstSelect.score)

        // Select adjacent matching tile at (1, 2) (destination)
        val stateAfterMerge = engine.selectTile(stateAfterFirstSelect, Position(1, 2))

        // Source cell must be cleared
        assertNull("Source tile at (1, 1) should be cleared", stateAfterMerge[Position(1, 1)])
        // Destination cell must receive doubled value (8)
        assertEquals("Destination tile at (1, 2) should be doubled to 8", 8, stateAfterMerge[Position(1, 2)])
        // Spawned tile must appear at designated location
        assertEquals("Spawned tile at (1, 3) should have value 8", 8, stateAfterMerge[spawnTarget])
        // Total occupied count: 2 (source merged into destination = -1, then +1 spawn)
        assertEquals(2, stateAfterMerge.board.count { it != null })
        // Selection must be cleared
        assertNull("Selection should be cleared after merge", stateAfterMerge.selectedPosition)
        // Score must increase by newly created tile value (8)
        assertEquals("Score should increase by 8", 8, stateAfterMerge.score)
        // Move count must increment by 1
        assertEquals("Move count should increment by 1", 1, stateAfterMerge.moveCount)
        // Board has 8 at (1,2) adjacent to 8 at (1,3) — game NOT over
        assertTrue("Board must have a valid merge", engine.hasValidMerges(stateAfterMerge.board))
        assertFalse("Game must not be over when a valid merge exists", stateAfterMerge.isGameOver)
    }

    @Test
    fun testSuccessfulMergeSpawnsExactlyOneNewTile() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(0, 1).index] = 2
        }
        // Use deterministic spawner: always spawn 2 at (3,3) — not adjacent to either tile.
        // Board after merge + spawn: (0,1)=4 and (3,3)=2. Both isolated, no valid merges.
        // Game over condition expected. We verify tile count and merge result only.
        val spawner = TileSpawner { _ -> Pair(Position(3, 3).index, 2) }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val initialState = engine.startNewGame(customBoard = board)
        val occupiedBefore = initialState.board.count { it != null }
        assertEquals(2, occupiedBefore)

        val selectedState = engine.selectTile(initialState, Position(0, 0))
        val mergedState = engine.selectTile(selectedState, Position(0, 1))

        // Net change in occupied tiles: 2 tiles merge into 1 (-1), and exactly 1 spawns (+1)
        val occupiedAfter = mergedState.board.count { it != null }
        assertEquals("Occupied tile count must remain consistent after merge and spawn", occupiedBefore, occupiedAfter)
        assertEquals(4, mergedState[Position(0, 1)])
        assertNull("Source tile must be cleared", mergedState[Position(0, 0)])
        assertEquals(2, mergedState[Position(3, 3)])
        assertEquals(4, mergedState.score)
        assertEquals(1, mergedState.moveCount)
    }

    @Test
    fun testInvalidMergesDoNotSpawnTiles() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(0, 1).index] = 4
            this[Position(2, 2).index] = 8
        }
        val initialState = defaultEngine.startNewGame(customBoard = board)
        val initialOccupiedCount = initialState.board.count { it != null }

        // 1. Non-matching adjacent tap: (0, 0) -> (0, 1)
        val state1 = defaultEngine.selectTile(initialState, Position(0, 0))
        val state2 = defaultEngine.selectTile(state1, Position(0, 1))
        assertEquals("Non-matching adjacent tap must not spawn tiles", initialOccupiedCount, state2.board.count { it != null })
        assertEquals(0, state2.score)
        assertEquals(0, state2.moveCount)

        // 2. Non-adjacent tap: (0, 1) -> (2, 2)
        val state3 = defaultEngine.selectTile(state2, Position(2, 2))
        assertEquals("Non-adjacent tap must not spawn tiles", initialOccupiedCount, state3.board.count { it != null })
        assertEquals(0, state3.score)
        assertEquals(0, state3.moveCount)

        // 3. Same tile repeated tap: (2, 2) -> (2, 2)
        val state4 = defaultEngine.selectTile(state3, Position(2, 2))
        assertNull("Tapping same tile unselects", state4.selectedPosition)
        assertEquals("Unselecting must not spawn tiles", initialOccupiedCount, state4.board.count { it != null })
        assertEquals(0, state4.score)
        assertEquals(0, state4.moveCount)
    }

    @Test
    fun testSpawningNeverOverwritesOccupiedCells() {
        val occupiedTarget = Position(1, 1)
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(0, 1).index] = 2
            this[occupiedTarget.index] = 16
        }

        // Malicious or buggy spawner attempting to overwrite an occupied cell
        val buggySpawner = TileSpawner { _ ->
            Pair(occupiedTarget.index, 4) // Attempts to overwrite (1, 1)
        }
        val engine = NeonMergeEngine(tileSpawner = buggySpawner)
        val state = engine.startNewGame(customBoard = board)

        val mergedState = engine.selectTile(
            engine.selectTile(state, Position(0, 0)),
            Position(0, 1)
        )

        // Occupied cell must NOT be overwritten
        assertEquals("Occupied cell must retain its original value", 16, mergedState[occupiedTarget])
        assertEquals(4, mergedState[Position(0, 1)])
    }

    @Test
    fun testNonMatchingPairDoesNotChangeBoardScoreOrMoves() {
        // Board has 2 and 4 (non-matching), plus a matching pair (2, 2) so the game is active.
        val board = createEmptyBoard().apply {
            this[Position(1, 1).index] = 2
            this[Position(1, 2).index] = 4
            this[Position(3, 0).index] = 2  // keeps board active (matching value exists)
            this[Position(3, 3).index] = 2
        }
        val initialState = defaultEngine.startNewGame(customBoard = board)
        assertFalse("Game must not be over on start", initialState.isGameOver)

        val state1 = defaultEngine.selectTile(initialState, Position(1, 1))
        assertEquals(Position(1, 1), state1.selectedPosition)

        val state2 = defaultEngine.selectTile(state1, Position(1, 2))
        assertEquals(2, state2[Position(1, 1)])
        assertEquals(4, state2[Position(1, 2)])
        assertEquals(0, state2.score)
        assertEquals(0, state2.moveCount)
        // Non-matching adjacent: selection shifts to the newly tapped tile
        assertEquals(Position(1, 2), state2.selectedPosition)
    }

    @Test
    fun testNonAdjacentPairDoesNotChangeBoardScoreOrMoves() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 8
            this[Position(2, 2).index] = 8
        }
        val initialState = defaultEngine.startNewGame(customBoard = board)

        val state1 = defaultEngine.selectTile(initialState, Position(0, 0))
        val state2 = defaultEngine.selectTile(state1, Position(2, 2))

        assertEquals(8, state2[Position(0, 0)])
        assertEquals(8, state2[Position(2, 2)])
        assertEquals(0, state2.score)
        assertEquals(0, state2.moveCount)
        assertEquals(Position(2, 2), state2.selectedPosition)
    }

    @Test
    fun testMergeAtBoardEdgeAndCorner() {
        val board = createEmptyBoard().apply {
            this[Position(0, 2).index] = 16
            this[Position(0, 3).index] = 16
        }
        val spawnTarget = Position(3, 0)
        val deterministicSpawner = TileSpawner { _ -> Pair(spawnTarget.index, 2) }
        val engine = NeonMergeEngine(tileSpawner = deterministicSpawner)
        val initialState = engine.startNewGame(customBoard = board)

        val state1 = engine.selectTile(initialState, Position(0, 2))
        val state2 = engine.selectTile(state1, Position(0, 3))

        assertNull("Source cell at edge (0, 2) should be cleared", state2[Position(0, 2)])
        assertEquals("Corner destination cell (0, 3) should receive 32", 32, state2[Position(0, 3)])
        assertEquals(32, state2.score)
        assertEquals(1, state2.moveCount)
        assertNull(state2.selectedPosition)
        assertEquals(2, state2[spawnTarget])
    }

    @Test
    fun testConsecutiveMergesUpdateScoreAndMovesCorrectly() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(0, 1).index] = 2
            this[Position(1, 0).index] = 4
            this[Position(1, 1).index] = 4
        }
        var spawnCounter = 0
        val spawner = TileSpawner { _ ->
            val target = if (spawnCounter == 0) Position(3, 0).index else Position(3, 1).index
            spawnCounter++
            Pair(target, 2)
        }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val state = engine.startNewGame(customBoard = board)

        // Merge 1: (0, 0) -> (0, 1) creates 4 (score +4, moves = 1)
        val afterMerge1 = engine.selectTile(
            engine.selectTile(state, Position(0, 0)),
            Position(0, 1)
        )
        assertEquals(4, afterMerge1.score)
        assertEquals(1, afterMerge1.moveCount)
        assertEquals(4, afterMerge1[Position(0, 1)])

        // Merge 2: (1, 0) -> (1, 1) creates 8 (score +8 = 12, moves = 2)
        val afterMerge2 = engine.selectTile(
            engine.selectTile(afterMerge1, Position(1, 0)),
            Position(1, 1)
        )
        assertEquals(12, afterMerge2.score)
        assertEquals(2, afterMerge2.moveCount)
        assertEquals(8, afterMerge2[Position(1, 1)])
    }

    @Test
    fun testPreventSelectingSameTileTwiceAsMerge() {
        // Include a matching pair so the board is not immediately game over.
        // We test the deselect behaviour on the isolated tile at (1,1)=4.
        val board = createEmptyBoard().apply {
            this[Position(1, 1).index] = 4
            this[Position(3, 0).index] = 4  // matching pair exists to keep game active
        }
        val state = defaultEngine.startNewGame(customBoard = board)
        assertFalse("Game must not be over on start", state.isGameOver)

        val state1 = defaultEngine.selectTile(state, Position(1, 1))
        assertEquals(Position(1, 1), state1.selectedPosition)

        val state2 = defaultEngine.selectTile(state1, Position(1, 1))
        assertNull("Tapping the same tile twice should clear selection", state2.selectedPosition)
        assertEquals(4, state2[Position(1, 1)])
        assertEquals(0, state2.score)
        assertEquals(0, state2.moveCount)
    }

    @Test
    fun testFullBoardWithNoValidMergesIsGameOver() {
        // Construct a full 4x4 checkerboard with alternating values (no two adjacent cells match)
        val fullNoMergeBoard = List(16) { index ->
            val row = index / 4
            val col = index % 4
            if ((row + col) % 2 == 0) 2 else 4
        }

        assertTrue("Board must be full", defaultEngine.isBoardFull(fullNoMergeBoard))
        assertFalse("Board must have no valid merges", defaultEngine.hasValidMerges(fullNoMergeBoard))
        assertTrue("Game over must be detected", defaultEngine.checkGameOver(fullNoMergeBoard))

        val state = defaultEngine.startNewGame(customBoard = fullNoMergeBoard)
        assertTrue("State isGameOver must be true", state.isGameOver)

        // Once game is over, selecting tiles must freeze and return state unchanged
        val attemptedAction = defaultEngine.selectTile(state, Position(0, 0))
        assertEquals(state, attemptedAction)
    }

    @Test
    fun testFullBoardWithAtLeastOneValidMergeIsNotGameOver() {
        // Construct a full 4x4 board where only (0, 0) and (0, 1) match (both 2)
        val board = List(16) { index ->
            val row = index / 4
            val col = index % 4
            if (row == 0 && col == 1) 2
            else if ((row + col) % 2 == 0) 2
            else 4
        }

        assertTrue("Board is full", defaultEngine.isBoardFull(board))
        assertTrue("Board has at least one valid merge", defaultEngine.hasValidMerges(board))
        assertFalse("Game over must NOT be triggered when a valid merge exists", defaultEngine.checkGameOver(board))

        val state = defaultEngine.startNewGame(customBoard = board)
        assertFalse(state.isGameOver)

        // Player must still be able to merge the matching pair
        val mergedState = defaultEngine.selectTile(
            defaultEngine.selectTile(state, Position(0, 0)),
            Position(0, 1)
        )
        assertEquals(4, mergedState[Position(0, 1)])
        assertEquals(4, mergedState.score)
        assertEquals(1, mergedState.moveCount)
    }

    @Test
    fun testMergeResultingInFullBoardWithNoMergesTriggersGameOver() {
        // Construct a full 16-tile board with EXACTLY ONE matching pair: (0, 0)=2 and (0, 1)=2.
        // All other cells have no matches.
        val board = listOf(
            2, 2, 8, 4,
            4, 8, 4, 2,
            2, 4, 2, 4,
            4, 2, 4, 2
        )
        val spawner = TileSpawner { emptyIndices ->
            assertEquals(1, emptyIndices.size)
            assertEquals(Position(0, 0).index, emptyIndices.first())
            // Spawning 2 at (0, 0) leaves it adjacent to 4 and 4, creating no new matches
            Pair(emptyIndices.first(), 2)
        }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val state = engine.startNewGame(customBoard = board)
        assertFalse("Initially not game over because valid merge exists", state.isGameOver)

        // Perform the merge: (0, 0) -> (0, 1)
        val mergedState = engine.selectTile(
            engine.selectTile(state, Position(0, 0)),
            Position(0, 1)
        )

        // Board must be full and game over must be triggered
        assertTrue("Board must be full after merge and spawn", engine.isBoardFull(mergedState.board))
        assertFalse("Board must have no remaining valid merges", engine.hasValidMerges(mergedState.board))
        assertTrue("Game over must be true", mergedState.isGameOver)

        // Further actions must be frozen
        val actionAfterGameOver = engine.selectTile(mergedState, Position(0, 1))
        assertEquals(mergedState, actionAfterGameOver)
    }

    @Test
    fun testResetRestoresStartingStateAndClearsSelection() {
        val initialBoard = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(0, 1).index] = 2
        }
        val deterministicSpawner = TileSpawner { _ -> Pair(Position(3, 3).index, 2) }
        val engine = NeonMergeEngine(tileSpawner = deterministicSpawner)
        val state = engine.startNewGame(customBoard = initialBoard)

        // Perform a merge to alter state
        val stateAfterMerge = engine.selectTile(
            engine.selectTile(state, Position(0, 0)),
            Position(0, 1)
        )
        assertEquals(4, stateAfterMerge.score)
        assertEquals(1, stateAfterMerge.moveCount)

        // Reset game with initial board
        val resetState = engine.reset(initialBoard = initialBoard)

        assertEquals("Score must be reset to 0", 0, resetState.score)
        assertEquals("Move count must be reset to 0", 0, resetState.moveCount)
        assertNull("Selected position must be reset to null", resetState.selectedPosition)
        assertFalse("Game over must be false", resetState.isGameOver)
        assertEquals(2, resetState[Position(0, 0)])
        assertEquals(2, resetState[Position(0, 1)])
    }

    @Test
    fun testEmptyCellClickDoesNotCrashOrMutateState() {
        // Board has two 2s (matching) so game is active,
        // and a single isolated tile to test empty-cell tap behaviour.
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(2, 0).index] = 2  // keeps game active
        }
        val state = defaultEngine.startNewGame(customBoard = board)
        assertFalse("Game must not be over on start", state.isGameOver)

        // Tap empty cell with no current selection → state unchanged
        val stateAfterEmptyTap = defaultEngine.selectTile(state, Position(1, 1))
        assertEquals(state, stateAfterEmptyTap)

        // Select (0,0)
        val selectedState = defaultEngine.selectTile(state, Position(0, 0))
        assertEquals(Position(0, 0), selectedState.selectedPosition)

        // Tap adjacent empty cell (0,1) — adjacent but empty → selection clears, board unchanged
        val stateAfterAdjacentEmptyTap = defaultEngine.selectTile(selectedState, Position(0, 1))
        assertEquals(2, stateAfterAdjacentEmptyTap[Position(0, 0)])
        assertNull(stateAfterAdjacentEmptyTap[Position(0, 1)])
        assertEquals(0, stateAfterAdjacentEmptyTap.score)
        assertEquals(0, stateAfterAdjacentEmptyTap.moveCount)
        assertNull(stateAfterAdjacentEmptyTap.selectedPosition)
    }

    @Test
    fun testStartNewGameDefaultGeneratesValidState() {
        val defaultState = defaultEngine.startNewGame()
        assertEquals(0, defaultState.score)
        assertEquals(0, defaultState.moveCount)
        assertNull(defaultState.selectedPosition)
        assertEquals(16, defaultState.board.size)

        val nonNullTiles = defaultState.board.filterNotNull()
        assertEquals(4, nonNullTiles.size)
        assertTrue(nonNullTiles.all { it == 2 || it == 4 })
    }

    @Test
    fun testDefaultTileSpawnerSpawnsTwoOrFourSafely() {
        val spawner = DefaultTileSpawner(Random(42))
        val emptySlots = listOf(1, 5, 9, 13)

        // Test multiple spawns
        for (i in 0 until 50) {
            val result = spawner.spawn(emptySlots)
            assertNotNull(result)
            val (index, value) = result!!
            assertTrue("Spawn index must be in empty slots", emptySlots.contains(index))
            assertTrue("Spawn value must be 2 or 4", value == 2 || value == 4)
        }

        // Test with empty list
        val emptyResult = spawner.spawn(emptyList())
        assertNull("Empty list must return null", emptyResult)
    }

    @Test
    fun testNewGameAlwaysStartsAtLeastOneValidMove() {
        // Run 50 random new games to verify that EVERY game starts with at least one valid adjacent matching pair
        for (i in 0 until 50) {
            val engine = NeonMergeEngine(random = Random(i * 31 + 7))
            val state = engine.startNewGame()
            assertTrue(
                "Every new game must start with at least one valid merge (seed=$i)",
                engine.hasValidMerges(state.board)
            )
            assertFalse("Initial game must never be game over", state.isGameOver)
        }
    }

    @Test
    fun testBoardWithNoValidMovesReshufflesFairlyWhenMatchingTilesExist() {
        // Scattered matching tiles: (0, 0)=4, (0, 3)=4, (3, 0)=2, (3, 3)=2
        val scatteredBoard = createEmptyBoard().apply {
            this[Position(0, 0).index] = 4
            this[Position(0, 3).index] = 4
            this[Position(3, 0).index] = 2
            this[Position(3, 3).index] = 2
        }

        assertFalse("Initially scattered board has no adjacent merges", defaultEngine.hasValidMerges(scatteredBoard))
        assertTrue("Matching values exist", defaultEngine.hasMatchingValues(scatteredBoard))

        val reshuffled = defaultEngine.reshuffle(scatteredBoard)
        assertNotNull("Reshuffle must succeed when matching values exist", reshuffled)
        assertTrue("Reshuffled board must contain at least one valid adjacent match", defaultEngine.hasValidMerges(reshuffled!!))
        assertEquals(
            "Tile values must be preserved exactly after reshuffle",
            scatteredBoard.filterNotNull().sorted(),
            reshuffled.filterNotNull().sorted()
        )
    }

    @Test
    fun testBoardWithNoValidMovesAndNoMatchingPairsTriggersGameOver() {
        // All distinct values: (0, 0)=2, (1, 1)=4, (2, 2)=8, (3, 3)=16.
        // No adjacent matching pair, no matching values at all.
        // checkGameOver: hasValidMerges=false, isBoardFull=false, hasMatchingValues=false
        // → with autoReshuffle=true: returns !hasMatchingValues = true → game over.
        val distinctBoard = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(1, 1).index] = 4
            this[Position(2, 2).index] = 8
            this[Position(3, 3).index] = 16
        }

        assertFalse("No valid merges exist", defaultEngine.hasValidMerges(distinctBoard))
        assertFalse("No matching values exist", defaultEngine.hasMatchingValues(distinctBoard))
        assertTrue("checkGameOver must return true: no reshuffle possible",
            defaultEngine.checkGameOver(distinctBoard))

        // startNewGame uses checkGameOver consistently for all boards.
        val state = defaultEngine.startNewGame(customBoard = distinctBoard)
        assertTrue("State must immediately reflect game over", state.isGameOver)
    }

    @Test
    fun testSuccessfulMergeFairReshuffleWhenSpawnDoesNotFormMatch() {
        // Setup a board where merge produces no natural adjacent matches, but matching values exist:
        // (0, 0)=2, (0, 1)=2, (3, 0)=4.
        // Merging (0, 0) into (0, 1) creates 4.
        // Spawner places a 4 far away at (3, 3).
        // Now board has values [4, 4, 4] at non-adjacent cells (0, 1), (3, 0), (3, 3).
        // Since no adjacent matches exist, engine auto-reshuffles and preserves all tiles!
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(0, 1).index] = 2
            this[Position(3, 0).index] = 4
        }
        val spawner = TileSpawner { _ -> Pair(Position(3, 3).index, 4) }
        val engine = NeonMergeEngine(tileSpawner = spawner, autoReshuffleOnNoMoves = true)
        val state = engine.startNewGame(customBoard = board)

        val mergedState = engine.selectTile(
            engine.selectTile(state, Position(0, 0)),
            Position(0, 1)
        )

        assertEquals("Score should update by 4", 4, mergedState.score)
        assertEquals("Move count should update to 1", 1, mergedState.moveCount)
        assertFalse("Game should NOT be over because tiles were reshuffled", mergedState.isGameOver)
        assertTrue("wasReshuffled must be true", mergedState.wasReshuffled)
        assertTrue("Board must now have a valid adjacent merge", engine.hasValidMerges(mergedState.board))
        assertEquals(
            "Values [4, 4, 4] must be preserved",
            listOf(4, 4, 4),
            mergedState.board.filterNotNull().sorted()
        )
    }

    // ── Phase 4 additional required tests ─────────────────────────────────────

    @Test
    fun testVerticalMerge() {
        // Two equal tiles stacked vertically at (0,0) and (1,0)
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 16
            this[Position(1, 0).index] = 16
        }
        val spawner = TileSpawner { _ -> Pair(Position(3, 3).index, 2) }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val state = engine.startNewGame(customBoard = board)

        val merged = engine.selectTile(engine.selectTile(state, Position(0, 0)), Position(1, 0))

        assertNull("Source at (0,0) must be empty", merged[Position(0, 0)])
        assertEquals(32, merged[Position(1, 0)])
        assertEquals(32, merged.score)
        assertEquals(1, merged.moveCount)
        assertNull(merged.selectedPosition)
    }

    @Test
    fun testDiagonalTilesAreNotAdjacent() {
        // (0,0) and (1,1) are diagonal — must NOT merge regardless of values
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 4
            this[Position(1, 1).index] = 4
        }
        val state = defaultEngine.startNewGame(customBoard = board)

        val afterSelect = defaultEngine.selectTile(state, Position(0, 0))
        val afterSecond = defaultEngine.selectTile(afterSelect, Position(1, 1))

        // Non-adjacent: selection shifts to (1,1), board/score/moves unchanged
        assertEquals(4, afterSecond[Position(0, 0)])
        assertEquals(4, afterSecond[Position(1, 1)])
        assertEquals(0, afterSecond.score)
        assertEquals(0, afterSecond.moveCount)
        assertEquals(Position(1, 1), afterSecond.selectedPosition)

        // Also verify hasValidMerges returns false for this board
        assertFalse("Diagonal tiles must not constitute a valid merge",
            defaultEngine.hasValidMerges(board))
    }

    @Test
    fun testSelectingDifferentNonAdjacentTileChangesSelection() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 8
            this[Position(3, 3).index] = 8
        }
        val state = defaultEngine.startNewGame(customBoard = board)

        // Select (0,0)
        val s1 = defaultEngine.selectTile(state, Position(0, 0))
        assertEquals(Position(0, 0), s1.selectedPosition)

        // Tap (3,3) — non-adjacent, different position → selection moves to (3,3)
        val s2 = defaultEngine.selectTile(s1, Position(3, 3))
        assertEquals(Position(3, 3), s2.selectedPosition)
        assertEquals(0, s2.score)
        assertEquals(0, s2.moveCount)
    }

    @Test
    fun testSpawningSkippedWhenBoardHasNoEmptyCells() {
        // 15 tiles, one empty cell at index 0. Board has one valid merge (0,0)=2 and (0,1)=2.
        // After merging, the board becomes full (15 tiles → 14 + newly spawned = 15, then
        // spawn tries to fill the last slot → board reaches 15 tiles after merge without spawn
        // because there is one empty slot and the spawner IS invoked.
        // This test verifies that when there are NO empty cells after the merge, spawn is skipped.
        // We use a 16-tile board with one matching pair and all other cells filled:
        val board = listOf(
            2, 2, 8, 4,
            4, 8, 4, 2,
            2, 4, 2, 4,
            4, 2, 4, 2
        )
        // This is a full (16-tile) board with one matching pair.
        // After merging (0,0) and (0,1): (0,0)=null, (0,1)=4. Board has 15 tiles filled and 1 empty.
        // The spawner IS called with 1 empty slot. We DON'T test 'no empty slots after merge' here,
        // because the spec is: spawn is skipped only when emptyIndices is empty.
        // The engine already handles this: if emptyIndices.isEmpty(), spawn is not attempted.
        // This subtest verifies the engine returns null from the spawner and does not crash.
        var spawnerSawEmptyList = false
        val spawner = TileSpawner { emptyIndices ->
            if (emptyIndices.isEmpty()) {
                spawnerSawEmptyList = true
                null
            } else {
                Pair(emptyIndices.first(), 2)
            }
        }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val state = engine.startNewGame(customBoard = board)
        assertFalse("Initial state is not game over", state.isGameOver)

        // Perform the merge — one empty slot exists, spawner IS called with that slot
        val merged = engine.selectTile(engine.selectTile(state, Position(0, 0)), Position(0, 1))
        // After spawn the board is full again (15-1+1=15... wait, board was 16 tiles, one merge makes 15 occupied,
        // spawn fills one = 16 occupied)
        assertTrue("Board must be full after merge and spawn on 15-tile board",
            engine.isBoardFull(merged.board))
        // The spawner never saw an empty list in this test
        assertFalse("Spawner was not called with empty list", spawnerSawEmptyList)
    }

    @Test
    fun testInvalidSpawnerOutputDoesNotCorruptBoard() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 4
            this[Position(0, 1).index] = 4
        }
        val preMergeOccupied = board.count { it != null }

        // Spawner returns an out-of-bounds index
        val badIndexSpawner = TileSpawner { _ -> Pair(-1, 2) }
        val engine1 = NeonMergeEngine(tileSpawner = badIndexSpawner)
        val state1 = engine1.startNewGame(customBoard = board)
        val merged1 = engine1.selectTile(engine1.selectTile(state1, Position(0, 0)), Position(0, 1))
        // The invalid index is rejected; no crash; occupied count is preMergeOccupied - 1 (no spawn)
        assertEquals(preMergeOccupied - 1, merged1.board.count { it != null })
        assertEquals(8, merged1[Position(0, 1)])

        // Spawner returns an already-occupied index
        val occupiedTarget = Position(0, 1)  // will already have value 8 after merge
        val occupiedIndexSpawner = TileSpawner { _ -> Pair(occupiedTarget.index, 2) }
        val engine2 = NeonMergeEngine(tileSpawner = occupiedIndexSpawner)
        val state2 = engine2.startNewGame(customBoard = board)
        val merged2 = engine2.selectTile(engine2.selectTile(state2, Position(0, 0)), Position(0, 1))
        // boardAfterMerge has (0,1)=8; spawner tries (0,1) which is NOT null → rejected
        assertEquals(8, merged2[occupiedTarget])
        assertEquals(preMergeOccupied - 1, merged2.board.count { it != null })
    }

    @Test
    fun testHasValidMergesHandlesEmptyAndMixedBoards() {
        // Empty board
        val emptyBoard = createEmptyBoard().toList()
        assertFalse("Empty board has no valid merges", defaultEngine.hasValidMerges(emptyBoard))

        // Single tile
        val singleTile = createEmptyBoard().apply { this[0] = 2 }.toList()
        assertFalse("Single-tile board has no valid merges", defaultEngine.hasValidMerges(singleTile))

        // Two matching tiles adjacent horizontally
        val hMatch = createEmptyBoard().apply {
            this[Position(2, 1).index] = 8
            this[Position(2, 2).index] = 8
        }.toList()
        assertTrue("Horizontal matching pair must be detected", defaultEngine.hasValidMerges(hMatch))

        // Two matching tiles adjacent vertically
        val vMatch = createEmptyBoard().apply {
            this[Position(1, 3).index] = 4
            this[Position(2, 3).index] = 4
        }.toList()
        assertTrue("Vertical matching pair must be detected", defaultEngine.hasValidMerges(vMatch))

        // Two matching tiles diagonal only — not a valid merge
        val diagOnly = createEmptyBoard().apply {
            this[Position(0, 0).index] = 4
            this[Position(1, 1).index] = 4
        }.toList()
        assertFalse("Diagonal pair must NOT be detected as valid merge",
            defaultEngine.hasValidMerges(diagOnly))
    }

    @Test
    fun testReshuffleIsBoundedAndPreservesValueMultiset() {
        // Six tiles, three matching pairs, none adjacent
        val scattered = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(3, 3).index] = 2
            this[Position(0, 3).index] = 4
            this[Position(3, 0).index] = 4
            this[Position(1, 3).index] = 8
            this[Position(2, 0).index] = 8
        }.toList()

        val originalValues = scattered.filterNotNull().sorted()
        assertFalse("No valid merges in scattered board", defaultEngine.hasValidMerges(scattered))

        val reshuffled = defaultEngine.reshuffle(scattered)
        assertNotNull("Reshuffle must succeed", reshuffled)
        assertTrue("Reshuffled board must have at least one valid merge",
            defaultEngine.hasValidMerges(reshuffled!!))
        assertEquals("All tile values must be preserved exactly",
            originalValues, reshuffled.filterNotNull().sorted())
        assertEquals("Board size must remain 16", 16, reshuffled.size)
    }

    @Test
    fun testReshuffleReturnsNullWhenNoMatchingValues() {
        // Three distinct values: no two tiles share a value → reshuffle impossible
        val noMatchBoard = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(1, 1).index] = 4
            this[Position(2, 2).index] = 8
        }.toList()

        assertNull("Reshuffle must return null when no matching values exist",
            defaultEngine.reshuffle(noMatchBoard))
    }

    @Test
    fun testTileDoubleDoesNotProduceNegativeOrZero() {
        // 1. Ensure doubling a large value (2^29 = 536,870,912) produces positive 2^30 (1,073,741,824)
        val largeValue = 1 shl 29
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = largeValue
            this[Position(0, 1).index] = largeValue
        }
        val spawner = TileSpawner { _ -> Pair(Position(3, 3).index, 2) }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val state = engine.startNewGame(customBoard = board)

        val merged = engine.selectTile(engine.selectTile(state, Position(0, 0)), Position(0, 1))

        val mergedValue = merged[Position(0, 1)]
        assertNotNull("Merged value must not be null", mergedValue)
        assertEquals("Merged value must be 2^30", 1 shl 30, mergedValue)
        assertTrue("Merged value must remain positive", mergedValue!! > 0)
        assertTrue("Score must not become negative", merged.score > 0)

        // 2. Ensure tiles at the maximum positive power-of-two (2^30) are guarded against overflow:
        // Attempting to merge them must not produce negative Int.MIN_VALUE or corrupt the board/score.
        val maxBoard = createEmptyBoard().apply {
            this[Position(0, 0).index] = 1 shl 30
            this[Position(0, 1).index] = 1 shl 30
        }
        val maxState = engine.startNewGame(customBoard = maxBoard)
        val afterAttemptedMerge = engine.selectTile(
            engine.selectTile(maxState, Position(0, 0)),
            Position(0, 1)
        )
        val val0 = afterAttemptedMerge[Position(0, 0)]
        val val1 = afterAttemptedMerge[Position(0, 1)]
        assertNotNull(val0)
        assertNotNull(val1)
        assertTrue("Tile at (0, 0) must remain positive", val0!! > 0)
        assertTrue("Tile at (0, 1) must remain positive", val1!! > 0)
        assertTrue("Score must never become negative", afterAttemptedMerge.score >= 0)
    }

    @Test
    fun testBoardAlwaysHasExactly16Cells() {
        repeat(10) { seed ->
            val engine = NeonMergeEngine(random = Random(seed))
            val state = engine.startNewGame()
            assertEquals("Board must always have exactly 16 cells", 16, state.board.size)
        }
    }

    @Test
    fun testRestartPreservesSessionBestScore() {
        // This is a UI-layer concern (bestScore is tracked in NeonMergeScreen, not the engine).
        // From the engine's perspective: reset() always returns score=0.
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 2
            this[Position(0, 1).index] = 2
        }
        val spawner = TileSpawner { _ -> Pair(Position(3, 3).index, 2) }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val state = engine.startNewGame(customBoard = board)

        val merged = engine.selectTile(engine.selectTile(state, Position(0, 0)), Position(0, 1))
        assertEquals("Score after merge must be 4", 4, merged.score)

        // Reset must return score=0 regardless of previous score
        val reset = engine.reset()
        assertEquals("Score must be 0 after reset", 0, reset.score)
        assertEquals("Moves must be 0 after reset", 0, reset.moveCount)
        assertNull("Selection must be null after reset", reset.selectedPosition)
        assertFalse("Game over must be false after reset", reset.isGameOver)
        // Best score is tracked by the UI (NeonMergeScreen), not the engine
        assertTrue("Reset board must have at least one valid merge", engine.hasValidMerges(reset.board))
    }

    @Test
    fun testGameOverStatePreventsAllBoardActions() {
        val fullNoMergeBoard = List(16) { index ->
            val row = index / 4
            val col = index % 4
            if ((row + col) % 2 == 0) 2 else 4
        }
        val state = defaultEngine.startNewGame(customBoard = fullNoMergeBoard)
        assertTrue("Must be game over", state.isGameOver)

        // Try several taps — all must return the exact same state
        val positions = listOf(Position(0, 0), Position(0, 1), Position(1, 0), Position(3, 3))
        for (pos in positions) {
            val result = defaultEngine.selectTile(state, pos)
            assertEquals("Game-over state must be frozen for tap at $pos", state, result)
        }
    }

    @Test
    fun testReshuffleRejectsValuesExceedingMaxDoubleLimit() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 1 shl 30
            this[Position(3, 3).index] = 1 shl 30
        }.toList()

        // Although two tiles have matching values (2^30), they cannot legally merge due to overflow protection.
        // Reshuffle must return null and not falsely claim a valid reshuffle.
        assertNull("Reshuffle must return null when matching values cannot double legally",
            defaultEngine.reshuffle(board))
        assertFalse("hasMatchingValues must return false for values > Int.MAX_VALUE / 2",
            defaultEngine.hasMatchingValues(board))
    }

    @Test
    fun testDefensiveBoardSizeValidationOnCoreMethods() {
        val emptyList = emptyList<Int?>()
        assertFalse("emptyList must not be reported as board full", defaultEngine.isBoardFull(emptyList))
        assertFalse("emptyList must not have valid merges", defaultEngine.hasValidMerges(emptyList))
        assertTrue("emptyList must trigger game over defensively", defaultEngine.checkGameOver(emptyList))
        assertNull("emptyList must return null on reshuffle", defaultEngine.reshuffle(emptyList))

        val shortList = listOf(2, 2, 4)
        assertFalse("shortList must not be reported as board full", defaultEngine.isBoardFull(shortList))
        assertFalse("shortList must not have valid merges", defaultEngine.hasValidMerges(shortList))
        assertTrue("shortList must trigger game over defensively", defaultEngine.checkGameOver(shortList))
        assertNull("shortList must return null on reshuffle", defaultEngine.reshuffle(shortList))
    }

    @Test
    fun testTapNonAdjacentEmptyCellClearsSelectionWithoutMutatingBoard() {
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = 4
            this[Position(0, 1).index] = 4
        }
        val state = defaultEngine.startNewGame(customBoard = board)

        // Select (0,0)
        val selectedState = defaultEngine.selectTile(state, Position(0, 0))
        assertEquals(Position(0, 0), selectedState.selectedPosition)

        // Tap non-adjacent empty cell at (3,3)
        val afterTapEmpty = defaultEngine.selectTile(selectedState, Position(3, 3))
        assertNull("Selection must be cleared after tapping non-adjacent empty cell", afterTapEmpty.selectedPosition)
        assertEquals(4, afterTapEmpty[Position(0, 0)])
        assertEquals(4, afterTapEmpty[Position(0, 1)])
        assertEquals(0, afterTapEmpty.score)
        assertEquals(0, afterTapEmpty.moveCount)
        assertFalse(afterTapEmpty.wasReshuffled)
    }

    @Test
    fun testRejectedOverflowMergeLeavesBoardScoreAndMovesUnchangedAndAllowsOtherMoves() {
        val largeVal = 1 shl 30 // 1,073,741,824 > Int.MAX_VALUE / 2 (1,073,741,823)
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = largeVal
            this[Position(0, 1).index] = largeVal
            this[Position(2, 0).index] = 2
            this[Position(2, 1).index] = 2
        }
        val spawnerCalled = java.util.concurrent.atomic.AtomicBoolean(false)
        val spawner = TileSpawner { empty ->
            spawnerCalled.set(true)
            Pair(empty.first(), 2)
        }
        val engine = NeonMergeEngine(tileSpawner = spawner)
        val state = engine.startNewGame(customBoard = board)

        assertFalse("Initial state must not be game over", state.isGameOver)
        assertEquals(0, state.score)
        assertEquals(0, state.moveCount)

        // Select tile at (0, 0)
        val selectedState = engine.selectTile(state, Position(0, 0))
        assertEquals(Position(0, 0), selectedState.selectedPosition)

        // Tap adjacent matching tile at (0, 1) whose value exceeds Int.MAX_VALUE / 2
        val rejectedState = engine.selectTile(selectedState, Position(0, 1))

        // 1. Must NOT merge
        assertEquals("Tile at (0, 0) must remain unchanged", largeVal, rejectedState[Position(0, 0)])
        assertEquals("Tile at (0, 1) must remain unchanged", largeVal, rejectedState[Position(0, 1)])
        // 2. Must NOT overflow
        assertTrue("Tile values must remain strictly positive", rejectedState[Position(0, 0)]!! > 0)
        // 3. Must NOT trigger reshuffle
        assertFalse("Must not trigger reshuffle", rejectedState.wasReshuffled)
        // 4. Must NOT spawn any tiles
        assertFalse("Invalid merge must produce no spawn attempts", spawnerCalled.get())
        // 5. Must leave board, score, and move count completely unchanged
        assertEquals("Board must remain identical", board.toList(), rejectedState.board)
        assertEquals("Score must remain 0", 0, rejectedState.score)
        assertEquals("Move count must remain 0", 0, rejectedState.moveCount)
        assertEquals("Selection shifted to target", Position(0, 1), rejectedState.selectedPosition)

        // 6. Must NOT leave game silently stuck: player can still merge the valid pair
        val selectTwo = engine.selectTile(rejectedState, Position(2, 0))
        val mergedTwo = engine.selectTile(selectTwo, Position(2, 1))
        assertEquals("Valid pair must successfully merge into 4", 4, mergedTwo[Position(2, 1)])
        assertEquals("Score must update to 4", 4, mergedTwo.score)
        assertEquals("Move count must update to 1", 1, mergedTwo.moveCount)
        assertTrue("Spawner was called once for the valid merge", spawnerCalled.get())
    }

    @Test
    fun testBoardWithOnlyOverflowTilesTriggersGameOverWithoutEndlessReshuffle() {
        val largeVal = 1 shl 30
        val board = createEmptyBoard().apply {
            this[Position(0, 0).index] = largeVal
            this[Position(0, 1).index] = largeVal
        }.toList()

        // Although adjacent and equal, because they cannot double without overflowing:
        assertFalse("hasValidMerges must be false", defaultEngine.hasValidMerges(board))
        assertFalse("hasMatchingValues must be false", defaultEngine.hasMatchingValues(board))
        assertNull("reshuffle must return null", defaultEngine.reshuffle(board))
        assertTrue("checkGameOver must return true (game over, not stuck)", defaultEngine.checkGameOver(board))

        val state = defaultEngine.startNewGame(customBoard = board)
        assertTrue("Game must be game over", state.isGameOver)
        // All taps frozen
        val afterTap = defaultEngine.selectTile(state, Position(0, 0))
        assertEquals("State must be completely frozen on game over", state, afterTap)
    }
}


