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

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState

    fun submitUserInfo(
        name: String,
        phone: String,
        age: String,
        gender: String,
        level: String
    ) = viewModelScope.launch {
        _submitState.value = SubmitState.Loading

        val uid = FirebaseAuth.getInstance().currentUser?.uid
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
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(userInfo)
                .await()

            _submitState.value = SubmitState.Success
        } catch (e: Exception) {
            _submitState.value = SubmitState.Error(e.message ?: "Unknown error")
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }
}
