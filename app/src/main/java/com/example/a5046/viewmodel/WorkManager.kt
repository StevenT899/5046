// WorkManager.kt
package com.example.a5046.viewmodel

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlantReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

    override suspend fun doWork(): Result {
        val userId = inputData.getString("uid") ?: return Result.failure()
        val today = LocalDate.now()

        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("plants")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val name = doc.getString("name") ?: continue
                val lastWateredStr = doc.getString("lastWateredDate")
                val lastFertilizedStr = doc.getString("lastFertilizedDate")
                val waterInterval = doc.getString("wateringFrequency")?.toIntOrNull()
                val fertilizeInterval = doc.getString("fertilizingFrequency")?.toIntOrNull()



                val needWater = if (!lastWateredStr.isNullOrEmpty() && waterInterval != null) {
                    val lastWatered = LocalDate.parse(lastWateredStr, formatter)
                    !lastWatered.plusDays(waterInterval.toLong()).isAfter(today)
                } else false

                val needFertilize = if (!lastFertilizedStr.isNullOrEmpty() && fertilizeInterval != null) {
                    val lastFertilized = LocalDate.parse(lastFertilizedStr, formatter)
                    !lastFertilized.plusDays(fertilizeInterval.toLong()).isAfter(today)
                } else false

                if (needWater || needFertilize) {
                    val reminderDocRef = firestore.collection("users")
                        .document(userId)
                        .collection("plantReminders")
                        .document(doc.id)

                    val existingReminder = reminderDocRef.get().await()
                    val currentWater = existingReminder.getBoolean("needWater")
                    val currentFertilize = existingReminder.getBoolean("needFertilize")

                    if (currentWater != needWater || currentFertilize != needFertilize) {
                        Log.d("ReminderWorker", "Updated reminder for [$name]: Water=$needWater, Fertilize=$needFertilize")

                        reminderDocRef.set(
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
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Error: ${e.message}", e)
            return Result.failure()
        }
    }
}
