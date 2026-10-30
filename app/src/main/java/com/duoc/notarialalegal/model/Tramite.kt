package com.duoc.notarialalegal.model

data class Tramite(
    val id: Int = 0,
    val tipo: String,
    // Nombre del archivo adjuntado o del formato predefinido elegido
    val documento: String,
    val tipoFirma: String,
    val antecedentes: String,
    val estado: String,
    val observacion: String = "",
    val correoCliente: String,
    val fechaCreacion: String = "",
    val rutaDocumento: String = "",
    val tipoDocumento: String = "application/octet-stream"
) {
    // Código que se muestra al usuario, por ejemplo TR-007
    fun codigo(): String = "TR-" + id.toString().padStart(3, '0')
}
