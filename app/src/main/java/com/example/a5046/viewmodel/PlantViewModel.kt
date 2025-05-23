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

// ViewModel to handle plant data logic (Room + Firestore)
class PlantViewModel(application: Application) : AndroidViewModel(application) {

    // Local database access (Room)
    private val plantDao = PlantDatabase.getDatabase(application).plantDao()

    // Firestore and FirebaseAuth instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get the current user ID (empty string if not logged in)
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    /**
     * Insert a plant into both Room and Firestore
     * Optionally refresh reminders after successful save
     */
    fun insertPlant(plant: Plant, homeViewModel: com.example.a5046.viewmodel.HomeViewModel? = null) {
        viewModelScope.launch {
            // Save plant to local Room DB
            plantDao.insertPlant(plant)

            try {
                // Prepare Firestore document data
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
                // Add image as base64 string if available
                plant.image?.let {
                    val base64Image = Base64.encodeToString(it, Base64.DEFAULT)
                    plantMap["image"] = base64Image
                }
                // Save to Firestore
                firestore.collection("plants")
                    .add(plantMap)
                    .await()
                // After successful save, reload reminders
                homeViewModel?.loadReminders()
            } catch (e: Exception) {
                Log.e("PlantViewModel", "Exception occurred while saving to Firestore: ${e.message}", e)
            }
        }
    }

    // Observe all plants for the current user (or all if no user found)
    val allPlants: Flow<List<Plant>> = 
        auth.currentUser?.let {
            plantDao.getUserPlants(it.uid)
        } ?: plantDao.getAllPlants()

    /**
     * Delete a plant from both Room and Firestore
     * Also remove its corresponding reminder document
     */
    fun deletePlant(plant: Plant, homeViewModel: com.example.a5046.viewmodel.HomeViewModel? = null) {
        viewModelScope.launch {
            // Delete from Room database
            plantDao.delete(plant)

            try {
                // Find and delete from Firestore
                val querySnapshot = firestore.collection("plants")
                    .whereEqualTo("name", plant.name)
                    .whereEqualTo("userId", plant.userId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    document.reference.delete().await()
                }

                // Also delete reminder associated with this plant
                firestore.collection("users")
                    .document(plant.userId)
                    .collection("plantReminders")
                    .document(querySnapshot.documents.firstOrNull()?.id ?: plant.name)
                    .delete()
                // Refresh reminders after deletion
                homeViewModel?.loadReminders()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Data class for weekly summary used in bar chart */
    data class WeekFrequency(
        val label: String,   // Week label (e.g. Week 1, Week 2...)
        val waterCount: Int,  // Total watering frequency that week
        val fertilizeCount: Int  // Total fertilizing frequency that week
    )

    /**
     * Weekly frequency aggregation logic.
     * Groups all plants by their planting week and totals the water/fertilize values.
     */
    val frequencyByWeek = allPlants
        .map { plantList ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

            val plantsWithDate = plantList.mapNotNull { plant ->
                runCatching {
                    val date = LocalDate.parse(plant.plantingDate, formatter)
                    plant to date
                }.getOrNull()
            }
            // Group by week and summarize frequencies
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
            // Add "Week 1", "Week 2" labels
            rawByWeek.mapIndexed { idx, wf ->
                wf.copy(label = "Week ${idx + 1}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Count number of plants per type (used in pie chart)
     * Gets data from Room DAO and maps it to key-value format
     */
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
