package com.example.a5046.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.data.Plant
import com.example.a5046.data.PlantDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Blob
class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao = PlantDatabase.getDatabase(application).plantDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun insertPlant(plant: Plant) {
        viewModelScope.launch {
            // Insert to Room database
            plantDao.insertPlant(plant)
            
            // Also save to Firestore
            savePlantToFirestore(plant)
        }
    }
    
    private fun savePlantToFirestore(plant: Plant) {
        val userId = auth.currentUser?.uid ?: return
        
        // Convert plant to a Map for Firestore
        val plantMap = hashMapOf(
            "name" to plant.name,
            "plantingDate" to plant.plantingDate,
            "plantType" to plant.plantType,
            "wateringFrequency" to plant.wateringFrequency,
            "fertilizingFrequency" to plant.fertilizingFrequency,
            "lastWateredDate" to plant.lastWateredDate,
            "lastFertilizedDate" to plant.lastFertilizedDate,
            "userId" to plant.userId,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        // Save to Firestore
        firestore.collection("users")
            .document(userId)
            .collection("plants")
            .add(plantMap)
            .addOnSuccessListener { documentReference ->
                Log.d("PlantViewModel", "Plant saved to Firestore with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("PlantViewModel", "Error saving plant to Firestore", e)
            }
    }
}
