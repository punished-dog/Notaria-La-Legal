package com.duoc.notarialalegal.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duoc.notarialalegal.viewmodel.LoginViewModel
import com.duoc.notarialalegal.viewmodel.RegisterViewModel

// Rutas de la app
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
}

@Composable
fun Navegacion(loginViewModel: LoginViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onRegister = { navController.navigate(Rutas.REGISTRO) },
                onLoginSuccess = {
                    // Se quita el login de la pila para que "atrás" no vuelva a él
                    navController.navigate(Rutas.INICIO) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.REGISTRO) {
            // viewModel() crea un ViewModel nuevo cada vez que se entra a la pantalla
            val registerViewModel: RegisterViewModel = viewModel()
            RegisterScreen(registerViewModel, onBack = { volver(navController) })
        }

        composable(Rutas.INICIO) {
            val usuario = loginViewModel.usuario
            if (usuario != null) {
                HomeScreen(usuario, onLogout = {
                    loginViewModel.logout()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                    }
                })
            }
        }
    }
}

// Evita que un doble toque en "Volver" saque al usuario de la app
private fun volver(navController: NavHostController) {
    if (navController.previousBackStackEntry != null) {
        navController.popBackStack()
    }
}
