package com.duoc.notarialalegal.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// Endpoints del grupo "notaria" creado en Xano
interface ApiService {

    // Usuarios
    @POST("auth/signup")
    suspend fun registrar(@Body datos: SolicitudRegistro): Response<RespuestaToken>

    @POST("auth/login")
    suspend fun iniciarSesion(@Body datos: SolicitudLogin): Response<RespuestaToken>

    @GET("auth/me")
    suspend fun obtenerUsuarioActual(@Header("Authorization") token: String): Response<UsuarioApi>

    // Trámites
    @GET("tramite")
    suspend fun obtenerTramites(): List<TramiteApi>

    @GET("tramite/{tramite_id}")
    suspend fun obtenerTramite(@Path("tramite_id") id: Int): TramiteApi

    @POST("tramite")
    suspend fun crearTramite(@Body tramite: TramiteApi): TramiteApi

    @PATCH("tramite/{tramite_id}")
    suspend fun actualizarTramite(@Path("tramite_id") id: Int, @Body tramite: TramiteApi): TramiteApi

    @DELETE("tramite/{tramite_id}")
    suspend fun eliminarTramite(@Path("tramite_id") id: Int): Response<Unit>

    // Historial de cambios de cada trámite
    @GET("historial")
    suspend fun obtenerHistorial(): List<HistorialApi>

    @POST("historial")
    suspend fun registrarHistorial(@Body registro: HistorialApi): HistorialApi
}
