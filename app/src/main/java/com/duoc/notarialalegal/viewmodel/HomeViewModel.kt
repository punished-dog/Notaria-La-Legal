package com.duoc.notarialalegal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.duoc.notarialalegal.model.Roles
import com.duoc.notarialalegal.model.Tramite
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.repository.TramiteRepository
import kotlinx.coroutines.flow.Flow

class HomeViewModel : ViewModel() {

    var mostrarCerrarSesion by mutableStateOf(false)
        private set

    // El notario ve todos los trámites; el cliente solo los suyos.
    fun obtenerTramites(usuario: Usuario): Flow<List<Tramite>> {
        return if (usuario.rol == Roles.NOTARIO) {
            TramiteRepository.obtenerTodos()
        } else {
            TramiteRepository.obtenerDeCliente(usuario.correo)
        }
    }

    fun abrirCerrarSesion() {
        mostrarCerrarSesion = true
    }

    fun cancelarCerrarSesion() {
        mostrarCerrarSesion = false
    }
}
