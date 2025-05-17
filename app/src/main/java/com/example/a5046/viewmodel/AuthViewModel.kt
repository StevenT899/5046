package com.example.a5046.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//reference from GPT
sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object Success : AuthState
    data class Error(val msg: String) : AuthState
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun signInEmail(email: String, pwd: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            auth.signInWithEmailAndPassword(email, pwd).await()
            _state.value = AuthState.Success
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Login failed")
        }
    }

    fun signUpEmail(email: String, pwd: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            auth.createUserWithEmailAndPassword(email, pwd).await()
            _state.value = AuthState.Success
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Register failed")
        }
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        val cred = GoogleAuthProvider.getCredential(idToken, null)
        try {
            auth.signInWithCredential(cred).await()
            _state.value = AuthState.Success
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Google login failed")
        }
    }

    fun signOut() = auth.signOut()
    val currentUser get() = auth.currentUser
}
