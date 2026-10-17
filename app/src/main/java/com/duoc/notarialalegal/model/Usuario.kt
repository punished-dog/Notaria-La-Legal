package com.duoc.notarialalegal.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String = Roles.CLIENTE
)
