package com.duoc.notarialalegal.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.ui.theme.DarkPetroleum
import com.duoc.notarialalegal.ui.theme.ObservationBackground
import com.duoc.notarialalegal.ui.theme.StatusApproved
import com.duoc.notarialalegal.ui.theme.StatusObserved
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

    Surface(modifier = Modifier.fillMaxSize()) {
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
    var verHistorial by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(24.dp)) {
        DatosTramite(tramite)

        if (tramite.estado == EstadoTramite.OBSERVADO && tramite.observacion.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ObservationBackground,
                    contentColor = DarkPetroleum
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Observación del notario", style = MaterialTheme.typography.titleMedium, color = StatusObserved)
                    Text(tramite.observacion)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        val esNotario = usuario.rol == Roles.NOTARIO
        if (esNotario && tramite.estado == EstadoTramite.ENVIADO) {
            AccionesNotario(viewModel, tramite, usuario)
        } else if (!esNotario && tramite.estado == EstadoTramite.OBSERVADO) {
            AccionesCliente(viewModel, tramite, usuario, onSeleccionarArchivo = onSeleccionarArchivo)
        } else if (tramite.estado == EstadoTramite.APROBADO) {
            Text("Trámite finalizado. El notario aprobó la solicitud.", color = StatusApproved)
        } else if (tramite.estado == EstadoTramite.OBSERVADO) {
            Text("El trámite está esperando la corrección del cliente.")
        } else {
            Text("El trámite está esperando la revisión del notario.")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Historial", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { verHistorial = !verHistorial }) {
                Text(if (verHistorial) "Ocultar" else "Mostrar")
            }
        }
        AnimatedVisibility(visible = verHistorial) {
            Column {
                for (registro in historial) {
                    FilaHistorial(registro)
                }
            }
        }
    }
}

@Composable
private fun DatosTramite(tramite: Tramite) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EtiquetaEstado(tramite.estado)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Documento: ${tramite.documento}")
            Text("Firma: ${tramite.tipoFirma}")
            Text("Cliente: ${tramite.correoCliente}")
            Text("Creado: ${tramite.fechaCreacion}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Antecedentes", style = MaterialTheme.typography.titleSmall)
            Text(tramite.antecedentes)
        }
    }
}

@Composable
private fun AccionesNotario(viewModel: DetalleTramiteViewModel, tramite: Tramite, notario: Usuario) {
    Text("Revisión del notario", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Aprobar trámite")
    }
    OutlinedButton(onClick = { viewModel.devolver(tramite, notario) }, modifier = Modifier.fillMaxWidth()) {
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
    Text("Corregir trámite", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(onClick = onSeleccionarArchivo, modifier = Modifier.fillMaxWidth()) {
        Text("Adjuntar documento corregido")
    }
    Text(
        if (viewModel.documentoCorregido.isNotEmpty()) "Seleccionado: ${viewModel.documentoCorregido}"
        else "Ningún archivo seleccionado"
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
    Button(onClick = { viewModel.reenviar(tramite, cliente) }, modifier = Modifier.fillMaxWidth()) {
        Text("Reenviar al notario")
    }
}

@Composable
private fun FilaHistorial(registro: Historial) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(registro.fecha, style = MaterialTheme.typography.bodySmall)
        Text(registro.estado, style = MaterialTheme.typography.titleSmall, color = colorDeEstado(registro.estado))
        Text(registro.detalle)
        Text("Responsable: ${registro.responsable}", style = MaterialTheme.typography.bodySmall)
    }
    HorizontalDivider()
}
