package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AbisorLightColorScheme = lightColorScheme(
    primary = DeepGreen,
    onPrimary = PureWhite,
    primaryContainer = MintBubble,
    onPrimaryContainer = DeepGreenDark,
    secondary = DeepGreenLight,
    onSecondary = PureWhite,
    secondaryContainer = MintSurface,
    onSecondaryContainer = LightTextPrimary,
    tertiary = CrimsonAccent,
    onTertiary = PureWhite,
    background = CreamBackground,
    onBackground = LightTextPrimary,
    surface = PureWhite,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFE7ECEA),
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    error = CrimsonAccent,
    onError = PureWhite
)

private val AbisorDarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    onPrimary = DarkOnPrimaryGreen,
    primaryContainer = DarkUserBubble,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DeepGreenLight,
    onSecondary = PureWhite,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = Color(0xFFFF7A6B),
    onTertiary = Color(0xFF420600),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    error = Color(0xFFFF7A6B),
    onError = Color(0xFF420600)
)

val AbisorShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AbisorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AbisorDarkColorScheme else AbisorLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AbisorShapes,
        content = content
    )
}
