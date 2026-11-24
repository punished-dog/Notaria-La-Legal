package com.duoc.notarialalegal.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.core.content.FileProvider
import com.duoc.notarialalegal.model.DocumentoAdjunto
import com.duoc.notarialalegal.network.DocumentoRespuesta
import java.io.File
import java.io.IOException

// Copias privadas para enviar o abrir documentos. El respaldo permanente está en Xano.
class ArchivoRepository(private val context: Context) {
    companion object {
        const val MAXIMO_BYTES = 2 * 1024 * 1024
        val TIPOS_PERMITIDOS = arrayOf("application/pdf", "image/jpeg", "image/png")
    }

    private fun carpeta(): File = File(context.filesDir, "documentos").apply {
        if (!exists() && !mkdirs()) throw IOException("No se pudo crear la carpeta de documentos.")
    }

    fun guardarCopia(uri: Uri): DocumentoAdjunto {
        if (uri.authority == "${context.packageName}.documentos" && uri.pathSegments.firstOrNull() == "camara") {
            return guardarFoto(uri)
        }
        val nombre = obtenerNombre(uri)
        val copia = File.createTempFile("adjunto_", ".bin", carpeta())
        try {
            val entrada = context.contentResolver.openInputStream(uri)
                ?: throw IOException("No se pudo leer el documento.")
            entrada.use { origen ->
                copia.outputStream().use { destino ->
                    val buffer = ByteArray(8192)
                    var total = 0
                    var leidos = origen.read(buffer)
                    while (leidos != -1) {
                        total += leidos
                        if (total > MAXIMO_BYTES) throw IOException("El archivo debe pesar como máximo 2 MB.")
                        destino.write(buffer, 0, leidos)
                        leidos = origen.read(buffer)
                    }
                }
            }
            val tipo = identificarTipo(copia)
            return DocumentoAdjunto(nombre, copia.absolutePath, tipo)
        } catch (error: Exception) {
            copia.delete()
            throw error
        }
    }

    private fun identificarTipo(archivo: File): String {
        val encabezado = archivo.inputStream().use { entrada ->
            val bytes = ByteArray(8)
            entrada.read(bytes)
            bytes
        }
        return when {
            encabezado.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray()) -> "application/pdf"
            encabezado[0] == 0xff.toByte() && encabezado[1] == 0xd8.toByte() -> "image/jpeg"
            encabezado.contentEquals(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) -> "image/png"
            else -> throw IOException("Selecciona un documento PDF o una imagen JPG o PNG.")
        }
    }

    // La cámara del sistema escribe aquí mediante un permiso temporal de FileProvider.
    fun prepararFoto(): Uri {
        val carpeta = File(context.cacheDir, "camara").apply { mkdirs() }
        val foto = File.createTempFile("foto_", ".jpg", carpeta)
        return FileProvider.getUriForFile(context, "${context.packageName}.documentos", foto)
    }

    private fun guardarFoto(uri: Uri): DocumentoAdjunto {
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opciones) }
        var reduccion = 1
        while (opciones.outWidth / reduccion > 1600 || opciones.outHeight / reduccion > 1600) reduccion *= 2
        opciones.inJustDecodeBounds = false
        opciones.inSampleSize = reduccion
        val original = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: throw IOException("No se pudo leer la fotografía.")
        val temporal = File(context.cacheDir, "camara/${uri.lastPathSegment}")
        val orientacion = ExifInterface(temporal.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val grados = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val imagen = if (grados == 0f) original else {
            val rotada = Bitmap.createBitmap(original, 0, 0, original.width, original.height, Matrix().apply { postRotate(grados) }, true)
            if (rotada !== original) original.recycle()
            rotada
        }
        val copia = File.createTempFile("foto_", ".jpg", carpeta())
        try {
            copia.outputStream().use { imagen.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            if (copia.length() > MAXIMO_BYTES) throw IOException("La fotografía debe pesar como máximo 2 MB.")
            return DocumentoAdjunto("Fotografía del documento.jpg", copia.absolutePath, "image/jpeg")
        } catch (error: Exception) {
            copia.delete()
            throw error
        } finally {
            imagen.recycle()
            temporal.delete()
        }
    }

    fun convertirBase64(ruta: String): String {
        val archivo = File(ruta)
        if (!archivo.isFile || archivo.length() == 0L || archivo.length() > MAXIMO_BYTES) {
            throw IOException("Adjunta un archivo válido de hasta 2 MB.")
        }
        return Base64.encodeToString(archivo.readBytes(), Base64.NO_WRAP)
    }

    fun guardarDescarga(documento: DocumentoRespuesta): DocumentoAdjunto {
        if (documento.contenido.length > 2796204 || documento.tipo !in TIPOS_PERMITIDOS) {
            throw IOException("El documento recibido no tiene un formato válido.")
        }
        val contenido = try { Base64.decode(documento.contenido, Base64.NO_WRAP) }
            catch (_: IllegalArgumentException) { throw IOException("No se pudo leer el documento recibido.") }
        if (contenido.isEmpty() || contenido.size > MAXIMO_BYTES) throw IOException("El documento recibido supera los 2 MB.")
        val extension = when (documento.tipo) { "application/pdf" -> ".pdf"; "image/png" -> ".png"; else -> ".jpg" }
        val copia = File.createTempFile("descarga_", extension, carpeta())
        try {
            copia.writeBytes(contenido)
            if (identificarTipo(copia) != documento.tipo) throw IOException("El contenido no coincide con el tipo de documento.")
            return DocumentoAdjunto(documento.nombre, copia.absolutePath, documento.tipo)
        } catch (error: Exception) {
            copia.delete()
            throw error
        }
    }

    // El formato es una solicitud de demostración, no un instrumento notarial oficial.
    fun crearFormato(tipo: String, nombre: String, antecedentes: String): DocumentoAdjunto {
        val archivo = File.createTempFile("solicitud_", ".pdf", carpeta())
        val pdf = PdfDocument()
        try {
            val pintura = Paint().apply { textSize = 12f }
            val lineas = listOf("NOTARIA LA LEGAL", "Solicitud de trámite", tipo, "Solicitante: $nombre", "Antecedentes:") +
                antecedentes.chunked(75) + listOf("", "Firma representada en la aplicación.", "Formato académico de solicitud, sin validez notarial.")
            // Se agregan páginas para no cortar antecedentes largos.
            lineas.chunked(42).forEachIndexed { numero, paginaLineas ->
                val pagina = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, numero + 1).create())
                paginaLineas.forEachIndexed { indice, linea -> pagina.canvas.drawText(linea, 40f, 55f + indice * 17f, pintura) }
                pdf.finishPage(pagina)
            }
            archivo.outputStream().use { pdf.writeTo(it) }
            return DocumentoAdjunto("Solicitud - $tipo.pdf", archivo.absolutePath, "application/pdf")
        } catch (error: Exception) {
            archivo.delete()
            throw error
        } finally { pdf.close() }
    }

    fun obtenerUri(ruta: String): Uri? {
        val archivo = File(ruta)
        if (ruta.isBlank() || !archivo.isFile) return null
        return FileProvider.getUriForFile(context, "${context.packageName}.documentos", archivo)
    }

    private fun obtenerNombre(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) return it.getString(0) ?: "documento"
        }
        return "documento"
    }
}
