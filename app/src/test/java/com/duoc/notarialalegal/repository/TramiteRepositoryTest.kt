package com.duoc.notarialalegal.repository

import com.duoc.notarialalegal.model.EstadoTramite
import com.duoc.notarialalegal.model.Tramite
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TramiteRepositoryTest {
    private lateinit var api: ApiFalsa
    private lateinit var repositorio: TramiteRepository

    private val tramiteBase = Tramite(
        tipo = "Declaración jurada",
        documento = "declaracion.pdf",
        tipoFirma = "Firma electrónica simple",
        antecedentes = "Declaración de domicilio para el banco.",
        estado = "",
        correoCliente = "demo@lalegal.test"
    )

    @Before
    fun preparar() {
        api = ApiFalsa()
        repositorio = TramiteRepository(api)
    }

    @Test
    fun crearTramiteLoDejaEnviadoYRegistraHistorial() = runTest {
        val creado = repositorio.crearTramite(tramiteBase, "Juan Doe")

        assertEquals(EstadoTramite.ENVIADO, creado.estado)
        assertTrue(creado.fechaCreacion.isNotEmpty())
        val historial = repositorio.obtenerHistorial(creado.id)
        assertEquals(1, historial.size)
        assertEquals("Juan Doe", historial.first().responsable)
    }

    @Test
    fun clienteSoloVeSusTramites() = runTest {
        repositorio.crearTramite(tramiteBase, "Juan Doe")
        repositorio.crearTramite(tramiteBase.copy(correoCliente = "otro@lalegal.test"), "Otro cliente")

        val tramitesCliente = repositorio.obtenerDeCliente("demo@lalegal.test")
        assertEquals(1, tramitesCliente.size)
        assertEquals(2, repositorio.obtenerTodos().size)
    }

    @Test
    fun devolverGuardaLaObservacion() = runTest {
        val creado = repositorio.crearTramite(tramiteBase, "Juan Doe")

        repositorio.devolver(creado, "Falta la página con la firma.", "Ricardo Muñoz")

        val actualizado = repositorio.obtenerPorId(creado.id)
        assertEquals(EstadoTramite.OBSERVADO, actualizado.estado)
        assertEquals("Falta la página con la firma.", actualizado.observacion)
        assertEquals(EstadoTramite.OBSERVADO, repositorio.obtenerHistorial(creado.id).first().estado)
    }

    @Test
    fun eliminarQuitaElTramite() = runTest {
        val creado = repositorio.crearTramite(tramiteBase, "Juan Doe")

        repositorio.eliminar(creado)

        assertTrue(repositorio.obtenerTodos().isEmpty())
    }
}
