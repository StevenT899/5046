package com.example.a5046.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val profile: UserProfile) : ProfileState
    data class Error(val message: String) : ProfileState
}

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() = viewModelScope.launch {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _profileState.value = ProfileState.Error("Not logged in.")
            return@launch
        }

        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            val profile = snapshot.toObject(UserProfile::class.java)
            if (profile != null) {
                _profileState.value = ProfileState.Success(profile)
            } else {
                _profileState.value = ProfileState.Error("No profile found.")
            }
        } catch (e: Exception) {
            _profileState.value = ProfileState.Error(e.message ?: "Unknown error")
        }
    }
}