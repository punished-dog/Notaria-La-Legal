package com.duoc.notarialalegal.network

import com.duoc.notarialalegal.repository.SesionActual
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteXano {
    const val URL_BASE = "https://x8ki-letl-twmt.n7.xano.io/"
    val api: XanoApi by lazy { crear(URL_BASE) { SesionActual.token } }

    // El proveedor permite usar una sesión nueva sin reconstruir Retrofit.
    fun crear(url: String, obtenerToken: () -> String = { "" }): XanoApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .callTimeout(35, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val solicitud = cadena.request().newBuilder()
                val token = obtenerToken()
                val ruta = cadena.request().url.encodedPath
                if (token.isNotBlank() && !ruta.endsWith("/auth/login") && !ruta.endsWith("/auth/signup")) {
                    solicitud.header("Authorization", "Bearer $token")
                }
                cadena.proceed(solicitud.build())
            }.build()
        return Retrofit.Builder().baseUrl(url).client(cliente)
            .addConverterFactory(GsonConverterFactory.create()).build().create(XanoApi::class.java)
    }
}
