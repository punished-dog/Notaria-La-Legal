package com.duoc.notarialalegal.repository

import android.content.Context
import com.duoc.notarialalegal.model.DocumentoAdjunto
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.network.ClienteXano
import com.duoc.notarialalegal.network.CorreccionSolicitud
import com.duoc.notarialalegal.network.ObservacionSolicitud
import com.duoc.notarialalegal.network.TramiteRespuesta
import com.duoc.notarialalegal.network.TramiteSolicitud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Xano es la fuente de los datos. Los StateFlow solo conservan lo mostrado en pantalla.
object TramiteRepository {
    private val lista = MutableStateFlow<List<Tramite>>(emptyList())
    private val movimientos = MutableStateFlow<Map<Int, List<Historial>>>(emptyMap())
    private var archivos: ArchivoRepository? = null
    val tramites = lista.asStateFlow()

    fun inicializar(context: Context) { archivos = ArchivoRepository(context.applicationContext) }
    fun obtenerPorId(id: Int) = lista.map { it.find { tramite -> tramite.id == id } }
    fun obtenerHistorial(id: Int) = movimientos.map { it[id].orEmpty() }
    fun limpiar() {
        lista.value = emptyList()
        movimientos.value = emptyMap()
    }
    // Una respuesta de la cuenta anterior nunca debe llenar la pantalla de una nueva sesión.
    private fun comprobarSesion(token: String) {
        if (token != SesionActual.token) throw CancellationException("La sesión cambió")
    }
    private fun convertir(respuesta: TramiteRespuesta): Tramite {
        return respuesta.aTramite(SesionActual.usuario)
    }
    private fun actualizar(tramite: Tramite) {
        lista.value = (listOf(tramite) + lista.value.filter { it.id != tramite.id }).sortedByDescending { it.id }
    }
    suspend fun cargarLista() {
        val token = SesionActual.token
        val respuesta = ClienteXano.api.listarTramites()
        comprobarSesion(token)
        lista.value = respuesta.map { convertir(it) }
    }
    suspend fun cargarDetalle(id: Int) {
        val token = SesionActual.token
        val respuesta = ClienteXano.api.detalle(id)
        comprobarSesion(token)
        actualizar(convertir(respuesta))
    }
    suspend fun cargarHistorial(id: Int) {
        val token = SesionActual.token
        val respuesta = ClienteXano.api.historial(id)
        comprobarSesion(token)
        movimientos.value = movimientos.value + (id to respuesta.map { it.aHistorial() })
    }
    suspend fun crearTramite(tramite: Tramite): Tramite {
        val token = SesionActual.token
        val contenido = withContext(Dispatchers.IO) {
            requireNotNull(archivos).convertirBase64(tramite.rutaDocumento)
        }
        val respuesta = ClienteXano.api.crearTramite(TramiteSolicitud(
            tramite.tipo, tramite.documento, tramite.tipoFirma, tramite.antecedentes,
            contenido, tramite.tipoDocumento))
        comprobarSesion(token)
        val nuevo = convertir(respuesta)
        actualizar(nuevo)
        return nuevo
    }
    suspend fun obtenerDocumento(id: Int): DocumentoAdjunto {
        val token = SesionActual.token
        val respuesta = ClienteXano.api.documento(id)
        comprobarSesion(token)
        return withContext(Dispatchers.IO) { requireNotNull(archivos).guardarDescarga(respuesta) }
    }
    suspend fun aprobar(id: Int) {
        val token = SesionActual.token
        val respuesta = ClienteXano.api.aprobar(id)
        comprobarSesion(token)
        actualizar(convertir(respuesta))
    }
    suspend fun devolver(id: Int, observacion: String) {
        val token = SesionActual.token
        val respuesta = ClienteXano.api.devolver(id, ObservacionSolicitud(observacion))
        comprobarSesion(token)
        actualizar(convertir(respuesta))
    }
    suspend fun reenviar(id: Int, documento: DocumentoAdjunto, comentario: String) {
        val token = SesionActual.token
        val contenido = withContext(Dispatchers.IO) { requireNotNull(archivos).convertirBase64(documento.ruta) }
        val respuesta = ClienteXano.api.reenviar(id, CorreccionSolicitud(documento.nombre, comentario, contenido, documento.tipo))
        comprobarSesion(token)
        actualizar(convertir(respuesta))
    }
}
