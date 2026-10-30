package com.duoc.notarialalegal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// La app usa siempre el tema claro para mantener el diseño blanco con azul
private val ColoresApp = lightColorScheme(
    primary = Azul,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulOscuro,
    secondary = AzulOscuro,
    onSecondary = Color.White,
    background = FondoApp,
    onBackground = TextoPrincipal,
    surface = FondoApp,
    onSurface = TextoPrincipal,
    surfaceVariant = Color.White,
    onSurfaceVariant = TextoSecundario,
    outline = Borde
)

// Bordes redondeados para campos de texto, tarjetas y modales
private val FormasApp = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun NotariaLaLegalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColoresApp,
        typography = Typography,
        shapes = FormasApp,
        content = content
    )
}
