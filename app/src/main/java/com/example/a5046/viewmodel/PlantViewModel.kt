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


class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao = PlantDatabase.getDatabase(application).plantDao()
    fun insertPlant(plant: Plant) {
        viewModelScope.launch {
            plantDao.insertPlant(plant)
        }
    }

    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            plantDao.delete(plant)
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
