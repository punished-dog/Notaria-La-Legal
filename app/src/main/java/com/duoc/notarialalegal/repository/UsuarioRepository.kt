package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Usuario

// Los usuarios se guardan en memoria.
// Las cuentas creadas en el registro duran mientras la app está abierta.
object UsuarioRepository {
    private val usuarios = mutableListOf(
        Usuario("Juan Doe", "demo@lalegal.test", "Demo1234", Roles.CLIENTE),
        Usuario("Ricardo Muñoz", "notario@lalegal.test", "Notario123", Roles.NOTARIO)
    )

    fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        return usuarios.find { it.correo == correo.lowercase() && it.contrasena == contrasena }
    }

    // Devuelve false si ya existe una cuenta con ese correo
    fun registrar(nombre: String, correo: String, contrasena: String): Boolean {
        val correoNormalizado = correo.lowercase()
        if (usuarios.any { it.correo == correoNormalizado }) {
            return false
        }
        usuarios.add(Usuario(nombre, correoNormalizado, contrasena))
        return true
    }
}
