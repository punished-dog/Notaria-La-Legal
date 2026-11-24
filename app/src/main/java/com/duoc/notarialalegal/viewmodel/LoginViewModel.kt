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
import com.duoc.notarialalegal.network.mensajeDeError
import com.duoc.notarialalegal.repository.SesionActual
import com.duoc.notarialalegal.repository.SesionRepository
import com.duoc.notarialalegal.repository.TramiteRepository
import com.duoc.notarialalegal.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val sesiones = SesionRepository(AppDatabase.obtenerBaseDatos(application).sesionDao())
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
        viewModelScope.launch {
            try {
                val guardado = sesiones.obtenerSesion()
                if (guardado != null) {
                    try {
                        val actual = UsuarioRepository.obtenerUsuarioActual()
                        sesiones.guardarSesion(actual, SesionActual.token)
                        usuario = actual
                    } catch (error: HttpException) {
                        if (error.code() == 401 || error.code() == 403) sesiones.cerrarSesion()
                        loginError = mensajeDeError(error)
                    } catch (error: java.io.IOException) {
                        // Se permite entrar y reintentar la carga, sin inventar datos del servidor.
                        usuario = guardado
                    }
                }
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) { loginError = mensajeDeError(error)
            } finally { revisandoSesion = false }
        }
    }
    fun updateEmail(valor: String) { email = valor; emailError = ""; loginError = "" }
    fun updatePassword(valor: String) { password = valor; passwordError = ""; loginError = "" }
    fun login() {
        if (revisandoSesion) return
        email = email.trim()
        emailError = Validaciones.validarCorreo(email)
        passwordError = Validaciones.validarContrasena(password)
        if (emailError.isNotEmpty() || passwordError.isNotEmpty()) return
        val correo = email
        val clave = password
        revisandoSesion = true
        loginError = ""
        viewModelScope.launch {
            try {
                val acceso = UsuarioRepository.iniciarSesion(correo, clave)
                val actual = acceso.user.aUsuario()
                TramiteRepository.limpiar()
                sesiones.guardarSesion(actual, acceso.authToken)
                password = ""
                usuario = actual
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) { loginError = mensajeDeError(error)
            } finally { revisandoSesion = false }
        }
    }
    fun logout() {
        if (revisandoSesion) return
        revisandoSesion = true
        viewModelScope.launch {
            try {
                sesiones.cerrarSesion()
                usuario = null
                email = ""; password = ""; emailError = ""; passwordError = ""; loginError = ""
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) { loginError = mensajeDeError(error)
            } finally { revisandoSesion = false }
        }
    }
}
