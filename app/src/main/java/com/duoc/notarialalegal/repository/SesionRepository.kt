package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.Sesion
import com.duoc.notarialalegal.model.SesionDao
import com.duoc.notarialalegal.model.Usuario

// Guarda la sesión con Room para que el usuario no tenga que volver a
// ingresar cada vez que abre la app. La contraseña no se guarda.
class SesionRepository(private val dao: SesionDao) {

    suspend fun guardarSesion(usuario: Usuario) {
        dao.guardar(Sesion(nombre = usuario.nombre, correo = usuario.correo, rol = usuario.rol))
    }

    suspend fun obtenerSesion(): Usuario? {
        val sesion = dao.obtener() ?: return null
        return Usuario(nombre = sesion.nombre, correo = sesion.correo, contrasena = "", rol = sesion.rol)
    }

    suspend fun cerrarSesion() {
        dao.borrar()
    }
}
