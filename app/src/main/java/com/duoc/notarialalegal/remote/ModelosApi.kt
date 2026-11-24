package com.duoc.notarialalegal.remote

import com.duoc.notarialalegal.model.Historial
import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.google.gson.annotations.SerializedName

// Datos que se envían y se reciben desde la API de Xano.
// Los campos de texto pueden venir nulos desde la API, por eso se usan tipos con "?".

data class SolicitudRegistro(
    val name: String,
    val email: String,
    val password: String
)

data class SolicitudLogin(
    val email: String,
    val password: String
)

data class RespuestaToken(
    val authToken: String?
)

data class UsuarioApi(
    val id: Int,
    val name: String?,
    val email: String?,
    val rol: String?
)

data class TramiteApi(
    // Al crear un trámite el id va nulo y Xano lo asigna
    val id: Int? = null,
    val tipo: String?,
    val documento: String?,
    @SerializedName("tipo_firma") val tipoFirma: String?,
    val antecedentes: String?,
    val estado: String?,
    val observacion: String?,
    @SerializedName("correo_cliente") val correoCliente: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?,
    @SerializedName("ruta_documento") val rutaDocumento: String?,
    @SerializedName("tipo_documento") val tipoDocumento: String?
)

data class HistorialApi(
    val id: Int? = null,
    @SerializedName("tramite_id") val tramiteId: Int,
    val estado: String?,
    val detalle: String?,
    val responsable: String?,
    val fecha: String?
)

// Conversiones entre los datos de la API y los modelos que usa la app

fun UsuarioApi.aModelo() = Usuario(
    nombre = name.orEmpty(),
    correo = email.orEmpty(),
    rol = if (rol.isNullOrBlank()) Roles.CLIENTE else rol
)

fun TramiteApi.aModelo() = Tramite(
    id = id ?: 0,
    tipo = tipo.orEmpty(),
    documento = documento.orEmpty(),
    tipoFirma = tipoFirma.orEmpty(),
    antecedentes = antecedentes.orEmpty(),
    estado = estado.orEmpty(),
    observacion = observacion.orEmpty(),
    correoCliente = correoCliente.orEmpty(),
    fechaCreacion = fechaCreacion.orEmpty(),
    rutaDocumento = rutaDocumento.orEmpty(),
    tipoDocumento = tipoDocumento ?: "application/octet-stream"
)

fun Tramite.aApi() = TramiteApi(
    id = if (id == 0) null else id,
    tipo = tipo,
    documento = documento,
    tipoFirma = tipoFirma,
    antecedentes = antecedentes,
    estado = estado,
    observacion = observacion,
    correoCliente = correoCliente,
    fechaCreacion = fechaCreacion,
    rutaDocumento = rutaDocumento,
    tipoDocumento = tipoDocumento
)

fun HistorialApi.aModelo() = Historial(
    tramiteId = tramiteId,
    estado = estado.orEmpty(),
    detalle = detalle.orEmpty(),
    responsable = responsable.orEmpty(),
    fecha = fecha.orEmpty()
)
