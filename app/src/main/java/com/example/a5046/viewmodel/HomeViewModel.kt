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

data class UserData(val name: String = "", val level: String = "")
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

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState

    private val _address = MutableStateFlow("Loading...")
    val address: StateFlow<String> = _address

    private val _reminders = MutableStateFlow<List<ReminderItem>>(emptyList())
    val reminders: StateFlow<List<ReminderItem>> = _reminders

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

    fun loadReminders() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        val snapshot = firestore.collection("users").document(uid).collection("plantReminders").get().await()
        _reminders.value = snapshot.documents.mapNotNull { doc ->
            val plantName = doc.getString("plantName") ?: return@mapNotNull null
            val needWater = doc.getBoolean("needWater") ?: false
            val needFertilize = doc.getBoolean("needFertilize") ?: false
            val isDone = doc.getBoolean("isDone") ?: false
            ReminderItem(doc.id, plantName, needWater, needFertilize, isDone)
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

        val now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val plantRef = firestore.collection("users").document(uid).collection("plants").document(reminder.id)
        if (reminder.needWater) plantRef.update("lastWatered", now)
        if (reminder.needFertilize) plantRef.update("lastFertilized", now)

        loadReminders()
    }


    fun debugRunReminderCheck() = viewModelScope.launch {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val snapshot = FirebaseFirestore.getInstance()
            .collection("plants")
            .whereEqualTo("userId", uid)
            .get()
            .await()

        for (doc in snapshot.documents) {
            val name = doc.getString("name") ?: continue
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
                val reminderDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("plantReminders")
                    .document(doc.id)

                reminderDoc.set(
                    mapOf(
                        "plantName" to name,
                        "needWater" to needWater,
                        "needFertilize" to needFertilize,
                        "isDone" to false,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
        }

        loadReminders()
    }

}