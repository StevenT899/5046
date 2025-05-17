package com.example.a5046.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Auth status definition
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

    private val _currentUserState = MutableStateFlow(auth.currentUser)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState

    //Email + Password Registration with Duplicate Check,reference from AI and
    fun signUpEmail(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            _currentUserState.value = auth.currentUser
            _state.value = AuthState.Success
        } catch (e: FirebaseAuthUserCollisionException) {
            _state.value = AuthState.Error("This email is already registered.")
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Registration failed. Please try again.")
        }
    }
    //Email + Password login with Duplicate Check
    fun signInEmail(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            _currentUserState.value = auth.currentUser
            _state.value = AuthState.Success
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Login failed. Please try again.")
        }
    }

    //Google Sign-In
    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            auth.signInWithCredential(credential).await()
            _currentUserState.value = auth.currentUser
            _state.value = AuthState.Success
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Google login failed.")
        }
    }

    // Sign out
    fun signOut(context: Context) {
        val googleSignInClient = GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        googleSignInClient.signOut().addOnCompleteListener {
            auth.signOut()
            _currentUserState.value = null
        }
    }

    private val _hasCompletedProfile = MutableStateFlow(false)
    val hasCompletedProfile: StateFlow<Boolean> = _hasCompletedProfile
    fun markProfileCompleted() {
        _hasCompletedProfile.value = true
    }
}