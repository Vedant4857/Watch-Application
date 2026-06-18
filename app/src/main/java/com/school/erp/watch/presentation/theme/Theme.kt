package com.school.erp.watch.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors

// ─── Color Palette ────────────────────────────────────────────────────────────
val DeepNavy       = Color(0xFF0A0E27)
val RoyalBlue      = Color(0xFF1A3A8F)
val ElectricBlue   = Color(0xFF3D6EFF)
val CyanAccent     = Color(0xFF00C9FF)
val TealGreen      = Color(0xFF00E5A0)
val GoldenYellow   = Color(0xFFFFBB00)
val CoralRed       = Color(0xFF9B5863)
val PurpleAccent   = Color(0xFFB06EFF)
val SoftWhite      = Color(0xFFECF0FF)
val MutedGray      = Color(0xFF7A8BA8)
val DarkCard       = Color(0xFF0F1535)
val CardBorder     = Color(0xFF1E2D5A)

val Black = Color(0xFF000000)

val grey = Color(0xFF202124)
val DarkGrey = Color(0xFF202124)

val MoneyGreen = Color(0xFF4CAF50)

val LeaveOrange = Color(0xFFFF9800)

// Semantic colors
val AttendanceGreen = TealGreen
//val AbsentRed       = CoralRed
val FeesGold        = GoldenYellow
val AdmissionPurple = PurpleAccent
val StaffBlue       = SoftWhite

// ─── Wear OS Color Scheme ─────────────────────────────────────────────────────
private val SchoolWatchColors = Colors(
    primary          = ElectricBlue,
    primaryVariant   = RoyalBlue,
    secondary        = CyanAccent,
    secondaryVariant = TealGreen,
    error            = CoralRed,
    onPrimary        = SoftWhite,
    onSecondary      = DeepNavy,
    onError          = SoftWhite,
    background       = DeepNavy,
    onBackground     = SoftWhite,
    surface          = DarkCard,
    onSurface        = SoftWhite
)

// ─── Theme ────────────────────────────────────────────────────────────────────
@Composable
fun SchoolERPWatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = SchoolWatchColors,
        content = content
    )
}
