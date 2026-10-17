package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Usuario

// Los usuarios se guardan en memoria con datos ficticios.
object UsuarioRepository {
    private val usuarios = mutableListOf(
        Usuario("Usuario de prueba", "demo@lalegal.test", "Demo1234", Roles.CLIENTE),
        Usuario("Notaria de prueba", "notario@lalegal.test", "Notario123", Roles.NOTARIO)
    )

    fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        return usuarios.find { it.correo == correo.lowercase() && it.contrasena == contrasena }
    }
}
