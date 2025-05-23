package com.example.a5046.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Declare a Room Entity with table name "plant_table"
@Entity(tableName = "plant_table")
data class Plant(
    // Primary key that auto-generates a unique ID for each plant
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Plant name (e.g., "Rose", "Basil")
    val name: String,
    // Date the plant was added or planted
    val plantingDate: String,
    val plantType: String,
    // Frequency (in days) the plant needs to be watered
    val wateringFrequency: String,
    val fertilizingFrequency: String,
    val lastWateredDate: String,
    val lastFertilizedDate: String,
    // Optional image of the plant, stored as a byte array (nullable)
    var image: ByteArray? = null,
    // Firebase user ID that owns this plant
    val userId: String
)