package com.duoc.notarialalegal.ui

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.duoc.notarialalegal.repository.ArchivoRepository

// Los launchers son de la vista; el ViewModel se encarga de guardar el documento.
@Composable
fun SelectorDocumento(
    habilitado: Boolean,
    onSeleccionar: (Uri?) -> Unit,
    onPrepararFoto: () -> Uri?,
    onErrorCamara: () -> Unit
) {
    var fotoPendiente by rememberSaveable { mutableStateOf("") }
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { onSeleccionar(it) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        if (guardada && fotoPendiente.isNotEmpty()) onSeleccionar(Uri.parse(fotoPendiente))
        fotoPendiente = ""
    }
    Button(onClick = { selector.launch(ArchivoRepository.TIPOS_PERMITIDOS) }, enabled = habilitado, modifier = Modifier.fillMaxWidth()) {
        Text("Seleccionar archivo")
    }
    OutlinedButton(
        onClick = {
            val uri = onPrepararFoto()
            if (uri != null) {
                fotoPendiente = uri.toString()
                try { camara.launch(uri) }
                catch (_: ActivityNotFoundException) { fotoPendiente = ""; onErrorCamara() }
                catch (_: SecurityException) { fotoPendiente = ""; onErrorCamara() }
            }
        },
        enabled = habilitado, modifier = Modifier.fillMaxWidth()
    ) { Text("Tomar fotografía") }
    Text("PDF, JPG o PNG. Máximo 2 MB.")
}
