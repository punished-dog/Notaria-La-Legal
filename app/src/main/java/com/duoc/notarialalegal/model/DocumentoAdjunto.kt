package com.duoc.notarialalegal.model

// Copia privada del documento que se puede abrir aunque cambie la sesión.
data class DocumentoAdjunto(val nombre: String, val ruta: String, val tipo: String)
