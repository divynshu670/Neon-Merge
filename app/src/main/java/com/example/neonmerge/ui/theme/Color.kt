package com.example.neonmerge.ui.theme

import androidx.compose.ui.graphics.Color

// Core Theme Palette - Deep Cyber Navy
val DarkNavyBackground = Color(0xFF090D16)
val DarkNavySurface = Color(0xFF111726)
val DarkNavyElevated = Color(0xFF182035)
val DarkNavyBorder = Color(0xFF232D48)
val BoardBackground = Color(0xFF0D1322)
val BoardSlotEmpty = Color(0xFF141C30)

// Neon Accents
val NeonCyan = Color(0xFF00F5D4)
val NeonCyanMuted = Color(0xFF00BFA5)
val NeonViolet = Color(0xFFA855F7)
val NeonVioletMuted = Color(0xFF8B5CF6)
val NeonMagenta = Color(0xFFF72585)
val NeonAmber = Color(0xFFFFB703)
val NeonBlue = Color(0xFF3B82F6)
val NeonEmerald = Color(0xFF10B981)
val NeonOrange = Color(0xFFFB8500)

// Text & Content
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Visual specification for game tiles
data class TileVisual(
    val background: Color,
    val border: Color,
    val text: Color,
    val glow: Color
)

fun getTileVisual(value: Int): TileVisual {
    return when (value) {
        0 -> TileVisual(
            background = BoardSlotEmpty,
            border = Color(0xFF1B243B),
            text = Color.Transparent,
            glow = Color.Transparent
        )
        2 -> TileVisual(
            background = Color(0xFF0B2233),
            border = NeonCyan.copy(alpha = 0.7f),
            text = NeonCyan,
            glow = NeonCyan.copy(alpha = 0.25f)
        )
        4 -> TileVisual(
            background = Color(0xFF0A2B26),
            border = NeonEmerald.copy(alpha = 0.75f),
            text = Color(0xFF34D399),
            glow = NeonEmerald.copy(alpha = 0.25f)
        )
        8 -> TileVisual(
            background = Color(0xFF13203E),
            border = NeonBlue.copy(alpha = 0.8f),
            text = Color(0xFF60A5FA),
            glow = NeonBlue.copy(alpha = 0.25f)
        )
        16 -> TileVisual(
            background = Color(0xFF26153D),
            border = NeonViolet.copy(alpha = 0.85f),
            text = Color(0xFFC084FC),
            glow = NeonViolet.copy(alpha = 0.3f)
        )
        32 -> TileVisual(
            background = Color(0xFF33122D),
            border = NeonMagenta.copy(alpha = 0.85f),
            text = Color(0xFFFF69B4),
            glow = NeonMagenta.copy(alpha = 0.3f)
        )
        64 -> TileVisual(
            background = Color(0xFF331E0F),
            border = NeonOrange.copy(alpha = 0.9f),
            text = Color(0xFFFDBA74),
            glow = NeonOrange.copy(alpha = 0.3f)
        )
        128 -> TileVisual(
            background = Color(0xFF332A0C),
            border = NeonAmber,
            text = Color(0xFFFDE047),
            glow = NeonAmber.copy(alpha = 0.35f)
        )
        else -> TileVisual(
            background = Color(0xFF330D1E),
            border = Color(0xFFFF007A),
            text = Color.White,
            glow = Color(0xFFFF007A).copy(alpha = 0.4f)
        )
    }
}