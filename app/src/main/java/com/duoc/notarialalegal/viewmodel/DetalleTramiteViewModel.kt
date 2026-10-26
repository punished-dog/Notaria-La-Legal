package com.duoc.notarialalegal.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.ArchivoRepository
import com.duoc.notarialalegal.repository.TramiteRepository
import kotlinx.coroutines.flow.Flow

class DetalleTramiteViewModel(application: Application) : AndroidViewModel(application) {
    private val archivoRepository = ArchivoRepository(application)

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
        if (uri != null) {
            documentoCorregido = archivoRepository.obtenerNombre(uri)
            errorCorreccion = ""
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
        if (documentoCorregido.isEmpty()) {
            errorCorreccion = "Adjunta el documento corregido antes de reenviar."
            return
        }
        TramiteRepository.reenviar(tramite, documentoCorregido, comentario, cliente.nombre)
        documentoCorregido = ""
        comentario = ""
        mensaje = "La corrección del trámite ${tramite.codigo()} fue enviada al notario."
    }

    fun cerrarMensaje() {
        mensaje = ""
    }
}
