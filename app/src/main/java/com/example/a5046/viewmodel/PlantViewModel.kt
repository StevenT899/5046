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

    private val plantDao = PlantDatabase.getDatabase(application).plantDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 获取当前用户ID
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

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
            Log.e("PlantViewModel", "Exception occurred while saving to Firestore: ${e.message}", e)
        }
    }

    // 只获取当前用户的植物列表
    val allPlants: Flow<List<Plant>> = 
        auth.currentUser?.let {
            plantDao.getUserPlants(it.uid)
        } ?: plantDao.getAllPlants() // 如果用户未登录，返回所有植物（理论上应该为空）

    fun deletePlant(plant: Plant) {
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

                // 同步删除plantReminders中的对应文档
                firestore.collection("users")
                    .document(plant.userId)
                    .collection("plantReminders")
                    .document(querySnapshot.documents.firstOrNull()?.id ?: plant.name)
                    .delete()
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
                        waterCount = list.sumOf { it.first.wateringFrequency.toIntOrNull() ?: 0 },
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

    // 只获取当前用户的植物类型统计
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
