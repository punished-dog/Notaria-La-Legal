package com.duoc.notarialalegal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.duoc.notarialalegal.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun esperarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // Primero se espera a que termine la revisión de la sesión guardada
    @Before
    fun esperarLogin() {
        esperarTexto("Ingresar")
    }

    @Test
    fun registroVacioMuestraError() {
        composeRule.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        composeRule.onNodeWithText("Registrarme").performScrollTo().performClick()
        composeRule.onNodeWithText("Ingresa un nombre de al menos 3 caracteres.").assertIsDisplayed()
    }

    @Test
    fun registroGuardaUsuarioYPermiteIngresar() {
        // Correo distinto en cada ejecución para no repetir una cuenta ya creada
        val correo = "usuario${System.currentTimeMillis()}@ejemplo.test"

        composeRule.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        composeRule.onNodeWithText("Nombre completo").performTextInput("Usuario Demo")
        composeRule.onNodeWithText("Correo electrónico").performTextInput(correo)
        composeRule.onNodeWithText("Contraseña").performScrollTo().performTextInput("Prueba123")
        composeRule.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput("Prueba123")
        composeRule.onNodeWithText("Registrarme").performScrollTo().performClick()
        esperarTexto("Cuenta creada")
        composeRule.onNodeWithText("Ir al login").performClick()

        composeRule.onNodeWithText("Correo electrónico").performTextInput(correo)
        composeRule.onNodeWithText("Contraseña").performTextInput("Prueba123")
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        esperarTexto("Mis trámites")

        // Se cierra la sesión para que la siguiente prueba parta en el login
        composeRule.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        composeRule.onNodeWithText("Sí, salir").performClick()
        esperarTexto("Iniciar sesión")
    }
}
