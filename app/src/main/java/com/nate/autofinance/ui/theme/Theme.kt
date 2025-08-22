package com.nate.autofinance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = White40,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    background = White,
    primary = Black,
    secondary = White40,
    tertiary = Blue,

    onPrimary = White,
    onSecondary = Grey,
    onTertiary = White,

    primaryContainer = Black,
    secondaryContainer = White40,
    tertiaryContainer = Blue,

    onPrimaryContainer = White,
    onSecondaryContainer = Grey,
    onTertiaryContainer = White,

    error = Error,


)

@Composable
fun AutofinanceTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}