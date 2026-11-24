package com.duoc.notarialalegal.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Los grupos de Xano comparten el mismo servidor y usan el token del usuario.
interface XanoApi {
    @POST("/api:T9E8dGD9/auth/signup")
    suspend fun registrar(@Body datos: RegistroSolicitud): AccesoRespuesta
    @POST("/api:T9E8dGD9/auth/login")
    suspend fun ingresar(@Body datos: LoginSolicitud): AccesoRespuesta
    @GET("/api:T9E8dGD9/auth/me")
    suspend fun usuarioActual(): UsuarioRespuesta
    @GET("/api:notaria-la-legal/tramites")
    suspend fun listarTramites(): List<TramiteRespuesta>
    @POST("/api:notaria-la-legal/tramites")
    suspend fun crearTramite(@Body datos: TramiteSolicitud): TramiteRespuesta
    @GET("/api:notaria-la-legal/tramites/{id}")
    suspend fun detalle(@Path("id") id: Int): TramiteRespuesta
    @GET("/api:notaria-la-legal/tramites/{id}/documento")
    suspend fun documento(@Path("id") id: Int): DocumentoRespuesta
    @GET("/api:notaria-la-legal/tramites/{id}/historial")
    suspend fun historial(@Path("id") id: Int): List<HistorialRespuesta>
    @POST("/api:notaria-la-legal/tramites/{id}/aprobar")
    suspend fun aprobar(@Path("id") id: Int): TramiteRespuesta
    @POST("/api:notaria-la-legal/tramites/{id}/devolver")
    suspend fun devolver(@Path("id") id: Int, @Body datos: ObservacionSolicitud): TramiteRespuesta
    @POST("/api:notaria-la-legal/tramites/{id}/reenviar")
    suspend fun reenviar(@Path("id") id: Int, @Body datos: CorreccionSolicitud): TramiteRespuesta
}
