package com.duoc.notarialalegal.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notarialalegal.model.DocumentoAdjunto
import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.model.Mensajes
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.ArchivoRepository
import com.duoc.notarialalegal.repository.TramiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class NuevoTramiteViewModel(application: Application) : AndroidViewModel(application) {
    private val archivoRepository = ArchivoRepository(application)
    private val tramiteRepository = TramiteRepository()

    private var archivoAdjunto: DocumentoAdjunto? = null
    // Archivo preparado para la cámara mientras el usuario toma la foto
    private var fotoPendiente: DocumentoAdjunto? = null
    var cargandoDocumento by mutableStateOf(false)
        private set

    // Datos del formulario
    var tipo by mutableStateOf("")
        private set
    var usarFormato by mutableStateOf(false)
        private set
    var nombreArchivo by mutableStateOf("")
        private set
    var tipoFirma by mutableStateOf("")
        private set
    var antecedentes by mutableStateOf("")
        private set

    // Mensajes de error de cada campo
    var errorTipo by mutableStateOf("")
        private set
    var errorDocumento by mutableStateOf("")
        private set
    var errorFirma by mutableStateOf("")
        private set
    var errorAntecedentes by mutableStateOf("")
        private set

    // Estado de los modales
    var mostrarConfirmacion by mutableStateOf(false)
        private set
    var firmando by mutableStateOf(false)
        private set
    var codigoCreado by mutableStateOf("")
        private set
    var errorEnvio by mutableStateOf("")
        private set

    fun seleccionarTipo(valor: String) {
        tipo = valor
        errorTipo = ""
    }

    fun cambiarOrigenDocumento(formatoPredefinido: Boolean) {
        usarFormato = formatoPredefinido
        errorDocumento = ""
    }

    // Recibe el archivo elegido en el selector de archivos del teléfono
    fun seleccionarArchivo(uri: Uri?) {
        if (uri == null || cargandoDocumento) return
        cargandoDocumento = true
        errorDocumento = ""
        viewModelScope.launch {
            try {
                val copia = withContext(Dispatchers.IO) { archivoRepository.guardarCopia(uri) }
                archivoAdjunto = copia
                nombreArchivo = copia.nombre
            } catch (error: IOException) {
                errorDocumento = "No se pudo copiar el documento. Inténtalo de nuevo."
            } catch (error: SecurityException) {
                errorDocumento = "No se puede acceder al archivo. Selecciónalo nuevamente."
            } finally {
                cargandoDocumento = false
            }
        }
    }

    // Prepara el archivo donde la cámara guardará la foto y devuelve su Uri
    fun prepararFoto(): Uri? {
        if (cargandoDocumento) return null
        return try {
            val foto = archivoRepository.crearArchivoFoto()
            fotoPendiente = foto
            archivoRepository.obtenerUri(foto.ruta)
        } catch (excepcion: IOException) {
            errorDocumento = "No se pudo preparar la cámara. Inténtalo de nuevo."
            null
        }
    }

    // Recibe el resultado de la cámara: si la foto se tomó, queda como documento adjunto
    fun recibirFoto(fotoTomada: Boolean) {
        val foto = fotoPendiente ?: return
        fotoPendiente = null
        if (fotoTomada) {
            archivoAdjunto = foto
            nombreArchivo = foto.nombre
            errorDocumento = ""
        } else {
            archivoRepository.eliminarArchivo(foto.ruta)
        }
    }

    fun camaraNoDisponible() {
        recibirFoto(false)
        errorDocumento = "No se encontró una aplicación de cámara en el teléfono."
    }

    fun seleccionarFirma(valor: String) {
        tipoFirma = valor
        errorFirma = ""
    }

    fun actualizarAntecedentes(valor: String) {
        antecedentes = valor
        errorAntecedentes = ""
    }

    fun nombreDocumento(): String {
        return if (usarFormato) "Formato predefinido: $tipo" else nombreArchivo
    }

    // Revisa todos los campos y, si están bien, abre el modal de firma
    fun revisarFormulario() {
        if (cargandoDocumento) {
            errorDocumento = "Espera a que termine de guardarse el documento."
            return
        }
        errorTipo = if (tipo.isEmpty()) "Selecciona un tipo de trámite." else ""
        errorDocumento = if (!usarFormato && nombreArchivo.isEmpty()) "Adjunta un documento." else ""
        errorFirma = if (tipoFirma.isEmpty()) "Selecciona el tipo de firma." else ""
        errorAntecedentes = Validaciones.validarTextoMinimo(antecedentes, "los antecedentes")

        val formularioValido = errorTipo.isEmpty() && errorDocumento.isEmpty() && errorFirma.isEmpty() &&
            errorAntecedentes.isEmpty()
        if (formularioValido) {
            mostrarConfirmacion = true
        }
    }

    fun cancelarConfirmacion() {
        if (!firmando) {
            mostrarConfirmacion = false
            errorEnvio = ""
        }
    }

    // Se muestra la firma en proceso durante un momento
    // y luego se envía el trámite a la API como enviado al notario.
    fun firmarYEnviar(usuario: Usuario) {
        if (firmando || cargandoDocumento) return
        firmando = true
        errorEnvio = ""
        viewModelScope.launch {
            delay(1500)
            val tramite = Tramite(
                tipo = tipo,
                documento = nombreDocumento(),
                tipoFirma = tipoFirma,
                antecedentes = antecedentes.trim(),
                estado = EstadoTramite.ENVIADO,
                correoCliente = usuario.correo,
                rutaDocumento = if (usarFormato) "" else archivoAdjunto?.ruta.orEmpty(),
                tipoDocumento = if (usarFormato) "application/octet-stream" else archivoAdjunto?.tipo ?: "application/octet-stream"
            )
            try {
                val guardado = tramiteRepository.crearTramite(tramite, usuario.nombre)
                mostrarConfirmacion = false
                codigoCreado = guardado.codigo()
            } catch (excepcion: IOException) {
                errorEnvio = Mensajes.SIN_CONEXION
            } finally {
                firmando = false
            }
        }
    }
}
