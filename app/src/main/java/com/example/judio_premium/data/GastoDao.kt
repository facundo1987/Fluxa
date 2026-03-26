package com.example.judio_premium.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Insert
    suspend fun insertar(gasto: Gasto)

    @Delete
    suspend fun eliminar(gasto: Gasto)

    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<Gasto>>

    // Categorias
    @Insert
    suspend fun insertarCategoria(categoria: Categoria)

    @Delete
    suspend fun eliminarCategoria(categoria: Categoria)

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun obtenerCategorias(): Flow<List<Categoria>>

    // Personas
    @Insert
    suspend fun insertarPersona(persona: Persona)

    @Delete
    suspend fun eliminarPersona(persona: Persona)

    @Query("SELECT * FROM personas ORDER BY nombre ASC")
    fun obtenerPersonas(): Flow<List<Persona>>
}
