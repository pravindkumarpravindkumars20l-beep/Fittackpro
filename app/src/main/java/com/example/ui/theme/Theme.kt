package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryActiveLime,
    secondary = SecondaryTeal,
    tertiary = AccentOrange,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = OnDarkPrimary,
    onSecondary = OnDarkText,
    onTertiary = OnDarkPrimary,
    onBackground = OnDarkText,
    onSurface = OnDarkText,
    surfaceVariant = DarkCardBorder,
    outline = OnDarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryActiveLime,
    secondary = SecondaryTeal,
    tertiary = AccentOrange,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = OnDarkPrimary,
    onSecondary = OnDarkText,
    onTertiary = OnDarkPrimary,
    onBackground = OnDarkText,
    onSurface = OnDarkText,
    surfaceVariant = LightCardBorder,
    outline = OnDarkTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
