package com.example.neonmerge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonmerge.ui.theme.BoardBackground
import com.example.neonmerge.ui.theme.DarkNavyBorder
import com.example.neonmerge.ui.theme.NeonCyan
import com.example.neonmerge.ui.theme.NeonViolet
import com.example.neonmerge.ui.theme.getTileVisual

@Composable
fun GameBoard(
    tiles: List<Int?>,
    selectedTileIndex: Int?,
    onTileClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .background(BoardBackground)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.35f),
                        NeonViolet.copy(alpha = 0.35f),
                        DarkNavyBorder
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(10.dp)
            .semantics {
                contentDescription = "4 by 4 Neon Merge game board"
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until 4) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 4) {
                        val index = row * 4 + col
                        val value = tiles.getOrNull(index)
                        val isSelected = selectedTileIndex == index

                        GameTile(
                            value = value,
                            isSelected = isSelected,
                            onClick = { onTileClick(index) },
                            row = row,
                            col = col,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameTile(
    value: Int?,
    isSelected: Boolean,
    onClick: () -> Unit,
    row: Int,
    col: Int,
    modifier: Modifier = Modifier
) {
    val visual = getTileVisual(value ?: 0)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "tileScale"
    )

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.5.dp,
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan, NeonViolet)
            ),
            shape = RoundedCornerShape(14.dp)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = visual.border,
            shape = RoundedCornerShape(14.dp)
        )
    }

    val tileContentDescription = if (value != null && value > 0) {
        "Row ${row + 1}, Column ${col + 1}, value $value${if (isSelected) ", selected" else ""}"
    } else {
        "Row ${row + 1}, Column ${col + 1}, empty slot"
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(visual.background)
            .then(borderModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = NeonCyan),
                onClick = onClick
            )
            .semantics {
                contentDescription = tileContentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        if (value != null && value > 0) {
            Text(
                text = value.toString(),
                style = when {
                    value < 100 -> MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                    value < 1000 -> MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    else -> MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                },
                color = visual.text
            )
        }
    }
}
