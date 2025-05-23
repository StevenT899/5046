package com.example.a5046.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a5046.R
import com.example.a5046.PlantApplication
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
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Base64
import android.util.Log
import com.example.a5046.data.Plant
import com.example.a5046.data.PlantDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Auth status definition
sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object Success : AuthState
    data class Error(val msg: String) : AuthState
}
// reference from AI
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private val _currentUserState = MutableStateFlow(auth.currentUser)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState

    private val _hasCompletedProfile = MutableStateFlow(false)
    val hasCompletedProfile: StateFlow<Boolean> = _hasCompletedProfile

    init {
        // Check profile completion status when ViewModel is created
        auth.currentUser?.let { user ->
            checkIfProfileCompleted { }
        }
    }

    //Email + Password Registration with Duplicate Check,reference from AI and
    fun signUpEmail(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            _currentUserState.value = auth.currentUser
            _hasCompletedProfile.value = false // Reset profile completion status for new user
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
            checkIfProfileCompleted { }  // Check profile status after successful login
            
            // Sync plants data after successful login
            auth.currentUser?.uid?.let { userId ->
                syncPlantsFromFirestore(userId)
            }
            
            // Initialize WorkManager after successful login
            (getApplication() as PlantApplication).setupWorkManager()
            
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
            checkIfProfileCompleted { }  // Check profile status after successful Google sign-in
            
            // Sync plants data after successful Google sign-in
            auth.currentUser?.uid?.let { userId ->
                syncPlantsFromFirestore(userId)
            }
            
            // Initialize WorkManager after successful Google sign-in
            (getApplication() as PlantApplication).setupWorkManager()
            
            _state.value = AuthState.Success
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Google login failed.")
        }
    }
    //  the part coding reference from AI
    // Sync plants data from Firestore to local Room database
    private suspend fun syncPlantsFromFirestore(userId: String) {
        try {
            withContext(Dispatchers.IO) {
                val plantDao = PlantDatabase.getDatabase(getApplication()).plantDao()
                //  the part coding reference from AI
                // 1. Clear existing plants in Room database
                plantDao.deleteAllUserPlants(userId)
                
                Log.d("AuthViewModel", "Cleared local plant database for user: $userId")
                
                // 2. Fetch plants from Firestore
                val querySnapshot = firestore.collection("plants")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                Log.d("AuthViewModel", "Found ${querySnapshot.documents.size} plants in Firestore")
                
                // 3. Insert plants into Room database
                for (document in querySnapshot.documents) {
                    val plant = Plant(
                        id = 0, // Room will auto-generate ID
                        name = document.getString("name") ?: "",
                        plantingDate = document.getString("plantingDate") ?: "",
                        plantType = document.getString("plantType") ?: "",
                        wateringFrequency = document.getString("wateringFrequency") ?: "",
                        fertilizingFrequency = document.getString("fertilizingFrequency") ?: "",
                        lastWateredDate = document.getString("lastWateredDate") ?: "",
                        lastFertilizedDate = document.getString("lastFertilizedDate") ?: "",
                        userId = userId,
                        image = null // Default to null
                    )
                    
                    // Process image if exists
                    document.getString("image")?.let { base64Image ->
                        try {
                            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            plant.image = imageBytes
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "Failed to decode image", e)
                        }
                    }
                    
                    // Insert plant into Room
                    plantDao.insertPlant(plant)
                }
                
                Log.d("AuthViewModel", "Successfully synchronized plants to local database")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error synchronizing plants: ${e.message}", e)
        }
    }
    //  the part coding reference from AI
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
            _hasCompletedProfile.value = false  // Reset profile completion status on sign out
        }
    }

    fun markProfileCompleted() {
        _hasCompletedProfile.value = true
    }
    //  the part coding reference from AI
//    Check from Firestore whether the current user has already completed
    fun checkIfProfileCompleted(onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch

        try {
            val doc = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            val isCompleted = doc.exists() && (doc.getBoolean("profileCompleted") == true)
            _hasCompletedProfile.value = isCompleted
            onResult(isCompleted)
        } catch (e: Exception) {
            _hasCompletedProfile.value = false
            onResult(false)
        }
    }
}