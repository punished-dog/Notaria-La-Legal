package com.duoc.notarialalegal.model

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MigracionRoomTest {
    @Test fun actualizarLaSesionConservaLosDatosYAgregaElToken() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val nombre = "migracion_${System.nanoTime()}.db"
        val ruta = contexto.getDatabasePath(nombre)
        ruta.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(ruta, null).use {
            it.execSQL("CREATE TABLE sesion (id INTEGER NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, correo TEXT NOT NULL, rol TEXT NOT NULL)")
            it.execSQL("INSERT INTO sesion VALUES (1, 'Cliente', 'cliente@ejemplo.test', 'Cliente')")
            it.version = 1
        }
        val base = Room.databaseBuilder(contexto, AppDatabase::class.java, nombre)
            .addMigrations(AppDatabase.MIGRACION_1_2).build()
        try {
            val sesion = base.sesionDao().obtener()!!
            assertEquals("cliente@ejemplo.test", sesion.correo)
            assertEquals(0, sesion.usuarioId)
            assertEquals("", sesion.tokenCifrado)
        } finally {
            base.close()
            contexto.deleteDatabase(nombre)
        }
    }
}
