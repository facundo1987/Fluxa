package com.example.judio_premium.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GastoDatabaseTest {
    private lateinit var db: GastoDatabase
    private lateinit var dao: GastoDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GastoDatabase::class.java).build()
        dao = db.gastoDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadGasto() = runBlocking {
        val gasto = Gasto(
            monto = 100.0,
            categoria = "Comida",
            persona = "Yo",
            fecha = "2024-01-01",
            descripcion = "Pizza"
        )
        dao.insertar(gasto)
        val todos = dao.obtenerTodos().first()
        assertEquals(todos[0].monto, 100.0, 0.0)
        assertEquals(todos[0].categoria, "Comida")
    }

    @Test
    fun deleteGasto() = runBlocking {
        val gasto = Gasto(
            monto = 50.0,
            categoria = "Transporte",
            persona = "Emma",
            fecha = "2024-01-02",
            descripcion = "Bondi"
        )
        dao.insertar(gasto)
        var todos = dao.obtenerTodos().first()
        assertEquals(todos.size, 1)

        dao.eliminar(todos[0])
        todos = dao.obtenerTodos().first()
        assertEquals(todos.size, 0)
    }
}
