package com.duoc.notarialalegal.model

// Reglas de validación de los formularios.
// Cada función devuelve el mensaje de error, o un texto vacío si el dato es correcto.
object Validaciones {
    private val patronCorreo = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validarCorreo(correo: String): String {
        return if (correo.isBlank()) {
            "Ingresa tu correo electrónico."
        } else if (!patronCorreo.matches(correo)) {
            "Ingresa un correo válido."
        } else {
            ""
        }
    }

    fun validarContrasena(contrasena: String): String {
        return if (contrasena.isBlank()) {
            "Ingresa tu contraseña."
        } else if (contrasena.length < 6) {
            "La contraseña debe tener al menos 6 caracteres."
        } else {
            ""
        }
    }

    fun validarNombre(nombre: String): String {
        return if (nombre.trim().length < 3) "Ingresa un nombre de al menos 3 caracteres." else ""
    }

    fun validarConfirmacion(contrasena: String, confirmacion: String): String {
        return if (contrasena != confirmacion) "Las contraseñas no coinciden." else ""
    }

    // Se usa para los antecedentes del trámite y para las observaciones del notario
    fun validarTextoMinimo(texto: String, campo: String): String {
        return if (texto.trim().length < 10) "Escribe al menos 10 caracteres en $campo." else ""
    }
}
