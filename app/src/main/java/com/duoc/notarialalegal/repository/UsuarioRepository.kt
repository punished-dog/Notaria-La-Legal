package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.network.ClienteXano
import com.duoc.notarialalegal.network.LoginSolicitud
import com.duoc.notarialalegal.network.RegistroSolicitud

object UsuarioRepository {
    suspend fun iniciarSesion(correo: String, contrasena: String) =
        ClienteXano.api.ingresar(LoginSolicitud(correo.trim().lowercase(), contrasena))
    suspend fun registrar(nombre: String, correo: String, contrasena: String) {
        ClienteXano.api.registrar(RegistroSolicitud(nombre.trim(), correo.trim().lowercase(), contrasena))
    }
    suspend fun obtenerUsuarioActual() = ClienteXano.api.usuarioActual().aUsuario()
}
