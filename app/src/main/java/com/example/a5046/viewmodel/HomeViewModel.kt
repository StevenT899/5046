// HomeViewModel.kt
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import androidx.work.workDataOf


data class UserData(val name: String = "", val level: String = "")

data class PlantReminder(
    val id: String,
    val plantName: String,
    val needWater: Boolean,
    val needFertilize: Boolean
)

data class ReminderItem(
    val id: String,
    val plantName: String,
    val needWater: Boolean,
    val needFertilize: Boolean,
    var isDone: Boolean = false
)

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val userData: UserData) : HomeState
    data class Error(val message: String) : HomeState
}

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
//    StateFlows consumed by Composables
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState

    private val _address = MutableStateFlow("Loading...")
    val address: StateFlow<String> = _address

    private val _plantReminders = MutableStateFlow<List<PlantReminder>>(emptyList())
    val plantReminders: StateFlow<List<PlantReminder>> = _plantReminders

    init {
        loadUserData()
    }
//    user profile
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
//    today's reminders
    fun loadReminders() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
                val plantsSnapshot = firestore.collection("plants")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val reminders = plantsSnapshot.documents.mapNotNull { doc ->
                    val plantName = doc.getString("name") ?: return@mapNotNull null
                    val lastWateredStr = doc.getString("lastWateredDate")
                    val lastFertilizedStr = doc.getString("lastFertilizedDate")
                    val waterFreq = doc.getString("wateringFrequency")?.toIntOrNull()
                    val fertFreq = doc.getString("fertilizingFrequency")?.toIntOrNull()
                    val needWater = if (!lastWateredStr.isNullOrEmpty() && waterFreq != null) {
                        val lastWatered = LocalDate.parse(lastWateredStr, formatter)
                        !lastWatered.plusDays(waterFreq.toLong()).isAfter(today)
                    } else false
                    val needFertilize = if (!lastFertilizedStr.isNullOrEmpty() && fertFreq != null) {
                        val lastFertilized = LocalDate.parse(lastFertilizedStr, formatter)
                        !lastFertilized.plusDays(fertFreq.toLong()).isAfter(today)
                    } else false
                    if (needWater || needFertilize) {
                        PlantReminder(
                            id = doc.id,
                            plantName = plantName,
                            needWater = needWater,
                            needFertilize = needFertilize
                        )
                    } else null
                }
                _plantReminders.value = reminders
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading reminders: ${e.message}", e)
            }
        }
    }

    fun markReminderDone(reminder: ReminderItem) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        val docRef = firestore.collection("users").document(uid).collection("plantReminders").document(reminder.id)
        docRef.update("isDone", true)

        val userRef = firestore.collection("users").document(uid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val current = snapshot.getLong("activities") ?: 0L
            transaction.update(userRef, "activities", current + 1)
        }.await()

        val now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        val plantRef = firestore.collection("users").document(uid).collection("plants").document(reminder.id)
        if (reminder.needWater) plantRef.update("lastWatered", now)
        if (reminder.needFertilize) plantRef.update("lastFertilized", now)

        loadReminders()
    }
//    debug helper
    fun debugRunReminderCheck(context: Context) = viewModelScope.launch {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

        val workRequest = OneTimeWorkRequestBuilder<PlantReminderWorker>()
            .setInputData(workDataOf("uid" to uid))
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueue(workRequest)

        delay(2000)
        loadReminders()
    }

    fun markWaterDone(plantId: String) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                firestore.collection("plants")
                    .document(plantId)
                    .update("lastWateredDate", today)
                    .await()
                addActivity()
                loadReminders()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error marking water done: ", e)
            }
        }
    }

    fun markFertilizeDone(plantId: String) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                firestore.collection("plants")
                    .document(plantId)
                    .update("lastFertilizedDate", today)
                    .await()
                addActivity()
                loadReminders()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error marking fertilize done: ", e)
            }
        }
    }
//activity counter
    fun addActivity() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val userRef = firestore.collection("users").document(userId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val current = snapshot.getLong("activities") ?: 0
                    transaction.update(userRef, "activities", current + 1)
                }.await()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}