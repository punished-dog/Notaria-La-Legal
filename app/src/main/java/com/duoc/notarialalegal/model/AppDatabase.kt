package com.duoc.notarialalegal.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Sesion::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sesionDao(): SesionDao

    companion object {
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sesion ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sesion ADD COLUMN tokenCifrado TEXT NOT NULL DEFAULT ''")
            }
        }
        @Volatile
        private var instancia: AppDatabase? = null

        // Se crea una sola base de datos para toda la app
        fun obtenerBaseDatos(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val baseDatos = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notaria_db"
                ).addMigrations(MIGRACION_1_2).build()
                instancia = baseDatos
                baseDatos
            }
        }
    }
}
