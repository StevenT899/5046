package com.example.a5046.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.data.Plant
import com.example.a5046.data.PlantDatabase
import kotlinx.coroutines.launch

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao = PlantDatabase.getDatabase(application).plantDao()

    fun insertPlant(plant: Plant) {
        viewModelScope.launch {
            plantDao.insertPlant(plant)
        }
    }
}
