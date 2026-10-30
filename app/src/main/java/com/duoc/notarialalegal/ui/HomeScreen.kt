package com.duoc.notarialalegal.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.safeDrawingPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Encabezado azul con el logo en blanco y el saludo
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_la_legal),
                                contentDescription = "Logo Notaria La Legal",
                                modifier = Modifier.widthIn(max = 170.dp).fillMaxWidth(0.55f),
                                contentScale = ContentScale.FillWidth,
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                            TextButton(onClick = { viewModel.abrirCerrarSesion() }) {
                                Text("Cerrar sesión", color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "¡Bienvenido!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Hola, ${usuario.nombre} · ${usuario.rol}",
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (esNotario) {
                        Text(
                            "Revisa los trámites enviados y apruébalos o devuélvelos con observaciones.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Button(
                            onClick = onNuevoTramite,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Nuevo trámite")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (esNotario) "Trámites recibidos" else "Mis trámites",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (tramites.isEmpty()) {
                item {
                    Text(
                        "Todavía no hay trámites registrados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            items(tramites, key = { it.id }) { tramite ->
                TarjetaTramite(tramite = tramite, mostrarCliente = esNotario, onClick = { onVerTramite(tramite.id) })
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    tramite.codigo(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                EtiquetaEstado(tramite.estado)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(tramite.tipo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                tramite.tipoFirma,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (mostrarCliente) {
                Text(
                    "Cliente: ${tramite.correoCliente}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ver detalle ›", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}
