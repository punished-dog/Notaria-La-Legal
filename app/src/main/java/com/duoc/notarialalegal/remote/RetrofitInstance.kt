package com.duoc.notarialalegal.remote

import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitInstance {
    // URL base del grupo de endpoints "notaria" en Xano (debe terminar en /)
    private const val URL_BASE = "https://x8ki-letl-twmt.n7.xano.io/api:notaria/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// Ejecuta una petición a la API. Si el servidor responde con un error,
// se transforma en IOException para que los ViewModels lo manejen igual que una falla de conexión.
suspend fun <T> llamarApi(peticion: suspend () -> T): T {
    try {
        return peticion()
    } catch (error: HttpException) {
        throw IOException("El servidor respondió con el código ${error.code()}", error)
    }
}
