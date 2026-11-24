package com.duoc.notarialalegal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notarialalegal.network.mensajeDeError
import com.duoc.notarialalegal.repository.TramiteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    val tramites = TramiteRepository.tramites
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set
    var mostrarCerrarSesion by mutableStateOf(false)
        private set
    fun actualizar() {
        if (cargando) return
        cargando = true
        error = ""
        viewModelScope.launch {
            try { TramiteRepository.cargarLista()
            } catch (ex: CancellationException) { throw ex
            } catch (ex: Exception) { error = mensajeDeError(ex)
            } finally { cargando = false }
        }
    }
    fun abrirCerrarSesion() { mostrarCerrarSesion = true }
    fun cancelarCerrarSesion() { mostrarCerrarSesion = false }
}
