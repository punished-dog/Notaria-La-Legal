package com.duoc.notarialalegal.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notarialalegal.model.DocumentoAdjunto
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Mensajes
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.ArchivoRepository
import com.duoc.notarialalegal.repository.TramiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class DetalleTramiteViewModel(application: Application) : AndroidViewModel(application) {
    private val archivoRepository = ArchivoRepository(application)
    private val tramiteRepository = TramiteRepository()

    // Datos que se cargan desde la API
    var tramite by mutableStateOf<Tramite?>(null)
        private set
    var historial by mutableStateOf<List<Historial>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var errorCarga by mutableStateOf("")
        private set

    // Es true mientras se envía una acción a la API (aprobar, devolver, reenviar o eliminar)
    var procesando by mutableStateOf(false)
        private set

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

    // Modal para confirmar la eliminación y aviso para volver al inicio
    var mostrarConfirmarEliminar by mutableStateOf(false)
        private set
    var tramiteEliminado by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf("")
        private set

    fun cargar(id: Int) {
        cargando = true
        errorCarga = ""
        viewModelScope.launch {
            try {
                tramite = tramiteRepository.obtenerPorId(id)
                historial = tramiteRepository.obtenerHistorial(id)
            } catch (excepcion: IOException) {
                errorCarga = Mensajes.SIN_CONEXION
            } finally {
                cargando = false
            }
        }
    }

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
            } catch (excepcion: IOException) {
                errorCorreccion = "No se pudo copiar el documento. Inténtalo de nuevo."
            } catch (excepcion: SecurityException) {
                errorCorreccion = "No se puede acceder al archivo. Selecciónalo nuevamente."
            } finally {
                cargandoDocumento = false
            }
        }
    }

    fun aprobar(tramite: Tramite, notario: Usuario) {
        ejecutarAccion(tramite.id, "El trámite ${tramite.codigo()} fue aprobado.") {
            tramiteRepository.aprobar(tramite, notario.nombre)
        }
    }

    fun devolver(tramite: Tramite, notario: Usuario) {
        errorObservacion = Validaciones.validarTextoMinimo(observacion, "la observación")
        if (errorObservacion.isNotEmpty()) {
            return
        }
        ejecutarAccion(tramite.id, "El trámite ${tramite.codigo()} fue devuelto al cliente con observaciones.") {
            tramiteRepository.devolver(tramite, observacion.trim(), notario.nombre)
            observacion = ""
        }
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
        ejecutarAccion(tramite.id, "La corrección del trámite ${tramite.codigo()} fue enviada al notario.") {
            tramiteRepository.reenviar(tramite, documentoCorregido, comentario, cliente.nombre,
                archivoAdjunto?.ruta.orEmpty(), archivoAdjunto?.tipo ?: "application/octet-stream")
            archivoAdjunto = null
            documentoCorregido = ""
            comentario = ""
        }
    }

    fun pedirConfirmarEliminar() {
        mostrarConfirmarEliminar = true
    }

    fun cancelarEliminar() {
        if (!procesando) {
            mostrarConfirmarEliminar = false
        }
    }

    fun eliminar(tramite: Tramite) {
        if (procesando) return
        procesando = true
        viewModelScope.launch {
            try {
                tramiteRepository.eliminar(tramite)
                tramiteEliminado = true
            } catch (excepcion: IOException) {
                mensaje = Mensajes.SIN_CONEXION
            } finally {
                procesando = false
                mostrarConfirmarEliminar = false
            }
        }
    }

    // Envía la acción a la API y, si resulta, vuelve a cargar el trámite para mostrar el nuevo estado
    private fun ejecutarAccion(tramiteId: Int, mensajeExito: String, accion: suspend () -> Unit) {
        if (procesando) return
        procesando = true
        viewModelScope.launch {
            try {
                accion()
                mensaje = mensajeExito
                tramite = tramiteRepository.obtenerPorId(tramiteId)
                historial = tramiteRepository.obtenerHistorial(tramiteId)
            } catch (excepcion: IOException) {
                mensaje = Mensajes.SIN_CONEXION
            } finally {
                procesando = false
            }
        }
    }

    fun obtenerUriDocumento(tramite: Tramite): Uri? {
        val uri = archivoRepository.obtenerUri(tramite.rutaDocumento)
        if (uri == null) {
            mensaje = "Este trámite usa un formato predefinido o su archivo no está guardado en este teléfono."
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
