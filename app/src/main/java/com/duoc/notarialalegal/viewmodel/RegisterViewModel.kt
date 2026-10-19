package com.duoc.notarialalegal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.UsuarioRepository

class RegisterViewModel : ViewModel() {
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
        if (error.isNotEmpty()) {
            return
        }

        if (UsuarioRepository.registrar(name, email, password)) {
            password = ""
            confirmPassword = ""
            registrationComplete = true
        } else {
            error = "Ya existe una cuenta con ese correo."
        }
    }
}
