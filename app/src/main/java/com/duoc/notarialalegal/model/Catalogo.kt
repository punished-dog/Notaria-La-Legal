package com.duoc.notarialalegal.model

object Roles {
    const val CLIENTE = "Cliente"
    const val NOTARIO = "Notario"
}

object EstadoTramite {
    const val ENVIADO = "Enviado al notario"
    const val OBSERVADO = "Con observaciones"
    const val APROBADO = "Aprobado"
}

// Opciones que se muestran en el formulario de nuevo trámite.
object Catalogo {
    val tiposTramite = listOf(
        "Autorización de documento",
        "Declaración jurada",
        "Poder simple",
        "Contrato de arriendo",
        "Finiquito laboral"
    )

    val tiposFirma = listOf(
        "Firma electrónica simple",
        "Firma electrónica avanzada",
        "Firma ante notario"
    )
}
