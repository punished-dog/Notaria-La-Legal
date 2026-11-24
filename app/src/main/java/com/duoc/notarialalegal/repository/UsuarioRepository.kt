package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.remote.ApiService
import com.duoc.notarialalegal.remote.RetrofitInstance
import com.duoc.notarialalegal.remote.SolicitudLogin
import com.duoc.notarialalegal.remote.SolicitudRegistro
import com.duoc.notarialalegal.remote.aModelo
import com.duoc.notarialalegal.remote.llamarApi

// Las cuentas de usuario se guardan en Xano.
// El ApiService se recibe por parámetro para poder probar el repositorio sin internet.
class UsuarioRepository(private val api: ApiService = RetrofitInstance.api) {

    // Primero se obtiene el token con el correo y la contraseña,
    // y con ese token se piden los datos del usuario (nombre y rol).
    // Devuelve null si el correo o la contraseña no son correctos.
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val respuestaLogin = llamarApi { api.iniciarSesion(SolicitudLogin(correo.lowercase(), contrasena)) }
        val token = respuestaLogin.body()?.authToken
        if (!respuestaLogin.isSuccessful || token == null) {
            return null
        }
        val respuestaUsuario = llamarApi { api.obtenerUsuarioActual("Bearer $token") }
        return respuestaUsuario.body()?.aModelo()
    }

    // Devuelve false si Xano rechaza el registro, por ejemplo si el correo ya existe
    suspend fun registrar(nombre: String, correo: String, contrasena: String): Boolean {
        val respuesta = llamarApi { api.registrar(SolicitudRegistro(nombre, correo.lowercase(), contrasena)) }
        return respuesta.isSuccessful
    }
}
