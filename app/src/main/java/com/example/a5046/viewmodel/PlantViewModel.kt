package com.example.a5046.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.data.Plant
import com.example.a5046.data.PlantDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoField
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Base64
import kotlinx.coroutines.tasks.await
import android.util.Log


class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao = PlantDatabase.getDatabase(application).plantDao()
    private val firestore = FirebaseFirestore.getInstance()

    fun insertPlant(plant: Plant) {
        viewModelScope.launch {
            // Save to Room database
            plantDao.insertPlant(plant)

            // Save to Firestore
            saveToFirestore(plant)
        }
    }

    private suspend fun saveToFirestore(plant: Plant) {
        try {
            // Create Firestore document data
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

            // Convert image to Base64 if available
            plant.image?.let {
                val base64Image = Base64.encodeToString(it, Base64.DEFAULT)
                plantMap["image"] = base64Image
            }

            Log.d("PlantViewModel", "Starting to save plant to Firestore: ${plant.name}")

            // Store data in Firestore
            firestore.collection("plants")
                .add(plantMap)
                .addOnSuccessListener { documentReference ->
                    Log.d("PlantViewModel", "Plant successfully saved to Firestore, ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("PlantViewModel", "Failed to save plant to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            // Handle errors, log or notify user if needed
            Log.e("PlantViewModel", "Exception occurred while saving to Firestore: ${e.message}", e)
        }
    }

    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            plantDao.delete(plant)

            // Delete from Firestore
            try {
                // Query documents by plant name and user ID
                val querySnapshot = firestore.collection("plants")
                    .whereEqualTo("name", plant.name)
                    .whereEqualTo("userId", plant.userId)
                    .get()
                    .await()

                // Delete all matching documents
                for (document in querySnapshot.documents) {
                    document.reference.delete().await()
                }
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

            val plantsWithDate = plantList.mapNotNull { plant ->
                runCatching {
                    val date = LocalDate.parse(plant.plantingDate, formatter)
                    plant to date
                }.getOrNull()
            }

            val rawByWeek = plantsWithDate
                .groupBy { (_, date) -> date.with(ChronoField.DAY_OF_WEEK, 1) }
                .map { (weekStart, list) ->
                    WeekFrequency(
                        label = weekStart.toString(),
                        waterCount     = list.sumOf { it.first.wateringFrequency.toIntOrNull() ?: 0 },
                        fertilizeCount = list.sumOf { it.first.fertilizingFrequency.toIntOrNull() ?: 0 }
                    )
                }
                .sortedBy { it.label }

            rawByWeek.mapIndexed { idx, wf ->
                wf.copy(label = "Week ${idx + 1}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val plantCounts: StateFlow<Map<String, Int>> =
        plantDao.getCountsByType()
            .map { list ->
                list.associate { it.type to it.count }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyMap()
            )
}