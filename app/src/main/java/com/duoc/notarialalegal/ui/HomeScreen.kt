package com.duoc.notarialalegal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.R
import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    usuario: Usuario,
    onNuevoTramite: () -> Unit,
    onVerTramite: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val esNotario = usuario.rol == Roles.NOTARIO
    // La lista se actualiza sola cuando cambia un trámite
    val tramites by remember(usuario) { viewModel.obtenerTramites(usuario) }
        .collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.safeDrawingPadding(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Mismo logo del login, más pequeño y alineado a la izquierda
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.logo_la_legal),
                        contentDescription = "Logo Notaria La Legal",
                        modifier = Modifier.widthIn(max = 200.dp).fillMaxWidth().height(60.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.CenterStart,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Bienvenido!", style = MaterialTheme.typography.titleLarge)
                    Text("Hola, ${usuario.nombre} (${usuario.rol})")
                }
            }

            item {
                if (esNotario) {
                    Text(
                        "Revisa los trámites enviados y apruébalos o devuélvelos con observaciones.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Button(
                        onClick = onNuevoTramite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Nuevo trámite")
                    }
                }
            }

            item {
                Text(
                    if (esNotario) "Trámites recibidos" else "Mis trámites",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (tramites.isEmpty()) {
                item {
                    Text("Todavía no hay trámites registrados.")
                }
            }

            items(tramites, key = { it.id }) { tramite ->
                TarjetaTramite(tramite = tramite, mostrarCliente = esNotario, onClick = { onVerTramite(tramite.id) })
            }

            item {
                Column {
                    Text(
                        "Todos los datos son ficticios y solo sirven para la demostración.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(onClick = { viewModel.abrirCerrarSesion() }) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }

    if (viewModel.mostrarCerrarSesion) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarCerrarSesion() },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelarCerrarSesion()
                    onLogout()
                }) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarCerrarSesion() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaTramite(tramite: Tramite, mostrarCliente: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tramite.codigo(), style = MaterialTheme.typography.titleMedium)
                EtiquetaEstado(tramite.estado)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(tramite.tipo, style = MaterialTheme.typography.bodyLarge)
            Text("Firma: ${tramite.tipoFirma}", style = MaterialTheme.typography.bodySmall)
            if (mostrarCliente) {
                Text("Cliente: ${tramite.correoCliente}", style = MaterialTheme.typography.bodySmall)
            }
            Text("Toca para ver el detalle", color = MaterialTheme.colorScheme.primary)
        }
    }
}
