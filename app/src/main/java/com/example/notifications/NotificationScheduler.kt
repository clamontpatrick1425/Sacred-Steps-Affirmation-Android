package com.example.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelayMillis = (target.timeInMillis - now.timeInMillis).coerceAtLeast(1_000L)

        val request = PeriodicWorkRequestBuilder<DailyGraceWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_WORK_NAME)
    }

    companion object {
        private const val DAILY_WORK_NAME = "sacred_steps_daily_grace_reminder"
    }
}
