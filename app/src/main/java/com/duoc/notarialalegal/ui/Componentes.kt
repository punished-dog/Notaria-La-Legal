package com.duoc.notarialalegal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.ui.theme.StatusApproved
import com.duoc.notarialalegal.ui.theme.StatusObserved
import com.duoc.notarialalegal.ui.theme.StatusSent

// Barra azul superior que se repite en las pantallas internas
@Composable
fun EncabezadoApp(titulo: String, subtitulo: String = "", onVolver: (() -> Unit)? = null) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)) {
            if (onVolver != null) {
                TextButton(onClick = onVolver) {
                    Text("← Volver", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Text(
                titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            if (subtitulo.isNotEmpty()) {
                Text(
                    subtitulo,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

// Tarjeta blanca con título que agrupa una sección de una pantalla
@Composable
fun TarjetaSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            contenido()
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

// Etiqueta con el estado del trámite, con un color distinto para cada estado
@Composable
fun EtiquetaEstado(estado: String) {
    Surface(color = colorDeEstado(estado).copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
        Text(
            estado,
            color = colorDeEstado(estado),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
