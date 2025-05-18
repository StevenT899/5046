package com.example.a5046.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserData(
    val name: String = "",
    val level: String = ""
)

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val userData: UserData) : HomeState
    data class Error(val message: String) : HomeState
}

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState

    init {
        loadUserData()
    }

    private fun loadUserData() = viewModelScope.launch {
        try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _homeState.value = HomeState.Error("User not logged in")
                return@launch
            }

            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val userData = UserData(
                    name = document.getString("name") ?: "User",
                    level = document.getString("level") ?: "Gardening Beginner"
                )
                _homeState.value = HomeState.Success(userData)
            } else {
                _homeState.value = HomeState.Error("User data not found")
            }
        } catch (e: Exception) {
            _homeState.value = HomeState.Error(e.message ?: "Failed to load user data")
        }
    }

    fun refreshUserData() {
        loadUserData()
    }
} 