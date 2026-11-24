package com.duoc.notarialalegal.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val rol: String = Roles.CLIENTE,
    val id: Int = 0
)
