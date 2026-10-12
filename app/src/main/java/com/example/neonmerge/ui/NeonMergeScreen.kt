package com.example.neonmerge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.neonmerge.game.NeonMergeEngine
import com.example.neonmerge.game.Position
import com.example.neonmerge.ui.components.ControlsSection
import com.example.neonmerge.ui.components.GameBoard
import com.example.neonmerge.ui.components.GameOverDialog
import com.example.neonmerge.ui.components.HeaderSection
import com.example.neonmerge.ui.components.ScoreSection
import com.example.neonmerge.ui.components.SettingsDialog
import com.example.neonmerge.ui.theme.DarkNavyBackground

@Composable
fun NeonMergeScreen(
    modifier: Modifier = Modifier,
    engine: NeonMergeEngine = remember { NeonMergeEngine() }
) {
    // Engine state as the single source of truth for the game
    var gameState by remember { mutableStateOf(engine.startNewGame()) }
    var bestScore by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Preserve and update best score for current session immediately
    if (gameState.score > bestScore) {
        bestScore = gameState.score
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkNavyBackground
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isCompactHeight = maxHeight < 680.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .then(
                        if (isCompactHeight) Modifier.verticalScroll(scrollState)
                        else Modifier
                    )
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header with Title, Icon badge, and Settings
                HeaderSection(
                    onSettingsClick = { showSettingsDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Score Section (SCORE & BEST)
                ScoreSection(
                    score = gameState.score,
                    bestScore = bestScore
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4x4 Game Board rendered directly from engine state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    GameBoard(
                        tiles = gameState.board,
                        selectedTileIndex = gameState.selectedPosition?.index,
                        onTileClick = { clickedIndex ->
                            val target = Position.fromIndex(clickedIndex)
                            gameState = engine.selectTile(gameState, target)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Move Counter, Restart Action, Instruction Banner, and Game-Over indicator
                ControlsSection(
                    moveCount = gameState.moveCount,
                    isGameOver = gameState.isGameOver,
                    wasReshuffled = gameState.wasReshuffled,
                    onRestartClick = {
                        // Reset board, score, and moves, while preserving session bestScore
                        gameState = engine.reset()
                    }
                )
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            onDismissRequest = { showSettingsDialog = false }
        )
    }

    // Polished Game-Over Dialog with final score, moves, best score, and Play Again
    if (gameState.isGameOver) {
        GameOverDialog(
            score = gameState.score,
            bestScore = bestScore,
            moveCount = gameState.moveCount,
            onPlayAgainClick = {
                // Start a fresh game while preserving session bestScore
                gameState = engine.reset()
            }
        )
    }
}
