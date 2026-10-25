package com.duoc.notarialalegal.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

// Lee la información del archivo que el usuario eligió con el selector de Android.
// El archivo no se sube a ningún servidor: solo guardamos su nombre en el trámite.
class ArchivoRepository(private val context: Context) {

    fun obtenerNombre(uri: Uri): String {
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
