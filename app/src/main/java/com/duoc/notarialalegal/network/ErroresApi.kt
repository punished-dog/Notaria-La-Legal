package com.duoc.notarialalegal.network

import com.google.gson.JsonParser
import java.io.IOException
import retrofit2.HttpException

fun mensajeDeError(error: Exception): String {
    if (error is HttpException) {
        return when (error.code()) {
            401 -> "Tu sesión venció o las credenciales son incorrectas. Inicia sesión nuevamente."
            403 -> "Tu cuenta no tiene permiso para realizar esta acción."
            404 -> "No se encontró la información solicitada."
            429 -> "Se alcanzó el límite de solicitudes. Espera 20 segundos e inténtalo nuevamente."
            in 500..599 -> "El servidor no está disponible. Inténtalo más tarde."
            else -> {
                val texto = try {
                    JsonParser.parseString(error.response()?.errorBody()?.string().orEmpty())
                        .asJsonObject.get("message")?.asString
                } catch (_: Exception) { null }
                texto?.takeIf { it.isNotBlank() } ?: "Revisa los datos e inténtalo nuevamente."
            }
        }
    }
    return if (error is IOException) "No se pudo conectar. Revisa tu conexión a Internet y vuelve a intentarlo."
    else "No se pudo completar la operación. Inténtalo nuevamente."
}
