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
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // Primero se espera a que termine la revisión de la sesión guardada
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
    fun registroVacioMuestraError() {
        composeRule.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        composeRule.onNodeWithText("Registrarme").performScrollTo().performClick()
        composeRule.onNodeWithText("Ingresa un nombre de al menos 3 caracteres.").assertIsDisplayed()
    }

    @Test
    fun confirmacionDistintaImpideCrearLaCuenta() {
        composeRule.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        composeRule.onNodeWithText("Nombre completo").performTextInput("Usuario de prueba")
        composeRule.onNodeWithText("Correo electrónico").performTextInput("usuario@ejemplo.test")
        composeRule.onNodeWithText("Contraseña").performScrollTo().performTextInput("test1234")
        composeRule.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput("otra1234")
        composeRule.onNodeWithText("Registrarme").performScrollTo().performClick()
        composeRule.onNodeWithText("Las contraseñas no coinciden.").assertIsDisplayed()
    }
}
