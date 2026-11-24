package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.remote.ApiService
import com.duoc.notarialalegal.remote.HistorialApi
import com.duoc.notarialalegal.remote.RetrofitInstance
import com.duoc.notarialalegal.remote.aApi
import com.duoc.notarialalegal.remote.aModelo
import com.duoc.notarialalegal.remote.llamarApi
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Los trámites y su historial se guardan en Xano.
// Cada cambio de estado también agrega un registro al historial (trazabilidad).
class TramiteRepository(private val api: ApiService = RetrofitInstance.api) {

    // Los más nuevos aparecen primero
    suspend fun obtenerTodos(): List<Tramite> {
        return llamarApi { api.obtenerTramites() }
            .map { it.aModelo() }
            .sortedByDescending { it.id }
    }

    suspend fun obtenerDeCliente(correo: String): List<Tramite> {
        return obtenerTodos().filter { it.correoCliente == correo }
    }

    suspend fun obtenerPorId(id: Int): Tramite {
        return llamarApi { api.obtenerTramite(id) }.aModelo()
    }

    // El cambio más reciente aparece primero
    suspend fun obtenerHistorial(tramiteId: Int): List<Historial> {
        return llamarApi { api.obtenerHistorial() }
            .filter { it.tramiteId == tramiteId }
            .sortedByDescending { it.id ?: 0 }
            .map { it.aModelo() }
    }

    // Guarda el trámite ya firmado y lo deja enviado al notario
    suspend fun crearTramite(tramite: Tramite, responsable: String): Tramite {
        val nuevoTramite = tramite.copy(estado = EstadoTramite.ENVIADO, fechaCreacion = fechaActual())
        val guardado = llamarApi { api.crearTramite(nuevoTramite.aApi()) }.aModelo()
        registrarCambio(guardado.id, EstadoTramite.ENVIADO, "Trámite creado y firmado (${tramite.tipoFirma}).", responsable)
        return guardado
    }

    suspend fun aprobar(tramite: Tramite, responsable: String) {
        actualizar(tramite.copy(estado = EstadoTramite.APROBADO, observacion = ""))
        registrarCambio(tramite.id, EstadoTramite.APROBADO, "El notario aprobó el trámite.", responsable)
    }

    suspend fun devolver(tramite: Tramite, observacion: String, responsable: String) {
        actualizar(tramite.copy(estado = EstadoTramite.OBSERVADO, observacion = observacion))
        registrarCambio(tramite.id, EstadoTramite.OBSERVADO, observacion, responsable)
    }

    // El cliente corrige el documento y lo vuelve a enviar
    suspend fun reenviar(tramite: Tramite, nuevoDocumento: String, comentario: String, responsable: String,
                         rutaDocumento: String, tipoDocumento: String) {
        actualizar(tramite.copy(estado = EstadoTramite.ENVIADO, documento = nuevoDocumento, observacion = "",
            rutaDocumento = rutaDocumento, tipoDocumento = tipoDocumento))
        var detalle = "Corrección enviada con el documento $nuevoDocumento."
        if (comentario.isNotBlank()) {
            detalle += " Comentario: ${comentario.trim()}"
        }
        registrarCambio(tramite.id, EstadoTramite.ENVIADO, detalle, responsable)
    }

    // El cliente puede retirar un trámite que todavía no está aprobado
    suspend fun eliminar(tramite: Tramite) {
        val respuesta = llamarApi { api.eliminarTramite(tramite.id) }
        if (!respuesta.isSuccessful) {
            throw IOException("No se pudo eliminar el trámite ${tramite.codigo()}")
        }
    }

    // El id va en la ruta, por eso no se envía dentro del cuerpo
    private suspend fun actualizar(tramite: Tramite) {
        llamarApi { api.actualizarTramite(tramite.id, tramite.aApi().copy(id = null)) }
    }

    private suspend fun registrarCambio(tramiteId: Int, estado: String, detalle: String, responsable: String) {
        val registro = HistorialApi(
            tramiteId = tramiteId,
            estado = estado,
            detalle = detalle,
            responsable = responsable,
            fecha = fechaActual()
        )
        llamarApi { api.registrarHistorial(registro) }
    }

    private fun fechaActual(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }
}
