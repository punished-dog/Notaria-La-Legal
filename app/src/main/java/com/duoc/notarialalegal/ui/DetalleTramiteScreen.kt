package com.duoc.notarialalegal.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.ui.theme.ObservationBackground
import com.duoc.notarialalegal.ui.theme.StatusApproved
import com.duoc.notarialalegal.ui.theme.StatusObserved
import com.duoc.notarialalegal.ui.theme.TextoPrincipal
import com.duoc.notarialalegal.viewmodel.DetalleTramiteViewModel

@Composable
fun DetalleTramiteScreen(
    viewModel: DetalleTramiteViewModel,
    tramiteId: Int,
    usuario: Usuario,
    onVolver: () -> Unit
) {
    val tramite by remember(tramiteId) { viewModel.obtenerTramite(tramiteId) }.collectAsState(initial = null)
    val historial by remember(tramiteId) { viewModel.obtenerHistorial(tramiteId) }.collectAsState(initial = emptyList())

    val selectorArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.seleccionarArchivo(uri)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            val tramiteActual = tramite
            EncabezadoApp(
                titulo = tramiteActual?.codigo() ?: "Trámite",
                subtitulo = tramiteActual?.tipo ?: "",
                onVolver = onVolver
            )

            if (tramiteActual == null) {
                Text("Cargando trámite...", modifier = Modifier.padding(24.dp))
            } else {
                ContenidoDetalle(
                    viewModel = viewModel,
                    tramite = tramiteActual,
                    historial = historial,
                    usuario = usuario,
                    onSeleccionarArchivo = { selectorArchivo.launch("*/*") }
                )
            }
        }
    }

    if (viewModel.mensaje.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarMensaje() },
            title = { Text("Notaria La Legal") },
            text = { Text(viewModel.mensaje) },
            confirmButton = {
                TextButton(onClick = { viewModel.cerrarMensaje() }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun ContenidoDetalle(
    viewModel: DetalleTramiteViewModel,
    tramite: Tramite,
    historial: List<Historial>,
    usuario: Usuario,
    onSeleccionarArchivo: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DatosTramite(tramite)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val uri = viewModel.obtenerUriDocumento(tramite)
                if (uri != null) {
                    val abrirDocumento = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, tramite.tipoDocumento)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(abrirDocumento)
                    } catch (error: ActivityNotFoundException) {
                        viewModel.mostrarErrorDocumento()
                    } catch (error: SecurityException) {
                        viewModel.mostrarErrorDocumento()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver documento")
        }

        if (tramite.estado == EstadoTramite.OBSERVADO && tramite.observacion.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ObservationBackground,
                    contentColor = TextoPrincipal
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Observación del notario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusObserved
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(tramite.observacion)
                }
            }
        }

        // Cada perfil ve solo las acciones que le corresponden según el estado
        val esNotario = usuario.rol == Roles.NOTARIO
        if (esNotario && tramite.estado == EstadoTramite.ENVIADO) {
            TarjetaSeccion(titulo = "Revisión del notario") {
                AccionesNotario(viewModel, tramite, usuario)
            }
        } else if (!esNotario && tramite.estado == EstadoTramite.OBSERVADO) {
            TarjetaSeccion(titulo = "Corregir trámite") {
                AccionesCliente(viewModel, tramite, usuario, onSeleccionarArchivo = onSeleccionarArchivo)
            }
        } else if (tramite.estado == EstadoTramite.APROBADO) {
            Text("Trámite finalizado. El notario aprobó la solicitud.", color = StatusApproved)
        } else if (tramite.estado == EstadoTramite.OBSERVADO) {
            Text("El trámite está esperando la corrección del cliente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("El trámite está esperando la revisión del notario.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        TarjetaSeccion(titulo = "Historial") {
            for (registro in historial) {
                FilaHistorial(registro)
            }
        }
    }
}

@Composable
private fun DatosTramite(tramite: Tramite) {
    TarjetaSeccion(titulo = "Datos del trámite") {
        EtiquetaEstado(tramite.estado)
        Spacer(modifier = Modifier.height(12.dp))
        DatoTramite("Documento", tramite.documento)
        DatoTramite("Firma", tramite.tipoFirma)
        DatoTramite("Cliente", tramite.correoCliente)
        DatoTramite("Creado", tramite.fechaCreacion)
        DatoTramite("Antecedentes", tramite.antecedentes)
    }
}

// Muestra un dato con su nombre en gris arriba
@Composable
private fun DatoTramite(nombre: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(nombre, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AccionesNotario(viewModel: DetalleTramiteViewModel, tramite: Tramite, notario: Usuario) {
    OutlinedTextField(
        value = viewModel.observacion,
        onValueChange = { viewModel.actualizarObservacion(it) },
        label = { Text("Observación (solo si lo devuelves)") },
        minLines = 2,
        isError = viewModel.errorObservacion.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )
    MensajeError(viewModel.errorObservacion)
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = { viewModel.aprobar(tramite, notario) },
        colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text("Aprobar trámite")
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(onClick = { viewModel.devolver(tramite, notario) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text("Devolver con observaciones")
    }
}

@Composable
private fun AccionesCliente(
    viewModel: DetalleTramiteViewModel,
    tramite: Tramite,
    cliente: Usuario,
    onSeleccionarArchivo: () -> Unit
) {
    OutlinedButton(onClick = onSeleccionarArchivo, modifier = Modifier.fillMaxWidth()) {
        Text("Adjuntar documento corregido")
    }
    Text(
        if (viewModel.documentoCorregido.isNotEmpty()) "Seleccionado: ${viewModel.documentoCorregido}"
        else "Ningún archivo seleccionado",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
    MensajeError(viewModel.errorCorreccion)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = viewModel.comentario,
        onValueChange = { viewModel.actualizarComentario(it) },
        label = { Text("Comentario para el notario (opcional)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = { viewModel.reenviar(tramite, cliente) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text("Reenviar al notario")
    }
}

@Composable
private fun FilaHistorial(registro: Historial) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(registro.estado, style = MaterialTheme.typography.titleSmall, color = colorDeEstado(registro.estado))
        Text(registro.detalle)
        Text(
            "${registro.fecha} · ${registro.responsable}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}
