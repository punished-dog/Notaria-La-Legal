package com.duoc.notarialalegal.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ValidacionesTest {

    @Test
    fun correoVacioMuestraError() {
        assertEquals("Ingresa tu correo electrónico.", Validaciones.validarCorreo(""))
    }

    @Test
    fun correoSinFormatoEsRechazado() {
        assertEquals("Ingresa un correo válido.", Validaciones.validarCorreo("correo-invalido"))
    }

    @Test
    fun correoValidoNoTieneError() {
        assertEquals("", Validaciones.validarCorreo("demo@lalegal.test"))
    }

    @Test
    fun contrasenaCortaEsRechazada() {
        assertEquals("La contraseña debe tener al menos 6 caracteres.", Validaciones.validarContrasena("123"))
    }

    @Test
    fun contrasenaVaciaMuestraError() {
        assertEquals("Ingresa tu contraseña.", Validaciones.validarContrasena(""))
    }

    @Test
    fun nombreCortoEsRechazado() {
        assertEquals("Ingresa un nombre de al menos 3 caracteres.", Validaciones.validarNombre("  a "))
    }

    @Test
    fun contrasenasDistintasSonRechazadas() {
        assertEquals("Las contraseñas no coinciden.", Validaciones.validarConfirmacion("Prueba123", "Otra123"))
        assertEquals("", Validaciones.validarConfirmacion("Prueba123", "Prueba123"))
    }

    @Test
    fun textoMinimoRequiereDiezCaracteres() {
        assertEquals("Escribe al menos 10 caracteres en los antecedentes.", Validaciones.validarTextoMinimo("corto", "los antecedentes"))
        assertEquals("", Validaciones.validarTextoMinimo("Declaración de prueba", "los antecedentes"))
    }

    @Test
    fun codigoDelTramiteTieneTresDigitos() {
        val tramite = Tramite(
            id = 7,
            tipo = "Poder simple",
            documento = "poder.pdf",
            tipoFirma = "Firma electrónica simple",
            antecedentes = "Poder de prueba",
            estado = EstadoTramite.ENVIADO,
            correoCliente = "demo@lalegal.test"
        )
        assertEquals("TR-007", tramite.codigo())
    }
}
