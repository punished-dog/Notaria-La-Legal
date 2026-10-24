package com.duoc.notarialalegal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Petroleum,
    onPrimary = Color.White,
    primaryContainer = Aqua,
    onPrimaryContainer = DarkPetroleum,
    secondary = Gold,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = DarkPetroleum,
    surface = LightBackground,
    onSurface = DarkPetroleum,
    surfaceVariant = Color.White,
    onSurfaceVariant = DarkPetroleum
)

private val DarkColors = darkColorScheme(
    primary = Aqua,
    onPrimary = DarkPetroleum,
    primaryContainer = Petroleum,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = DarkPetroleum
)

@Composable
fun NotariaLaLegalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
