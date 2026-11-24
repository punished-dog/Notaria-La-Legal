package com.duoc.notarialalegal.repository

import android.graphics.Bitmap
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.duoc.notarialalegal.network.DocumentoRespuesta
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ArchivoRepositoryTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivos = ArchivoRepository(contexto)

    @Test fun formatoPdfSePuedePrepararYRecuperarComoAdjunto() {
        val formato = archivos.crearFormato("Declaración jurada", "Cliente de prueba", "Antecedentes de la solicitud.")
        var descargado: File? = null
        try {
            val contenido = archivos.convertirBase64(formato.ruta)
            val documento = archivos.guardarDescarga(DocumentoRespuesta(formato.nombre, formato.tipo, contenido))
            descargado = File(documento.ruta)
            assertEquals("application/pdf", documento.tipo)
            assertArrayEquals(File(formato.ruta).readBytes(), descargado.readBytes())
            assertEquals("${contexto.packageName}.documentos", archivos.obtenerUri(documento.ruta)?.authority)
        } finally {
            File(formato.ruta).delete()
            descargado?.delete()
        }
    }

    @Test fun fotografiaSeGuardaYSePuedeEnviarSinPermisoDeAlmacenamiento() {
        val uri = archivos.prepararFoto()
        val imagen = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888)
        contexto.contentResolver.openOutputStream(uri)!!.use { imagen.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        imagen.recycle()
        val foto = archivos.guardarCopia(uri)
        try {
            assertEquals("image/jpeg", foto.tipo)
            val bytes = Base64.decode(archivos.convertirBase64(foto.ruta), Base64.NO_WRAP)
            assertTrue(bytes.isNotEmpty())
            assertTrue(bytes.size <= ArchivoRepository.MAXIMO_BYTES)
            assertNotNull(archivos.obtenerUri(foto.ruta))
        } finally { File(foto.ruta).delete() }
    }
}
