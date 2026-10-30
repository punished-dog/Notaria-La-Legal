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

class LoginScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun esperarTexto(texto: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // La sesión queda guardada en Room, por eso cada prueba que ingresa
    // debe cerrar sesión al final para no afectar a la siguiente.
    private fun cerrarSesion() {
        composeRule.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        composeRule.onNodeWithText("Sí, salir").performClick()
        esperarTexto("Iniciar sesión")
    }

    // Primero se espera a que termine la revisión de la sesión guardada
    @Before
    fun esperarLogin() {
        esperarTexto("Ingresar")
    }

    @Test
    fun formularioVacioMuestraErrores() {
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        composeRule.onNodeWithText("Ingresa tu correo electrónico.").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresa tu contraseña.").assertIsDisplayed()
    }

    @Test
    fun contrasenaIncorrectaNoInicia() {
        composeRule.onNodeWithText("Correo electrónico").performTextInput("demo@lalegal.test")
        composeRule.onNodeWithText("Contraseña").performTextInput("incorrecta")
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        esperarTexto("Correo o contraseña incorrectos.")
    }

    @Test
    fun clienteDePruebaIngresaYCierraSesion() {
        composeRule.onNodeWithText("Correo electrónico").performTextInput("demo@lalegal.test")
        composeRule.onNodeWithText("Contraseña").performTextInput("Demo1234")
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        esperarTexto("Mis trámites")
        cerrarSesion()
    }

    @Test
    fun formularioDeTramiteValidaCampos() {
        composeRule.onNodeWithText("Correo electrónico").performTextInput("demo@lalegal.test")
        composeRule.onNodeWithText("Contraseña").performTextInput("Demo1234")
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        esperarTexto("Nuevo trámite")
        composeRule.onNodeWithText("Nuevo trámite").performClick()
        composeRule.onNodeWithText("Revisar y firmar").performScrollTo().performClick()
        composeRule.onNodeWithText("Selecciona un tipo de trámite.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("← Volver").performClick()
        cerrarSesion()
    }
}
