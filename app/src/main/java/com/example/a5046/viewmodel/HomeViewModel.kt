package com.example.a5046.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.*

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

    private val _address = MutableStateFlow<String>("Loading...")
    val address: StateFlow<String> = _address

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

    fun updateAddress(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _address.value = getAddressFromLocation(context, latitude, longitude)
        }
    }

    private fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: ""
                val state = address.adminArea ?: ""
                "$city, $state"
            } else {
                "No address found"
            }
        } catch (e: IOException) {
            "Geocoder failed: ${e.message}"
        }
    }
} 