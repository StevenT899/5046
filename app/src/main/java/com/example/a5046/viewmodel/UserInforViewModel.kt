package com.example.a5046.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Loading : SubmitState
    data object Success : SubmitState
    data class Error(val msg: String) : SubmitState
}

class UserInfoViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState

    init {
        // Check if profile is already completed when ViewModel is created
        checkExistingProfile()
    }

    private fun checkExistingProfile() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val doc = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            if (doc.exists() && doc.getBoolean("profileCompleted") == true) {
                _submitState.value = SubmitState.Success
            }
        } catch (e: Exception) {
            // Silently handle the error as this is just an initial check
        }
    }

    fun submitUserInfo(
        name: String,
        phone: String,
        age: String,
        gender: String,
        level: String
    ) = viewModelScope.launch {
        // Validate required fields
        if (name.isBlank() || phone.isBlank() || age.isBlank() || gender.isBlank() || level.isBlank()) {
            _submitState.value = SubmitState.Error("All fields are required")
            return@launch
        }

        _submitState.value = SubmitState.Loading

        val uid = auth.currentUser?.uid
        if (uid == null) {
            _submitState.value = SubmitState.Error("User not logged in")
            return@launch
        }

        val userInfo = mapOf(
            "name" to name,
            "phone" to phone,
            "age" to age,
            "gender" to gender,
            "level" to level,
            "profileCompleted" to true
        )

        try {
            // Update or create the user document
            firestore
                .collection("users")
                .document(uid)
                .set(userInfo)
                .await()

            _submitState.value = SubmitState.Success
        } catch (e: Exception) {
            _submitState.value = SubmitState.Error(e.message ?: "Failed to save user information")
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }
}
