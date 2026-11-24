package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.Roles
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UsuarioRepositoryTest {
    private lateinit var api: ApiFalsa
    private lateinit var repositorio: UsuarioRepository

    @Before
    fun preparar() {
        api = ApiFalsa()
        repositorio = UsuarioRepository(api)
    }

    @Test
    fun registroNuevoQuedaComoCliente() = runTest {
        assertTrue(repositorio.registrar("Juan Doe", "Demo@LaLegal.test", "Demo1234"))

        val usuario = repositorio.iniciarSesion("demo@lalegal.test", "Demo1234")
        assertEquals("Juan Doe", usuario?.nombre)
        assertEquals("demo@lalegal.test", usuario?.correo)
        assertEquals(Roles.CLIENTE, usuario?.rol)
    }

    @Test
    fun contrasenaIncorrectaNoIniciaSesion() = runTest {
        repositorio.registrar("Juan Doe", "demo@lalegal.test", "Demo1234")

        assertNull(repositorio.iniciarSesion("demo@lalegal.test", "Incorrecta1"))
    }
}
