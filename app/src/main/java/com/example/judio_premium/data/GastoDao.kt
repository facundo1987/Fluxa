package com.example.judio_premium.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Insert
    suspend fun insertar(gasto: Gasto)

    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<Gasto>>
}
