package com.duoc.notarialalegal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notarialalegal.model.Mensajes
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.UsuarioRepository
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterViewModel : ViewModel() {
    private val usuarioRepository = UsuarioRepository()

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var error by mutableStateOf("")
        private set
    var registrationComplete by mutableStateOf(false)
        private set
    var cargando by mutableStateOf(false)
        private set

    fun updateName(value: String) {
        name = value
        error = ""
    }

    fun updateEmail(value: String) {
        email = value
        error = ""
    }

    fun updatePassword(value: String) {
        password = value
        error = ""
    }

    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        error = ""
    }

    fun register() {
        name = name.trim()
        email = email.trim()

        // Se muestra el primer error que se encuentre
        error = Validaciones.validarNombre(name)
        if (error.isEmpty()) error = Validaciones.validarCorreo(email)
        if (error.isEmpty()) error = Validaciones.validarContrasena(password)
        if (error.isEmpty()) error = Validaciones.validarConfirmacion(password, confirmPassword)
        if (error.isNotEmpty() || cargando) {
            return
        }

        cargando = true
        viewModelScope.launch {
            try {
                if (usuarioRepository.registrar(name, email, password)) {
                    password = ""
                    confirmPassword = ""
                    registrationComplete = true
                } else {
                    error = "No se pudo crear la cuenta. Es posible que el correo ya esté registrado."
                }
            } catch (excepcion: IOException) {
                error = Mensajes.SIN_CONEXION
            } finally {
                cargando = false
            }
        }
    }
}
