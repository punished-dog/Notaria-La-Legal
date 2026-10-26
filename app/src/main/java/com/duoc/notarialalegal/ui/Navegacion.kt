package com.duoc.notarialalegal.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.notarialalegal.viewmodel.DetalleTramiteViewModel
import com.duoc.notarialalegal.viewmodel.HomeViewModel
import com.duoc.notarialalegal.viewmodel.LoginViewModel
import com.duoc.notarialalegal.viewmodel.NuevoTramiteViewModel
import com.duoc.notarialalegal.viewmodel.RegisterViewModel

// Rutas de la app
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val NUEVO_TRAMITE = "nuevo_tramite"
    const val DETALLE = "detalle/{id}"

    fun detalle(id: Int) = "detalle/$id"
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
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    usuario = usuario,
                    onNuevoTramite = { navController.navigate(Rutas.NUEVO_TRAMITE) },
                    onVerTramite = { id -> navController.navigate(Rutas.detalle(id)) },
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.INICIO) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Rutas.NUEVO_TRAMITE) {
            val usuario = loginViewModel.usuario
            if (usuario != null) {
                val nuevoTramiteViewModel: NuevoTramiteViewModel = viewModel()
                NuevoTramiteScreen(nuevoTramiteViewModel, usuario, onTerminar = { volver(navController) })
            }
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val usuario = loginViewModel.usuario
            val tramiteId = entrada.arguments?.getInt("id") ?: 0
            if (usuario != null) {
                val detalleViewModel: DetalleTramiteViewModel = viewModel()
                DetalleTramiteScreen(detalleViewModel, tramiteId, usuario, onVolver = { volver(navController) })
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
