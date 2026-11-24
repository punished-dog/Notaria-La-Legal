package com.duoc.notarialalegal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.network.mensajeDeError
import com.duoc.notarialalegal.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
    var registrando by mutableStateOf(false)
        private set
    fun updateName(value: String) { name = value; error = "" }
    fun updateEmail(value: String) { email = value; error = "" }
    fun updatePassword(value: String) { password = value; error = "" }
    fun updateConfirmPassword(value: String) { confirmPassword = value; error = "" }
    fun register() {
        if (registrando) return
        name = name.trim(); email = email.trim()
        error = Validaciones.validarNombre(name)
        if (error.isEmpty()) error = Validaciones.validarCorreo(email)
        if (error.isEmpty()) error = Validaciones.validarContrasena(password)
        if (error.isEmpty()) error = Validaciones.validarConfirmacion(password, confirmPassword)
        if (error.isNotEmpty()) return
        val nombre = name; val correo = email; val clave = password
        registrando = true
        viewModelScope.launch {
            try {
                UsuarioRepository.registrar(nombre, correo, clave)
                password = ""; confirmPassword = ""
                registrationComplete = true
            } catch (error: CancellationException) { throw error
            } catch (ex: Exception) { error = mensajeDeError(ex)
            } finally { registrando = false }
        }
    }
}
