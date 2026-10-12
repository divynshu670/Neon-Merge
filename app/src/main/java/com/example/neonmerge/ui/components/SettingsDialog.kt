package com.example.neonmerge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.neonmerge.ui.theme.DarkNavyElevated
import com.example.neonmerge.ui.theme.DarkNavySurface
import com.example.neonmerge.ui.theme.NeonCyan
import com.example.neonmerge.ui.theme.NeonViolet
import com.example.neonmerge.ui.theme.TextMuted
import com.example.neonmerge.ui.theme.TextPrimary
import com.example.neonmerge.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var highContrastEnabled by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, DarkNavyBorder, RoundedCornerShape(24.dp)),
        containerColor = DarkNavyElevated,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = NeonCyan
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingToggleItem(
                    title = "Sound Effects",
                    description = "Play audio on tile merge and select",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )

                SettingToggleItem(
                    title = "Haptic Feedback",
                    description = "Vibrate when tapping tiles",
                    checked = hapticsEnabled,
                    onCheckedChange = { hapticsEnabled = it }
                )

                SettingToggleItem(
                    title = "Vibrant Neon Glow",
                    description = "Enhanced luminous tile effects",
                    checked = highContrastEnabled,
                    onCheckedChange = { highContrastEnabled = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.semantics {
                    contentDescription = "Close settings dialog"
                }
            ) {
                Text(
                    text = "CLOSE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NeonCyan
                )
            }
        }
    )
}

@Composable
private fun SettingToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkNavySurface
            ),
            modifier = Modifier.semantics {
                contentDescription = "$title toggle"
            }
        )
    }
}
