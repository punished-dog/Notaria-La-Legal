package com.duoc.notarialalegal.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {
    @Test fun contrasenaDebeCumplirElMinimoDeXano() {
        assertEquals("La contraseña debe tener al menos 8 caracteres.", Validaciones.validarContrasena("test123"))
        assertEquals("", Validaciones.validarContrasena("test1234"))
    }
    @Test fun correoRechazaEspaciosYDominioIncompleto() {
        assertTrue(Validaciones.validarCorreo("persona @correo.cl").isNotEmpty())
        assertTrue(Validaciones.validarCorreo("persona@correo").isNotEmpty())
        assertEquals("", Validaciones.validarCorreo("persona@correo.cl"))
    }
}
