package com.blackout.app.domain.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blackout.app.data.datastore.SettingsDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        return try {
            performWork()
        } catch (e: Exception) {
            android.util.Log.e("ReminderWorker", "Fatal error during background work", e)
            androidx.work.ListenableWorker.Result.retry()
        }
    }

    private suspend fun performWork(): androidx.work.ListenableWorker.Result {
        val notificationManager = applicationContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val areNotificationsEnabled = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else true

        if (!areNotificationsEnabled) {
            android.util.Log.w("ReminderWorker", "System-level notifications are DISABLED for this app")
        }

        if (!settingsDataStore.notificationsEnabled.first()) {
            android.util.Log.d("ReminderWorker", "App-level notifications disabled in settings, skipping")
            return androidx.work.ListenableWorker.Result.success()
        }

        // --- 1. Daily reminder TODO add logic to only send for days that have tasks ---
        // Always fires when the worker runs, independent of streak/goal status.
        android.util.Log.d("ReminderWorker", "Sending daily reminder")
        NotificationHelper.showDailyReminderNotification(applicationContext)

        // TODO Quote Notification Logic

        return androidx.work.ListenableWorker.Result.success()
    }
}