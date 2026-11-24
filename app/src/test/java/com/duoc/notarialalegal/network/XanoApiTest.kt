package com.duoc.notarialalegal.network

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

// Comprueban el contrato HTTP sin depender de Internet ni de las cuentas de Xano.
class XanoApiTest {
    private lateinit var servidor: MockWebServer
    private lateinit var api: XanoApi
    @Before fun preparar() {
        servidor = MockWebServer()
        servidor.start()
        api = ClienteXano.crear(servidor.url("/").toString()) { "token-de-prueba" }
    }
    @After fun cerrar() { servidor.shutdown() }
    private fun responder(json: String, codigo: Int = 200) {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(json))
    }
    @Test fun loginEnviaCredencialesSinTokenYRecuperaRol() = runBlocking {
        responder("""{"authToken":"sesion-nueva","user":{"id":7,"name":"Cliente ficticio","email":"cliente@example.test","rol":"Cliente"}}""")
        val respuesta = api.ingresar(LoginSolicitud("cliente@example.test", "ClaveFicticia123"))
        val solicitud = servidor.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", solicitud.method)
        assertEquals("/api:T9E8dGD9/auth/login", solicitud.path)
        assertNull(solicitud.getHeader("Authorization"))
        val cuerpo = JsonParser.parseString(solicitud.body.readUtf8()).asJsonObject
        assertEquals("cliente@example.test", cuerpo["email"].asString)
        assertEquals("ClaveFicticia123", cuerpo["password"].asString)
        assertEquals("sesion-nueva", respuesta.authToken)
        assertEquals(7, respuesta.user.aUsuario().id)
        assertEquals("Cliente", respuesta.user.aUsuario().rol)
    }
    @Test fun crearTramiteEnviaBearerSinPermitirSuplantarClienteOEstado() = runBlocking {
        responder("""{"id":3,"created_at":1795460000000,"cliente_id":7,"tipo":"Poder simple","documento":"prueba.pdf","tipo_firma":"Firma electrónica simple","antecedentes":"Antecedentes de prueba","estado":"Enviado al notario","observacion":"","documento_url":""}""")
        val creado = api.crearTramite(TramiteSolicitud("Poder simple", "prueba.pdf", "Firma electrónica simple", "Antecedentes de prueba"))
        val solicitud = servidor.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", solicitud.method)
        assertEquals("/api:notaria-la-legal/tramites", solicitud.path)
        assertEquals("Bearer token-de-prueba", solicitud.getHeader("Authorization"))
        val cuerpo = JsonParser.parseString(solicitud.body.readUtf8()).asJsonObject
        assertEquals("Firma electrónica simple", cuerpo["tipo_firma"].asString)
        assertFalse(cuerpo.has("cliente_id"))
        assertFalse(cuerpo.has("estado"))
        assertEquals(3, creado.id)
    }
    @Test fun historialUsaIdDelTramiteYMapeaMovimientos() = runBlocking {
        responder("""[{"tramite_id":3,"created_at":1795460000000,"estado":"Con observaciones","detalle":"Falta una página","responsable":"Notario ficticio"}]""")
        val historial = api.historial(3)
        val solicitud = servidor.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("GET", solicitud.method)
        assertEquals("/api:notaria-la-legal/tramites/3/historial", solicitud.path)
        assertEquals("Bearer token-de-prueba", solicitud.getHeader("Authorization"))
        assertEquals("Notario ficticio", historial.single().aHistorial().responsable)
        assertEquals(3, historial.single().aHistorial().tramiteId)
    }
    @Test fun documentoSeDescargaPorIdConAutenticacion() = runBlocking {
        responder("""{"nombre":"solicitud.pdf","tipo":"application/pdf","contenido":"JVBERi0xLjc="}""")
        val documento = api.documento(3)
        val solicitud = servidor.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api:notaria-la-legal/tramites/3/documento", solicitud.path)
        assertEquals("Bearer token-de-prueba", solicitud.getHeader("Authorization"))
        assertEquals("application/pdf", documento.tipo)
        assertEquals("JVBERi0xLjc=", documento.contenido)
    }
    @Test fun correccionEnviaElNuevoArchivoYElComentario() = runBlocking {
        responder("""{"id":3,"created_at":1795460000000,"cliente_id":7,"tipo":"Poder simple","documento":"correccion.pdf","tipo_firma":"Firma electrónica simple","antecedentes":"Antecedentes de prueba","estado":"Enviado al notario","observacion":""}""")
        api.reenviar(3, CorreccionSolicitud("correccion.pdf", "Página completada", "JVBERi0xLjc=", "application/pdf"))
        val solicitud = servidor.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", solicitud.method)
        assertEquals("/api:notaria-la-legal/tramites/3/reenviar", solicitud.path)
        val cuerpo = JsonParser.parseString(solicitud.body.readUtf8()).asJsonObject
        assertEquals("JVBERi0xLjc=", cuerpo["documento_base64"].asString)
        assertEquals("application/pdf", cuerpo["documento_mime"].asString)
        assertEquals("Página completada", cuerpo["comentario"].asString)
    }
    @Test fun limiteDelPlanGratuitoProduceMensajeParaReintentar() = runBlocking {
        responder("""{"message":"Too many requests"}""", 429)
        try {
            api.listarTramites()
            fail("Se esperaba un error HTTP")
        } catch (error: HttpException) {
            assertEquals("Se alcanzó el límite de solicitudes. Espera 20 segundos e inténtalo nuevamente.", mensajeDeError(error))
        }
    }
}
