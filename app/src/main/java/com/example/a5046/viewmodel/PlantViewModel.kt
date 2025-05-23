package com.example.a5046.viewmodel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.data.Plant
import com.example.a5046.data.PlantDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao = PlantDatabase.getDatabase(application).plantDao()// Local DB access
    private val firestore = FirebaseFirestore.getInstance()// Firebase Firestore
    private val auth = FirebaseAuth.getInstance()// Firebase Auth

    // get current user ID
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Insert a new plant (local DB + Firestore)
    fun insertPlant(plant: Plant, homeViewModel: com.example.a5046.viewmodel.HomeViewModel? = null) {
        viewModelScope.launch {
            // Save to Room database
            plantDao.insertPlant(plant)

            // Save to Firestore
            try {
                val plantMap = hashMapOf(
                    "name" to plant.name,
                    "plantingDate" to plant.plantingDate,
                    "plantType" to plant.plantType,
                    "wateringFrequency" to plant.wateringFrequency,
                    "fertilizingFrequency" to plant.fertilizingFrequency,
                    "lastWateredDate" to plant.lastWateredDate,
                    "lastFertilizedDate" to plant.lastFertilizedDate,
                    "userId" to plant.userId
                )
                plant.image?.let {
                    val base64Image = Base64.encodeToString(it, Base64.DEFAULT)
                    plantMap["image"] = base64Image
                }
                // Waiting for Firebase write to complete.
                firestore.collection("plants")
                    .add(plantMap)
                    .await()
                // Refresh the reminder after Firebase write is successful.
                homeViewModel?.loadReminders()
            } catch (e: Exception) {
                Log.e("PlantViewModel", "Exception occurred while saving to Firestore: ${e.message}", e)
            }
        }
    }

    // All plants as Flow (user-specific if logged in)
    val allPlants: Flow<List<Plant>> =
        auth.currentUser?.let {
            plantDao.getUserPlants(it.uid)
        } ?: plantDao.getAllPlants()

    // Delete a plant (from both Room and Firestore)
    fun deletePlant(plant: Plant, homeViewModel: com.example.a5046.viewmodel.HomeViewModel? = null) {
        viewModelScope.launch {
            // Delete from Room database
            plantDao.delete(plant)

            // Delete from Firestore
            try {
                val querySnapshot = firestore.collection("plants")
                    .whereEqualTo("name", plant.name)
                    .whereEqualTo("userId", plant.userId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    document.reference.delete().await()
                }

                // Synchronously delete the corresponding document in plantReminders.
                firestore.collection("users")
                    .document(plant.userId)
                    .collection("plantReminders")
                    .document(querySnapshot.documents.firstOrNull()?.id ?: plant.name)
                    .delete()
                // Refresh the reminder after Firebase write is successful.
                homeViewModel?.loadReminders()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    data class WeekFrequency(
        val label: String,
        val waterCount: Int,
        val fertilizeCount: Int
    )

    val frequencyByWeek = allPlants
        .map { plantList ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

            // Parse planting dates
            val plantsWithDate = plantList.mapNotNull { plant ->
                runCatching {
                    val date = LocalDate.parse(plant.plantingDate, formatter)
                    plant to date
                }.getOrNull()
            }

            // Group by week and calculate total frequencies
            val rawByWeek = plantsWithDate
                .groupBy { (_, date) -> date.with(ChronoField.DAY_OF_WEEK, 1) }
                .map { (weekStart, list) ->
                    WeekFrequency(
                        label = weekStart.toString(),
                        waterCount = list.sumOf { it.first.wateringFrequency.toIntOrNull() ?: 0 },
                        fertilizeCount = list.sumOf { it.first.fertilizingFrequency.toIntOrNull() ?: 0 }
                    )
                }
                .sortedBy { it.label }

            // Label weeks as "Week 1", "Week 2", etc.
            rawByWeek.mapIndexed { idx, wf ->
                wf.copy(label = "Week ${idx + 1}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Count of plants grouped by type
    val plantCounts: StateFlow<Map<String, Int>> = (
            auth.currentUser?.let {
                plantDao.getUserCountsByType(it.uid)
            } ?: plantDao.getCountsByType()
            ).map { list ->
            list.associate { it.type to it.count }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyMap()
        )
}