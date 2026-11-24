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
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.ArchivoRepository
import com.duoc.notarialalegal.repository.TramiteRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CancellationException
import com.duoc.notarialalegal.network.mensajeDeError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class DetalleTramiteViewModel(application: Application) : AndroidViewModel(application) {
    private val archivoRepository = ArchivoRepository(application)

    private var archivoAdjunto: DocumentoAdjunto? = null
    var cargandoDocumento by mutableStateOf(false)
        private set

    var descargando by mutableStateOf(false)
        private set
    var uriDocumento by mutableStateOf<Uri?>(null)
        private set
    var tipoDocumento by mutableStateOf("")
        private set

    var cargando by mutableStateOf(false)
        private set
    var procesando by mutableStateOf(false)
        private set
    var errorCarga by mutableStateOf("")
        private set

    fun cargar(id: Int) {
        if (cargando || procesando) return
        cargando = true
        errorCarga = ""
        viewModelScope.launch {
            try {
                TramiteRepository.cargarDetalle(id)
                TramiteRepository.cargarHistorial(id)
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) { errorCarga = mensajeDeError(error)
            } finally { cargando = false }
        }
    }

    private fun realizarAccion(id: Int, confirmacion: String, accion: suspend () -> Unit) {
        if (procesando || cargando || cargandoDocumento || descargando) return
        procesando = true
        viewModelScope.launch {
            try {
                accion()
                mensaje = confirmacion
                // Si el cambio ya se guardó, un fallo de historial no debe repetirlo.
                try { TramiteRepository.cargarHistorial(id)
                } catch (error: CancellationException) { throw error
                } catch (_: Exception) { mensaje += " No se pudo actualizar el historial; vuelve a cargar el detalle." }
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) { mensaje = mensajeDeError(error)
            } finally { procesando = false }
        }
    }

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
        if (uri == null || cargandoDocumento || procesando) return
        cargandoDocumento = true
        errorCorreccion = ""
        viewModelScope.launch {
            try {
                val copia = withContext(Dispatchers.IO) { archivoRepository.guardarCopia(uri) }
                archivoAdjunto = copia
                documentoCorregido = copia.nombre
            } catch (error: IOException) {
                errorCorreccion = error.message ?: "No se pudo copiar el documento."
            } catch (error: SecurityException) {
                errorCorreccion = "No se puede acceder al archivo. Selecciónalo nuevamente."
            } finally {
                cargandoDocumento = false
            }
        }
    }

    fun aprobar(tramite: Tramite) {
        realizarAccion(tramite.id, "El trámite ${tramite.codigo()} fue aprobado.") {
            TramiteRepository.aprobar(tramite.id)
        }
    }

    fun devolver(tramite: Tramite) {
        errorObservacion = Validaciones.validarTextoMinimo(observacion, "la observación")
        if (errorObservacion.isNotEmpty()) {
            return
        }
        val texto = observacion.trim()
        realizarAccion(tramite.id, "El trámite fue devuelto con observaciones.") {
            TramiteRepository.devolver(tramite.id, texto)
            observacion = ""
        }
    }

    fun reenviar(tramite: Tramite) {
        if (cargandoDocumento) {
            errorCorreccion = "Espera a que termine de guardarse el documento."
            return
        }
        if (documentoCorregido.isEmpty()) {
            errorCorreccion = "Adjunta el documento corregido antes de reenviar."
            return
        }
        val archivo = archivoAdjunto ?: return
        val texto = comentario.trim()
        realizarAccion(tramite.id, "La corrección fue enviada al notario.") {
            TramiteRepository.reenviar(tramite.id, archivo, texto)
            archivoAdjunto = null
            documentoCorregido = ""
            comentario = ""
        }
    }

    fun prepararFoto(): Uri? = try {
        archivoRepository.prepararFoto()
    } catch (_: Exception) {
        errorCorreccion = "No se pudo preparar la cámara. Inténtalo nuevamente."
        null
    }

    fun mostrarErrorCamara() { errorCorreccion = "No se pudo abrir la cámara en este dispositivo." }

    fun descargarDocumento(tramite: Tramite) {
        if (descargando || procesando) return
        descargando = true
        viewModelScope.launch {
            try {
                val documento = TramiteRepository.obtenerDocumento(tramite.id)
                tipoDocumento = documento.tipo
                uriDocumento = archivoRepository.obtenerUri(documento.ruta)
                if (uriDocumento == null) mostrarErrorDocumento()
            } catch (error: CancellationException) {
                throw error
            } catch (error: retrofit2.HttpException) {
                mensaje = if (error.code() == 404) "Este trámite no tiene un documento adjunto en el servidor." else mensajeDeError(error)
            } catch (error: Exception) {
                mensaje = mensajeDeError(error)
            } finally { descargando = false }
        }
    }

    fun documentoAbierto() { uriDocumento = null }

    fun mostrarErrorDocumento() {
        mensaje = "No se pudo abrir el documento. Comprueba que haya una aplicación compatible instalada."
    }

    fun cerrarMensaje() {
        mensaje = ""
    }
}
