package com.example.a5046.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.data.Plant
import com.example.a5046.data.PlantDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow


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
