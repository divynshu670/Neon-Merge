package com.example.neonmerge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonmerge.ui.theme.DarkNavyBorder
import com.example.neonmerge.ui.theme.DarkNavyElevated
import com.example.neonmerge.ui.theme.DarkNavySurface
import com.example.neonmerge.ui.theme.NeonMagenta
import com.example.neonmerge.ui.theme.NeonViolet
import com.example.neonmerge.ui.theme.TextPrimary
import com.example.neonmerge.ui.theme.TextSecondary

@Composable
fun ControlsSection(
    moveCount: Int,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGameOver: Boolean = false,
    wasReshuffled: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row with Move Counter & Restart Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Move Counter Card
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavySurface)
                    .border(1.dp, DarkNavyBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Total moves made: $moveCount"
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MOVES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = NeonViolet
                )

                Text(
                    text = moveCount.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = TextPrimary
                )
            }

            // Restart Button (Min 48dp touch target)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                NeonViolet.copy(alpha = 0.2f),
                                DarkNavyElevated
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(NeonViolet, DarkNavyBorder)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = NeonViolet),
                        onClick = onRestartClick
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .semantics {
                        contentDescription = "Restart game and reset board"
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = NeonViolet,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "RESTART",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = TextPrimary
                )
            }
        }

        // Compact instruction banner explaining adjacent matching tiles and game-over state
        val bannerBorderColor = when {
            isGameOver -> NeonMagenta.copy(alpha = 0.8f)
            wasReshuffled -> NeonViolet.copy(alpha = 0.8f)
            else -> DarkNavyBorder.copy(alpha = 0.8f)
        }
        val bannerBgColor = when {
            isGameOver -> NeonMagenta.copy(alpha = 0.15f)
            wasReshuffled -> NeonViolet.copy(alpha = 0.15f)
            else -> DarkNavySurface.copy(alpha = 0.6f)
        }
        val bannerTextColor = when {
            isGameOver -> NeonMagenta
            wasReshuffled -> TextPrimary
            else -> TextSecondary
        }
        val bannerIcon = when {
            isGameOver -> "⚠️ "
            wasReshuffled -> "🔀 "
            else -> "💡 "
        }
        val bannerText = when {
            isGameOver -> "GAME OVER — No valid merges left! Tap RESTART"
            wasReshuffled -> "No moves found — tiles reshuffled!"
            else -> "Tap adjacent matching tiles to select and merge"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bannerBgColor)
                .border(
                    width = 1.dp,
                    color = bannerBorderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .semantics {
                    contentDescription = bannerText
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = bannerIcon,
                    fontSize = 12.sp
                )
                Text(
                    text = bannerText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    ),
                    color = bannerTextColor
                )
            }
        }
    }
}
