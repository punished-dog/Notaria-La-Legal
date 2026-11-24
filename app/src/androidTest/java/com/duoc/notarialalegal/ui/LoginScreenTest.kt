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
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // Ejecutar desde el login, con la sesión cerrada.
    @Before
    fun esperarLogin() {
        composeRule.waitUntil(timeoutMillis = 40_000) {
            composeRule.onAllNodesWithText("Ingresar").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
            composeRule.onNodeWithText("Sí, salir").performClick()
            esperarTexto("Ingresar")
        }
    }

    @Test
    fun formularioVacioMuestraErrores() {
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        composeRule.onNodeWithText("Ingresa tu correo electrónico.").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresa tu contraseña.").assertIsDisplayed()
    }

    @Test
    fun correoInvalidoNoSeEnviaAlServidor() {
        composeRule.onNodeWithText("Correo electrónico").performTextInput("correo-incompleto")
        composeRule.onNodeWithText("Contraseña").performTextInput("test1234")
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        composeRule.onNodeWithText("Ingresa un correo válido.").assertIsDisplayed()
    }
}
