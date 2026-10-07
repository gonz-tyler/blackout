package com.blackout.app.domain.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {
    private const val WORK_NAME = "daily_reminder_work"

    fun scheduleDailyReminder(context: Context, time: String) {
        val parts = time.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("burnout_reminder")
            .build()

        android.util.Log.d("WorkManagerScheduler", "Scheduling daily reminder for $time. Initial delay: ${initialDelay / 1000}s")

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    fun updateNotificationSchedule(context: Context, enabled: Boolean, time: String) {
        android.util.Log.d("WorkManagerScheduler", "Updating notification schedule: enabled=$enabled, time=$time")
        if (enabled) {
            scheduleDailyReminder(context, time)
        } else {
            cancelDailyReminder(context)
        }
    }

    fun cancelDailyReminder(context: Context) {
        android.util.Log.d("WorkManagerScheduler", "Canceling daily reminder")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun runReminderCheckNow(context: Context) {
        android.util.Log.d("WorkManagerScheduler", "Manually triggering reminder logic check")
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<ReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
