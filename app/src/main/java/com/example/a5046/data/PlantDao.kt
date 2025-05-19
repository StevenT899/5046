package com.example.a5046.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)

    @Query("SELECT * FROM plant_table ORDER BY id DESC")
    fun getAllPlants(): Flow<List<Plant>>
}
