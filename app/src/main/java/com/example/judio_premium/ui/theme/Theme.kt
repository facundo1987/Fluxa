package com.example.judio_premium.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    secondary = VioletSecondary,
    tertiary = VioletSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OnDarkSurface,
    onSurface = OnDarkSurface,
    error = ErrorColor
)

@Composable
fun Judio_premiumTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}
