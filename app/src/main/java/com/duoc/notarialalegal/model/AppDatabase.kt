package com.duoc.notarialalegal.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Sesion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sesionDao(): SesionDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        // Se crea una sola base de datos para toda la app
        fun obtenerBaseDatos(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val baseDatos = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notaria_db"
                ).build()
                instancia = baseDatos
                baseDatos
            }
        }
    }
}
