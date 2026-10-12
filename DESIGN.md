# Neon Merge — Design Note

## Game Overview
Neon Merge is a small Android number-merging puzzle game featuring a cyber-neon aesthetic. The player combines matching numbered tiles on a grid to reach higher numbers while managing board space.

## Gameplay
The game is played on a 4×4 grid. The player selects a tile by tapping it, and then taps an orthogonally adjacent tile with the same value. Merging clears the source tile, doubles the value at the target cell, adds the new value to the score, and increments the move counter. After each successful merge, a new tile (valued 2 with a 90% chance, or 4 with a 10% chance) spawns in a random empty cell. If no valid adjacent moves remain but matching numbers exist on the board, the game automatically reshuffles the tiles to guarantee at least one valid merge. If the board fills up and no moves are possible, the game ends and displays a game-over dialog with the final score, move count, and session best score.

## Design Decisions
To create an engaging look without cluttering the screen, I chose a dark background with bright neon accents (cyan, purple, and green). Selected tiles have a distinct glowing border so the player always knows what is currently picked. Key controls like Restart and Settings are kept prominent and easy to tap, and score and move counters sit clearly at the top and bottom so the player can track progress at a glance.

## Implementation
The app is built with pure Kotlin and modern Jetpack Compose (Material 3). To keep the code clean and testable, the core game engine (`NeonMergeEngine`) is completely decoupled from the UI layer. The game state is immutable, and the Compose UI observes changes through state holders. A dedicated unit test suite covers board generation, merge mechanics, overflow protection, fair reshuffling, and game-over conditions.

## Limitations
In the current version, the player's best score persists across game restarts only during the active app session, resetting when the app process is closed. Persistent local storage (such as DataStore) and sound effects are not yet implemented.
