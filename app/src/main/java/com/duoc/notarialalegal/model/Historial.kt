package com.duoc.notarialalegal.model

// Cada cambio de estado de un trámite queda registrado aquí (trazabilidad).
data class Historial(
    val tramiteId: Int,
    val estado: String,
    val detalle: String,
    val responsable: String,
    val fecha: String
)
