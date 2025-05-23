package com.example.a5046.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database class for the Plant entity.
 * It defines the database configuration and serves as the main access point to the data.
 */
@Database(entities = [Plant::class], version = 2, exportSchema = false)
abstract class PlantDatabase : RoomDatabase() {
    /**
     * Abstract method to get the DAO (Data Access Object) for Plant.
     */
    abstract fun plantDao(): PlantDao

    companion object {
        // Volatile ensures that the INSTANCE is visible to all threads immediately
        @Volatile private var INSTANCE: PlantDatabase? = null

        /**
         * Returns the singleton instance of the database.
         * If it doesn't exist, it builds one using Room.
         */
        fun getDatabase(context: Context): PlantDatabase {
            // Double-checked locking to ensure thread safety
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,    // Use application context to avoid leaks
                    PlantDatabase::class.java,     // Reference to the database class
                    "plant_database"        // Name of the database file
                )
                    // If no migration strategy exists, recreate the DB destructively (for dev use)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
