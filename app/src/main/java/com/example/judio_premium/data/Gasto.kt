package com.example.judio_premium.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monto: Double,
    val categoria: String,
    val descripcion: String,
    val persona: String,
    val fecha: String
)
