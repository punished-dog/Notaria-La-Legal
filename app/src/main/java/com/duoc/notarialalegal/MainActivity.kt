package com.duoc.notarialalegal

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.duoc.notarialalegal.ui.Navegacion
import com.duoc.notarialalegal.ui.theme.NotariaLaLegalTheme
import com.duoc.notarialalegal.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras del sistema con íconos oscuros, porque el fondo de la app es claro
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        // El LoginViewModel se crea aquí porque guarda la sesión que usan todas las pantallas
        val loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setContent {
            NotariaLaLegalTheme {
                Navegacion(loginViewModel)
            }
        }
    }
}
