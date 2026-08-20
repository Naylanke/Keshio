package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, UserSettingsEntity::class],
    version = 3,
    exportSchema = false
)
abstract class KeshioDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: KeshioDatabase? = null

        fun getDatabase(context: Context): KeshioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeshioDatabase::class.java,
                    "keshio_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
