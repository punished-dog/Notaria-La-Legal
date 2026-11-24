package com.duoc.notarialalegal.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ValidacionesTest {

    @Test
    fun correoSinDominioEsInvalido() {
        assertEquals("Ingresa un correo válido.", Validaciones.validarCorreo("demo@lalegal"))
    }

    @Test
    fun correoCorrectoNoTieneError() {
        assertEquals("", Validaciones.validarCorreo("demo@lalegal.test"))
    }

    @Test
    fun contrasenaCortaMuestraError() {
        assertEquals("La contraseña debe tener al menos 8 caracteres.", Validaciones.validarContrasena("Abc123"))
    }

    @Test
    fun textoMinimoIndicaElCampo() {
        assertEquals("Escribe al menos 10 caracteres en los antecedentes.",
            Validaciones.validarTextoMinimo("corto", "los antecedentes"))
        assertEquals("", Validaciones.validarTextoMinimo("Contrato de arriendo", "los antecedentes"))
    }
}
