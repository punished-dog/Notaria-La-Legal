package com.duoc.notarialalegal.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notarialalegal.model.AppDatabase
import com.duoc.notarialalegal.model.Usuario
import com.duoc.notarialalegal.model.Validaciones
import com.duoc.notarialalegal.repository.SesionRepository
import com.duoc.notarialalegal.repository.UsuarioRepository
import kotlinx.coroutines.launch

// Este ViewModel se crea en MainActivity, por eso también guarda la sesión
// (el usuario que ingresó) mientras la app está abierta.
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val sesionRepository = SesionRepository(AppDatabase.obtenerBaseDatos(application).sesionDao())

    // Estos valores se muestran en la pantalla. Solo el ViewModel los modifica.
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var emailError by mutableStateOf("")
        private set
    var passwordError by mutableStateOf("")
        private set
    var loginError by mutableStateOf("")
        private set
    var revisandoSesion by mutableStateOf(true)
        private set
    var usuario by mutableStateOf<Usuario?>(null)
        private set

    init {
        // Si quedó una sesión guardada en Room, el usuario entra directo
        viewModelScope.launch {
            usuario = sesionRepository.obtenerSesion()
            revisandoSesion = false
        }
    }

    fun updateEmail(newEmail: String) {
        email = newEmail
        emailError = ""
        loginError = ""
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        passwordError = ""
        loginError = ""
    }

    fun login() {
        email = email.trim()
        emailError = Validaciones.validarCorreo(email)
        passwordError = Validaciones.validarContrasena(password)
        loginError = ""

        // Si hay errores, no se intenta iniciar sesión.
        if (emailError.isNotEmpty() || passwordError.isNotEmpty()) {
            return
        }

        val encontrado = UsuarioRepository.iniciarSesion(email, password)
        if (encontrado == null) {
            loginError = "Correo o contraseña incorrectos."
            return
        }

        viewModelScope.launch {
            sesionRepository.guardarSesion(encontrado)
            password = ""
            usuario = encontrado
        }
    }

    fun logout() {
        usuario = null
        email = ""
        password = ""
        emailError = ""
        passwordError = ""
        loginError = ""
        viewModelScope.launch {
            sesionRepository.cerrarSesion()
        }
    }
}
