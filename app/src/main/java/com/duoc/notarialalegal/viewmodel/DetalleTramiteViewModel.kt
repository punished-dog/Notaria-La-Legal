package com.duoc.notarialalegal.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.DocumentoAdjunto
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.ArchivoRepository
import com.duoc.notarialalegal.repository.TramiteRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class DetalleTramiteViewModel(application: Application) : AndroidViewModel(application) {
    private val archivoRepository = ArchivoRepository(application)

    private var archivoAdjunto: DocumentoAdjunto? = null
    var cargandoDocumento by mutableStateOf(false)
        private set

    // Campo que usa el notario para devolver el trámite
    var observacion by mutableStateOf("")
        private set
    var errorObservacion by mutableStateOf("")
        private set

    // Campos que usa el cliente para corregir
    var documentoCorregido by mutableStateOf("")
        private set
    var comentario by mutableStateOf("")
        private set
    var errorCorreccion by mutableStateOf("")
        private set

    var mensaje by mutableStateOf("")
        private set

    fun obtenerTramite(id: Int): Flow<Tramite?> = TramiteRepository.obtenerPorId(id)

    fun obtenerHistorial(id: Int): Flow<List<Historial>> = TramiteRepository.obtenerHistorial(id)

    fun actualizarObservacion(valor: String) {
        observacion = valor
        errorObservacion = ""
    }

    fun actualizarComentario(valor: String) {
        comentario = valor
    }

    fun seleccionarArchivo(uri: Uri?) {
        if (uri == null || cargandoDocumento) return
        cargandoDocumento = true
        errorCorreccion = ""
        viewModelScope.launch {
            try {
                val copia = withContext(Dispatchers.IO) { archivoRepository.guardarCopia(uri) }
                archivoAdjunto = copia
                documentoCorregido = copia.nombre
            } catch (error: IOException) {
                errorCorreccion = "No se pudo copiar el documento. Inténtalo de nuevo."
            } catch (error: SecurityException) {
                errorCorreccion = "No se puede acceder al archivo. Selecciónalo nuevamente."
            } finally {
                cargandoDocumento = false
            }
        }
    }

    fun aprobar(tramite: Tramite, notario: Usuario) {
        TramiteRepository.aprobar(tramite, notario.nombre)
        mensaje = "El trámite ${tramite.codigo()} fue aprobado."
    }

    fun devolver(tramite: Tramite, notario: Usuario) {
        errorObservacion = Validaciones.validarTextoMinimo(observacion, "la observación")
        if (errorObservacion.isNotEmpty()) {
            return
        }
        TramiteRepository.devolver(tramite, observacion.trim(), notario.nombre)
        observacion = ""
        mensaje = "El trámite ${tramite.codigo()} fue devuelto al cliente con observaciones."
    }

    fun reenviar(tramite: Tramite, cliente: Usuario) {
        if (cargandoDocumento) {
            errorCorreccion = "Espera a que termine de guardarse el documento."
            return
        }
        if (documentoCorregido.isEmpty()) {
            errorCorreccion = "Adjunta el documento corregido antes de reenviar."
            return
        }
        TramiteRepository.reenviar(tramite, documentoCorregido, comentario, cliente.nombre,
            archivoAdjunto?.ruta.orEmpty(), archivoAdjunto?.tipo ?: "application/octet-stream")
        archivoAdjunto = null
        documentoCorregido = ""
        comentario = ""
        mensaje = "La corrección del trámite ${tramite.codigo()} fue enviada al notario."
    }

    fun obtenerUriDocumento(tramite: Tramite): Uri? {
        val uri = archivoRepository.obtenerUri(tramite.rutaDocumento)
        if (uri == null) {
            mensaje = "Este trámite solo tiene un documento de ejemplo o un formato simulado. Adjunta un archivo real para poder abrirlo."
        }
        return uri
    }

    fun mostrarErrorDocumento() {
        mensaje = "No se pudo abrir el documento. Comprueba que haya una aplicación compatible instalada."
    }

    fun cerrarMensaje() {
        mensaje = ""
    }
}
