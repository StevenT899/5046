package com.example.a5046.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Recommendation(
    val id: String = "",
    val title: String = "",
    val content: String = ""
)

sealed class RecommendationState {
    object Loading : RecommendationState()
    data class Success(val recommendations: List<Recommendation>) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
}


class RecommendationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Loading)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState

    fun loadRecommendations(gardeningLevel: String) {
        viewModelScope.launch {
            try {
                Log.d("RecommendationViewModel", "Loading recommendations for level: $gardeningLevel")
                _recommendationState.value = RecommendationState.Loading
                
                val (startId, endId) = when (gardeningLevel) {
                    "Gardening Beginner" -> Pair("1", "2")
                    "Gardening Novice" -> Pair("3", "4")
                    "Gardening Enthusiast" -> Pair("5", "6")
                    else -> Pair("1", "2")
                }
                
                Log.d("RecommendationViewModel", "Fetching articles with IDs: $startId, $endId")

                val recommendations = db.collection("recommendations")
                    .whereIn("id", listOf(startId, endId))
                    .orderBy("id")
                    .get()
                    .await()
                    .toObjects(Recommendation::class.java)

                Log.d("RecommendationViewModel", "Fetched ${recommendations.size} recommendations")
                recommendations.forEach { 
                    Log.d("RecommendationViewModel", "Recommendation: id=${it.id}, title=${it.title}")
                }

                _recommendationState.value = RecommendationState.Success(recommendations)
            } catch (e: Exception) {
                Log.e("RecommendationViewModel", "Error loading recommendations", e)
                _recommendationState.value = RecommendationState.Error(e.message ?: "Failed to load recommendations")
            }
        }
    }
} 