package com.duoc.notarialalegal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.duoc.notarialalegal.ui.HomeScreen
import com.duoc.notarialalegal.ui.LoginScreen
import com.duoc.notarialalegal.ui.theme.NotariaLaLegalTheme
import com.duoc.notarialalegal.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setContent {
            NotariaLaLegalTheme {
                // Si hay un usuario se muestra el inicio, si no el login
                val usuario = loginViewModel.usuario
                if (usuario != null) {
                    HomeScreen(usuario, onLogout = { loginViewModel.logout() })
                } else {
                    LoginScreen(loginViewModel)
                }
            }
        }
    }
}
