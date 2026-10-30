package com.duoc.notarialalegal.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.duoc.notarialalegal.model.Catalogo
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.viewmodel.NuevoTramiteViewModel

@Composable
fun NuevoTramiteScreen(viewModel: NuevoTramiteViewModel, usuario: Usuario, onTerminar: () -> Unit) {
    // Solo controla si la lista desplegable está abierta, por eso queda en la vista
    var menuTiposAbierto by remember { mutableStateOf(false) }

    // Abre el selector de archivos del sistema (recurso nativo del teléfono)
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
            EncabezadoApp(titulo = "Nuevo trámite", subtitulo = "Completa los 4 pasos", onVolver = onTerminar)

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaSeccion(titulo = "1. Tipo de trámite") {
                    Box {
                        OutlinedButton(onClick = { menuTiposAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(viewModel.tipo.ifEmpty { "Selecciona un tipo de trámite" })
                        }
                        DropdownMenu(expanded = menuTiposAbierto, onDismissRequest = { menuTiposAbierto = false }) {
                            for (tipo in Catalogo.tiposTramite) {
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        viewModel.seleccionarTipo(tipo)
                                        menuTiposAbierto = false
                                    }
                                )
                            }
                        }
                    }
                    MensajeError(viewModel.errorTipo)
                }

                TarjetaSeccion(titulo = "2. Documento") {
                    OpcionRadio(
                        texto = "Adjuntar documento propio",
                        seleccionado = !viewModel.usarFormato,
                        onClick = { viewModel.cambiarOrigenDocumento(false) }
                    )
                    OpcionRadio(
                        texto = "Usar formato predefinido",
                        seleccionado = viewModel.usarFormato,
                        onClick = { viewModel.cambiarOrigenDocumento(true) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // El botón para adjuntar solo se muestra si se eligió documento propio
                    if (!viewModel.usarFormato) {
                        Button(onClick = { selectorArchivo.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                            Text("Seleccionar archivo")
                        }
                        Text(
                            if (viewModel.nombreArchivo.isNotEmpty()) "Seleccionado: ${viewModel.nombreArchivo}"
                            else "Ningún archivo seleccionado",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (viewModel.tipo.isNotEmpty()) {
                        Text(
                            "Se usará el formato predefinido de \"${viewModel.tipo}\".",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MensajeError(viewModel.errorDocumento)
                }

                TarjetaSeccion(titulo = "3. Tipo de firma") {
                    for (firma in Catalogo.tiposFirma) {
                        OpcionRadio(
                            texto = firma,
                            seleccionado = viewModel.tipoFirma == firma,
                            onClick = { viewModel.seleccionarFirma(firma) }
                        )
                    }
                    MensajeError(viewModel.errorFirma)
                }

                TarjetaSeccion(titulo = "4. Antecedentes") {
                    OutlinedTextField(
                        value = viewModel.antecedentes,
                        onValueChange = { viewModel.actualizarAntecedentes(it) },
                        label = { Text("Describe el trámite") },
                        minLines = 3,
                        isError = viewModel.errorAntecedentes.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    MensajeError(viewModel.errorAntecedentes)
                }

                Button(
                    onClick = { viewModel.revisarFormulario() },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Revisar y firmar")
                }
            }
        }
    }

    // Modal para confirmar los datos y firmar
    if (viewModel.mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarConfirmacion() },
            title = { Text("Confirmar y firmar") },
            text = {
                Column {
                    Text("Tipo: ${viewModel.tipo}")
                    Text("Documento: ${viewModel.nombreDocumento()}")
                    Text("Firma: ${viewModel.tipoFirma}")
                    Spacer(modifier = Modifier.height(12.dp))
                    if (viewModel.firmando) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Firmando documento...")
                        }
                    } else {
                        Text("Al confirmar, el trámite se enviará al notario para su revisión.")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.firmarYEnviar(usuario) }, enabled = !viewModel.firmando) {
                    Text("Firmar y enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarConfirmacion() }, enabled = !viewModel.firmando) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal que confirma que el trámite quedó guardado
    if (viewModel.codigoCreado.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onTerminar,
            title = { Text("Trámite enviado") },
            text = { Text("El trámite ${viewModel.codigoCreado} fue firmado y enviado al notario.") },
            confirmButton = {
                TextButton(onClick = onTerminar) {
                    Text("Volver al inicio")
                }
            }
        )
    }
}

@Composable
fun OpcionRadio(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionado, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp)
    ) {
        RadioButton(selected = seleccionado, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto)
    }
}

@Composable
fun MensajeError(mensaje: String) {
    if (mensaje.isNotEmpty()) {
        Text(mensaje, color = MaterialTheme.colorScheme.error)
    }
}
