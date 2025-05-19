package com.example.a5046.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_table")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val plantingDate: String,
    val plantType: String,
    val wateringFrequency: String,
    val fertilizingFrequency: String,
    val lastWateredDate: String,
    val lastFertilizedDate: String,
    val image: ByteArray? = null,
    val userId: String
)

