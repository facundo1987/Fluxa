package com.example.judio_premium.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Gasto::class], version = 1, exportSchema = false)
abstract class GastoDatabase : RoomDatabase() {
    abstract fun gastoDao(): GastoDao

    companion object {
        @Volatile
        private var INSTANCE: GastoDatabase? = null

        fun getDatabase(context: Context): GastoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GastoDatabase::class.java,
                    "gasto_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
