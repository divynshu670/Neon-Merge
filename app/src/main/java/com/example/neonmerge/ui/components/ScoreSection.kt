package com.example.neonmerge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonmerge.ui.theme.DarkNavyBorder
import com.example.neonmerge.ui.theme.DarkNavySurface
import com.example.neonmerge.ui.theme.NeonAmber
import com.example.neonmerge.ui.theme.NeonCyan
import com.example.neonmerge.ui.theme.NeonViolet
import com.example.neonmerge.ui.theme.TextMuted
import com.example.neonmerge.ui.theme.TextPrimary

@Composable
fun ScoreSection(
    score: Int,
    bestScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Score Card
        ScoreCard(
            title = "SCORE",
            value = score,
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f)
        )

        // Best Score Card
        ScoreCard(
            title = "BEST",
            value = bestScore,
            accentColor = NeonAmber,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreCard(
    title: String,
    value: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkNavySurface)
            .border(1.dp, DarkNavyBorder, RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "$title score is $value"
            }
    ) {
        // Decorative neon accent sliver at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(accentColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = TextPrimary
            )
        }
    }
}
