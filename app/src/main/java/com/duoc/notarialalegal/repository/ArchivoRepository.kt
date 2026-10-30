package com.duoc.notarialalegal.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.duoc.notarialalegal.model.DocumentoAdjunto
import java.io.File
import java.io.IOException

// Lee la información del archivo que el usuario eligió con el selector de Android.
// Conserva una copia privada en el mismo teléfono; no se sube a un servidor.
class ArchivoRepository(private val context: Context) {

    // Se llama desde un hilo de IO para no bloquear la pantalla.
    fun guardarCopia(uri: Uri): DocumentoAdjunto {
        val nombre = obtenerNombre(uri)
        val carpeta = File(context.filesDir, "documentos")
        if (!carpeta.exists() && !carpeta.mkdirs()) throw IOException("No se pudo crear la carpeta")
        val extension = nombre.substringAfterLast('.', "bin").filter { it.isLetterOrDigit() }.take(10).ifEmpty { "bin" }
        val copia = File.createTempFile("adjunto_", ".$extension", carpeta)
        try {
            val entrada = context.contentResolver.openInputStream(uri)
                ?: throw IOException("No se pudo leer el documento")
            entrada.use { origen -> copia.outputStream().use { destino -> origen.copyTo(destino) } }
            return DocumentoAdjunto(nombre, copia.absolutePath,
                context.contentResolver.getType(uri) ?: "application/octet-stream")
        } catch (error: Exception) {
            copia.delete()
            throw error
        }
    }

    fun obtenerUri(ruta: String): Uri? {
        if (ruta.isBlank()) return null
        val archivo = File(ruta)
        if (!archivo.isFile) return null
        return FileProvider.getUriForFile(context, "${context.packageName}.documentos", archivo)
    }

    private fun obtenerNombre(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val columnaNombre = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columnaNombre >= 0 && it.moveToFirst()) {
                val nombre: String? = it.getString(columnaNombre)
                if (nombre != null) {
                    return nombre
                }
            }
        }
        return uri.lastPathSegment ?: "documento"
    }
}
