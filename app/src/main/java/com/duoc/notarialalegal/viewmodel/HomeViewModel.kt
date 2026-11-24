package com.duoc.notarialalegal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notarialalegal.model.Mensajes
import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.repository.TramiteRepository
import kotlinx.coroutines.launch
import java.io.IOException

class HomeViewModel : ViewModel() {
    private val tramiteRepository = TramiteRepository()

    var tramites by mutableStateOf<List<Tramite>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set
    var mostrarCerrarSesion by mutableStateOf(false)
        private set

    // El notario ve todos los trámites; el cliente solo los suyos.
    fun cargarTramites(usuario: Usuario) {
        cargando = true
        error = ""
        viewModelScope.launch {
            try {
                tramites = if (usuario.rol == Roles.NOTARIO) {
                    tramiteRepository.obtenerTodos()
                } else {
                    tramiteRepository.obtenerDeCliente(usuario.correo)
                }
            } catch (excepcion: IOException) {
                error = Mensajes.SIN_CONEXION
            } finally {
                cargando = false
            }
        }
    }

    fun abrirCerrarSesion() {
        mostrarCerrarSesion = true
    }

    fun cancelarCerrarSesion() {
        mostrarCerrarSesion = false
    }
}
