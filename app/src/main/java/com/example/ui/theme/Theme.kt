package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CosmicPurple,
    onPrimary = Color.White,
    secondary = CosmicBlue,
    onSecondary = Color.Black,
    tertiary = CosmicGold,
    background = CosmicDeepSpace,
    onBackground = CosmicWhite,
    surface = CosmicCardBg,
    onSurface = CosmicWhite,
    outline = CosmicSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = CosmicPurple,
    onPrimary = Color.White,
    secondary = CosmicBlue,
    onSecondary = Color.White,
    tertiary = CosmicGold,
    background = Color(0xFFAEC4E4), // Beautiful light sky blue for light theme fallback
    onBackground = CosmicDeepSpace,
    surface = Color.White,
    onSurface = CosmicDeepSpace,
    outline = Color.Gray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Standardize on custom dark mode space theme by default
    dynamicColor: Boolean = false, // Use our custom-crafted color scheme for maximum branding polish
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
