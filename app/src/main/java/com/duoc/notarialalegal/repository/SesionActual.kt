package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.Usuario

// Solo conserva la sesión activa en memoria; Retrofit consulta el token al enviar.
object SesionActual {
    @Volatile var token: String = ""
        private set
    @Volatile var usuario: Usuario? = null
        private set
    fun establecer(usuario: Usuario, token: String) {
        this.usuario = usuario
        this.token = token
    }
    fun limpiar() {
        token = ""
        usuario = null
    }
}
