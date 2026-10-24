package com.duoc.notarialalegal.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.ui.theme.StatusApproved
import com.duoc.notarialalegal.ui.theme.StatusObserved
import com.duoc.notarialalegal.ui.theme.StatusSent

// Barra superior que se repite en las pantallas internas
@Composable
fun EncabezadoApp(titulo: String, subtitulo: String = "", onVolver: (() -> Unit)? = null) {
    Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (onVolver != null) {
                TextButton(onClick = onVolver) {
                    Text("< Volver", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            if (subtitulo.isNotEmpty()) {
                Text(subtitulo, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

fun colorDeEstado(estado: String): Color {
    return when (estado) {
        EstadoTramite.APROBADO -> StatusApproved
        EstadoTramite.OBSERVADO -> StatusObserved
        else -> StatusSent
    }
}

// Etiqueta con el estado del trámite. El color cambia con una animación
// cuando el notario aprueba o devuelve el trámite.
@Composable
fun EtiquetaEstado(estado: String) {
    val colorFondo by animateColorAsState(targetValue = colorDeEstado(estado), label = "colorEstado")
    Surface(color = colorFondo, shape = RoundedCornerShape(50)) {
        Text(
            estado,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
