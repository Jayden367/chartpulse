package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ChartPulseDarkScheme = darkColorScheme(
    primary = BullionGold,
    onPrimary = OnBullionGold,
    primaryContainer = BullionGoldContainer,
    onPrimaryContainer = BullionGold,
    secondary = EmeraldBull,
    onSecondary = OnEmeraldBull,
    secondaryContainer = EmeraldBullContainer,
    onSecondaryContainer = EmeraldBull,
    tertiary = AmberCaution,
    onTertiary = OnBullionGold,
    tertiaryContainer = AmberCautionContainer,
    onTertiaryContainer = AmberCaution,
    error = CrimsonBear,
    onError = SlateTextPrimary,
    errorContainer = CrimsonBearContainer,
    onErrorContainer = CrimsonBear,
    background = ObsidianBg,
    onBackground = SlateTextPrimary,
    surface = ObsidianSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = SlateTextSecondary,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianSurfaceElevated,
    surfaceContainerHigh = ObsidianSurfaceHighest,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorder
)

private val ChartPulseLightScheme = lightColorScheme(
    primary = BullionGoldDark,
    onPrimary = LightSurface,
    primaryContainer = ColorTokensLight.GoldWash,
    onPrimaryContainer = OnBullionGold,
    secondary = EmeraldBull,
    onSecondary = LightSurface,
    secondaryContainer = ColorTokensLight.EmeraldWash,
    onSecondaryContainer = OnEmeraldBull,
    error = CrimsonBear,
    onError = LightSurface,
    errorContainer = ColorTokensLight.CrimsonWash,
    onErrorContainer = OnCrimsonBear,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSurfaceVariant
)

private object ColorTokensLight {
    val GoldWash = androidx.compose.ui.graphics.Color(0xFFFEF3C7)
    val EmeraldWash = androidx.compose.ui.graphics.Color(0xFFD1FAE5)
    val CrimsonWash = androidx.compose.ui.graphics.Color(0xFFFEE2E2)
}

val ChartPulseShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ChartPulseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ChartPulseDarkScheme else ChartPulseLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChartPulseTypography,
        shapes = ChartPulseShapes,
        content = content
    )
}
