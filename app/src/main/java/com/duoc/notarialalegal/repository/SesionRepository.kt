package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.Sesion
import com.duoc.notarialalegal.model.SesionDao
import com.duoc.notarialalegal.model.Usuario

class SesionRepository(private val dao: SesionDao) {
    suspend fun guardarSesion(usuario: Usuario, token: String) {
        require(token.isNotBlank()) { "El servidor no devolvió una sesión válida." }
        dao.guardar(Sesion(nombre = usuario.nombre, correo = usuario.correo, rol = usuario.rol,
            usuarioId = usuario.id, tokenCifrado = TokenSeguro.cifrar(token)))
        SesionActual.establecer(usuario, token)
    }
    suspend fun obtenerSesion(): Usuario? {
        val sesion = dao.obtener() ?: return null
        if (sesion.tokenCifrado.isBlank()) { dao.borrar(); return null }
        val token = try { TokenSeguro.descifrar(sesion.tokenCifrado) } catch (_: Exception) {
            dao.borrar()
            return null
        }
        val usuario = Usuario(nombre = sesion.nombre, correo = sesion.correo, rol = sesion.rol, id = sesion.usuarioId)
        SesionActual.establecer(usuario, token)
        return usuario
    }
    suspend fun cerrarSesion() {
        SesionActual.limpiar()
        TramiteRepository.limpiar()
        dao.borrar()
    }
}
