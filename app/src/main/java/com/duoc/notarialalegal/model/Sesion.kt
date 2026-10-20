package com.duoc.notarialalegal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Única tabla de Room: guarda al usuario que inició sesión.
// El id siempre es 1, así nunca hay más de una sesión guardada.
@Entity(tableName = "sesion")
data class Sesion(
    @PrimaryKey val id: Int = 1,
    val nombre: String,
    val correo: String,
    val rol: String
)
