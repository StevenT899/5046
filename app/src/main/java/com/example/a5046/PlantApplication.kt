package com.example.a5046

import android.app.Application
import androidx.work.*
import com.example.a5046.viewmodel.PlantReminderWorker
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import java.util.Calendar

class PlantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

//    fun setupWorkManager() {
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//        // Calculate delay until next midnight
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_YEAR, 1)
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        val delay = calendar.timeInMillis - System.currentTimeMillis()
//
//        // Get current user's uid
//        val uid = FirebaseAuth.getInstance().currentUser?.uid
//        if (uid != null) {
//            val inputData = workDataOf("uid" to uid)
//
//            val reminderWorkRequest = PeriodicWorkRequestBuilder<PlantReminderWorker>(
//                  1, TimeUnit.DAYS
//            )
//                .setConstraints(constraints)
//                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
//                .setInputData(inputData)
//                .build()
//
//            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//                "plant_reminder_work",
//                ExistingPeriodicWorkPolicy.KEEP,
//                reminderWorkRequest
//            )
//        }
//    }


    fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val inputData = workDataOf("uid" to uid)

            // Change to OneTimeWorkRequest: Execute the task immediately once
            val testWorkRequest = OneTimeWorkRequestBuilder<PlantReminderWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(this).enqueue(testWorkRequest)
        }
    }
} 