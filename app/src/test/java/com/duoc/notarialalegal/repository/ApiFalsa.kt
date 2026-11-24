package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.remote.ApiService
import com.duoc.notarialalegal.remote.HistorialApi
import com.duoc.notarialalegal.remote.RespuestaToken
import com.duoc.notarialalegal.remote.SolicitudLogin
import com.duoc.notarialalegal.remote.SolicitudRegistro
import com.duoc.notarialalegal.remote.TramiteApi
import com.duoc.notarialalegal.remote.UsuarioApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

// Reemplaza a Xano en las pruebas: guarda los datos en listas
// para revisar qué enviaron los repositorios, sin usar internet.
class ApiFalsa : ApiService {
    val tramites = mutableListOf<TramiteApi>()
    val historial = mutableListOf<HistorialApi>()
    val usuarios = mutableListOf<UsuarioApi>()
    val contrasenas = mutableMapOf<String, String>()
    private var siguienteId = 1

    override suspend fun registrar(datos: SolicitudRegistro): Response<RespuestaToken> {
        if (usuarios.any { it.email == datos.email }) {
            return Response.error(403, cuerpoError())
        }
        usuarios.add(UsuarioApi(siguienteId++, datos.name, datos.email, null))
        contrasenas[datos.email] = datos.password
        return Response.success(RespuestaToken("token-${datos.email}"))
    }

    override suspend fun iniciarSesion(datos: SolicitudLogin): Response<RespuestaToken> {
        if (contrasenas[datos.email] != datos.password) {
            return Response.error(403, cuerpoError())
        }
        return Response.success(RespuestaToken("token-${datos.email}"))
    }

    override suspend fun obtenerUsuarioActual(token: String): Response<UsuarioApi> {
        val correo = token.removePrefix("Bearer token-")
        val usuario = usuarios.find { it.email == correo } ?: return Response.error(401, cuerpoError())
        return Response.success(usuario)
    }

    override suspend fun obtenerTramites(): List<TramiteApi> {
        return tramites.toList()
    }

    override suspend fun obtenerTramite(id: Int): TramiteApi {
        return tramites.first { it.id == id }
    }

    override suspend fun crearTramite(tramite: TramiteApi): TramiteApi {
        val guardado = tramite.copy(id = siguienteId++)
        tramites.add(guardado)
        return guardado
    }

    override suspend fun actualizarTramite(id: Int, tramite: TramiteApi): TramiteApi {
        val posicion = tramites.indexOfFirst { it.id == id }
        tramites[posicion] = tramite.copy(id = id)
        return tramites[posicion]
    }

    override suspend fun eliminarTramite(id: Int): Response<Unit> {
        val eliminado = tramites.removeIf { it.id == id }
        return if (eliminado) Response.success(Unit) else Response.error(404, cuerpoError())
    }

    override suspend fun obtenerHistorial(): List<HistorialApi> {
        return historial.toList()
    }

    override suspend fun registrarHistorial(registro: HistorialApi): HistorialApi {
        val guardado = registro.copy(id = siguienteId++)
        historial.add(guardado)
        return guardado
    }

    private fun cuerpoError() = "{}".toResponseBody("application/json".toMediaType())
}
