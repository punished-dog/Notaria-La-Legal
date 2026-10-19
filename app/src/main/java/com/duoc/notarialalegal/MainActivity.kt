package com.duoc.notarialalegal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.duoc.notarialalegal.ui.HomeScreen
import com.duoc.notarialalegal.ui.LoginScreen
import com.duoc.notarialalegal.ui.RegisterScreen
import com.duoc.notarialalegal.ui.theme.NotariaLaLegalTheme
import com.duoc.notarialalegal.viewmodel.LoginViewModel
import com.duoc.notarialalegal.viewmodel.RegisterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setContent {
            var showRegister by rememberSaveable { mutableStateOf(false) }

            NotariaLaLegalTheme {
                val usuario = loginViewModel.usuario
                if (usuario != null) {
                    HomeScreen(usuario, onLogout = { loginViewModel.logout() })
                } else if (showRegister) {
                    // Se crea un formulario nuevo cada vez que se abre el registro
                    val registerViewModel = remember { RegisterViewModel() }
                    RegisterScreen(registerViewModel, onBack = { showRegister = false })
                } else {
                    LoginScreen(loginViewModel, onRegister = { showRegister = true })
                }
            }
        }
    }
}
