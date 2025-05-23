package com.example.a5046.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


// DAO (Data Access Object) interface for accessing Plant-related data from Room database
@Dao
interface PlantDao {
    /**
     * Insert a plant into the database.
     * If a conflict occurs (e.g., same ID), the existing entry will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)

    /**
     * Get all plants from the database, ordered by ID (newest first).
     * This is useful for debugging or admin-level access.
     */
    @Query("SELECT * FROM plant_table ORDER BY id DESC")
    fun getAllPlants(): Flow<List<Plant>>

    /**
     * Get the count of plants by type for a specific user.
     * Used to generate user-specific summaries.
     */
    @Query("SELECT * FROM plant_table WHERE userId = :userId ORDER BY id DESC")
    fun getUserPlants(userId: String): Flow<List<Plant>>

    /**
     * Delete a single plant from the database.
     */
    @Delete
    suspend fun delete(plant: Plant)

    /**
     * Delete all plants associated with a specific user.
     * Useful for account deletion or reset.
     */
    @Query("DELETE FROM plant_table WHERE userId = :userId")
    suspend fun deleteAllUserPlants(userId: String)

    /**
     * Get the count of plants by plant type (for all users).
     * Used for generating summary statistics or charts (e.g., pie chart).
     */
    @Query("SELECT plantType AS type, COUNT(*) AS count FROM plant_table GROUP BY type")
    fun getCountsByType(): Flow<List<TypeCount>>

    /**
     * Get the count of plants by type for a specific user.
     * Used to generate user-specific summaries.
     */
    @Query("SELECT plantType AS type, COUNT(*) AS count FROM plant_table WHERE userId = :userId GROUP BY type")
    fun getUserCountsByType(userId: String): Flow<List<TypeCount>>

    /**
     * Data class representing the result of type-based aggregation.
     * Returned from count query methods above.
     */
    data class TypeCount(
        val type: String,
        val count: Int
    )
}
