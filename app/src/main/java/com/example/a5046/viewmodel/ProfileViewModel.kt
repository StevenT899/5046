package com.example.a5046.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val name: String = "",
    val phone: String = "",
    val age: String = "",
    val gender: String = "",
    val level: String = ""
)

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val profile: UserProfile) : ProfileState
    data class Error(val message: String) : ProfileState
}

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _currentProgress = MutableStateFlow(0f)
    val currentProgress: StateFlow<Float> = _currentProgress

    companion object {
        const val MAX_PROGRESS = 3000f
    }

    init {
        loadUserProfile()
    }

    fun loadUserProfile() = viewModelScope.launch {
        _profileState.value = ProfileState.Loading

        val uid = auth.currentUser?.uid
        if (uid == null) {
            _profileState.value = ProfileState.Error("Not logged in.")
            return@launch
        }

        try {
            val doc = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            if (doc.exists()) {
                val activities = doc.getLong("activities")?.toFloat() ?: 0f
                _currentProgress.value = activities.coerceIn(0f, MAX_PROGRESS)

                val profile = UserProfile(
                    name = doc.getString("name") ?: "",
                    phone = doc.getString("phone") ?: "",
                    age = doc.getString("age") ?: "",
                    gender = doc.getString("gender") ?: "",
                    level = doc.getString("level") ?: ""
                )
                _profileState.value = ProfileState.Success(profile)
            } else {
                _profileState.value = ProfileState.Error("No profile found.")
            }
        } catch (e: Exception) {
            _profileState.value = ProfileState.Error(e.message ?: "Failed to load profile")
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }
}