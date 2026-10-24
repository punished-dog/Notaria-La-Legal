package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Tramite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Los trámites y su historial se mantienen en memoria con datos ficticios.
// Al cerrar la app vuelven a quedar solo los dos trámites de ejemplo.
object TramiteRepository {
    private val tramites = MutableStateFlow(
        listOf(
            Tramite(
                id = 2,
                tipo = "Declaración jurada",
                documento = "declaracion_ejemplo.pdf",
                tipoFirma = "Firma electrónica avanzada",
                antecedentes = "Declaración de domicilio ficticia para la demostración.",
                estado = EstadoTramite.OBSERVADO,
                observacion = "Adjunta una versión completa del documento de ejemplo.",
                correoCliente = "demo@lalegal.test",
                fechaCreacion = "26/10/2026 16:40"
            ),
            Tramite(
                id = 1,
                tipo = "Autorización de documento",
                documento = "Formato predefinido: Autorización de documento",
                tipoFirma = "Firma electrónica simple",
                antecedentes = "Documento de ejemplo para probar el flujo de revisión.",
                estado = EstadoTramite.ENVIADO,
                correoCliente = "demo@lalegal.test",
                fechaCreacion = "27/10/2026 10:15"
            )
        )
    )

    private val historial = MutableStateFlow(
        listOf(
            Historial(2, EstadoTramite.ENVIADO, "Trámite creado y firmado (Firma electrónica avanzada).", "Usuario de prueba", "26/10/2026 16:40"),
            Historial(1, EstadoTramite.ENVIADO, "Trámite creado y firmado (Firma electrónica simple).", "Usuario de prueba", "27/10/2026 10:15"),
            Historial(2, EstadoTramite.OBSERVADO, "Adjunta una versión completa del documento de ejemplo.", "Notaria de prueba", "27/10/2026 09:05")
        )
    )

    private var siguienteId = 3

    // Al usar Flow, las pantallas se actualizan solas cuando cambia la lista
    fun obtenerTodos(): Flow<List<Tramite>> = tramites

    fun obtenerDeCliente(correo: String): Flow<List<Tramite>> {
        return tramites.map { lista -> lista.filter { it.correoCliente == correo } }
    }

    fun obtenerPorId(id: Int): Flow<Tramite?> {
        return tramites.map { lista -> lista.find { it.id == id } }
    }

    // El más reciente aparece primero
    fun obtenerHistorial(tramiteId: Int): Flow<List<Historial>> {
        return historial.map { lista -> lista.filter { it.tramiteId == tramiteId }.reversed() }
    }

    // Guarda el trámite ya firmado y lo deja enviado al notario
    fun crearTramite(tramite: Tramite, responsable: String): Tramite {
        val nuevoTramite = tramite.copy(id = siguienteId, estado = EstadoTramite.ENVIADO, fechaCreacion = fechaActual())
        siguienteId++
        tramites.value = listOf(nuevoTramite) + tramites.value
        registrarCambio(nuevoTramite.id, EstadoTramite.ENVIADO, "Trámite creado y firmado (${tramite.tipoFirma}).", responsable)
        return nuevoTramite
    }

    fun aprobar(tramite: Tramite, responsable: String) {
        actualizar(tramite.copy(estado = EstadoTramite.APROBADO, observacion = ""))
        registrarCambio(tramite.id, EstadoTramite.APROBADO, "El notario aprobó el trámite.", responsable)
    }

    fun devolver(tramite: Tramite, observacion: String, responsable: String) {
        actualizar(tramite.copy(estado = EstadoTramite.OBSERVADO, observacion = observacion))
        registrarCambio(tramite.id, EstadoTramite.OBSERVADO, observacion, responsable)
    }

    // El cliente corrige el documento y lo vuelve a enviar
    fun reenviar(tramite: Tramite, nuevoDocumento: String, comentario: String, responsable: String) {
        actualizar(tramite.copy(estado = EstadoTramite.ENVIADO, documento = nuevoDocumento, observacion = ""))
        var detalle = "Corrección enviada con el documento $nuevoDocumento."
        if (comentario.isNotBlank()) {
            detalle += " Comentario: ${comentario.trim()}"
        }
        registrarCambio(tramite.id, EstadoTramite.ENVIADO, detalle, responsable)
    }

    // Reemplaza el trámite que tiene el mismo id
    private fun actualizar(tramiteActualizado: Tramite) {
        tramites.value = tramites.value.map { if (it.id == tramiteActualizado.id) tramiteActualizado else it }
    }

    private fun registrarCambio(tramiteId: Int, estado: String, detalle: String, responsable: String) {
        historial.value = historial.value + Historial(tramiteId, estado, detalle, responsable, fechaActual())
    }

    private fun fechaActual(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }
}
