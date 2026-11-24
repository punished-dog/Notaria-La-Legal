package com.duoc.notarialalegal.network

import com.google.gson.annotations.SerializedName
import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LoginSolicitud(val email: String, val password: String)
data class RegistroSolicitud(val name: String, val email: String, val password: String)
data class AccesoRespuesta(val authToken: String, val user: UsuarioRespuesta)
data class UsuarioRespuesta(val id: Int, val name: String, val email: String, val rol: String?) {
    fun aUsuario() = Usuario(nombre = name, correo = email, rol = rol ?: Roles.CLIENTE, id = id)
}
data class TramiteSolicitud(
    val tipo: String,
    val documento: String,
    @SerializedName("tipo_firma") val tipoFirma: String,
    val antecedentes: String,
    @SerializedName("documento_base64") val documentoBase64: String = "",
    @SerializedName("documento_mime") val documentoMime: String = "application/octet-stream"
)
data class ObservacionSolicitud(val observacion: String)
data class CorreccionSolicitud(
    val documento: String,
    val comentario: String,
    @SerializedName("documento_base64") val documentoBase64: String = "",
    @SerializedName("documento_mime") val documentoMime: String = "application/octet-stream"
)
data class TramiteRespuesta(
    val id: Int,
    @SerializedName("created_at") val fecha: Long,
    @SerializedName("cliente_id") val clienteId: Int,
    val tipo: String,
    val documento: String,
    @SerializedName("tipo_firma") val tipoFirma: String,
    val antecedentes: String,
    val estado: String,
    val observacion: String?
) {
    fun aTramite(usuario: Usuario?): Tramite = Tramite(
        id = id, tipo = tipo, documento = documento, tipoFirma = tipoFirma,
        antecedentes = antecedentes, estado = estado, observacion = observacion.orEmpty(),
        correoCliente = if (usuario?.id == clienteId) usuario.correo else "Usuario #$clienteId",
        fechaCreacion = formatearFecha(fecha)
    )
}
data class HistorialRespuesta(
    @SerializedName("tramite_id") val tramiteId: Int,
    @SerializedName("created_at") val fecha: Long,
    val estado: String,
    val detalle: String,
    val responsable: String
) {
    fun aHistorial() = Historial(tramiteId, estado, detalle, responsable, formatearFecha(fecha))
}
private fun formatearFecha(fecha: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(fecha))

data class DocumentoRespuesta(val nombre: String, val tipo: String, val contenido: String)
