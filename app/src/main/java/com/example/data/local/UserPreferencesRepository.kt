package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.util.SacredCalendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sacred_steps_prefs")

data class StoredPreferences(
    val isPremiumUnlocked: Boolean = false,
    val userEmail: String = "",
    val userName: String = "",
    val currentDay: Int = SacredCalendar.today().dayOfYear365,
    val notificationsEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val darkModeOverride: Boolean? = null,
    val lastHeartMood: String = "",
    val lastOpenedEpochDay: Long = 0L,
    val isReturningAfterGap: Boolean = false,
    val pendingWebhookSync: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_PREMIUM_UNLOCKED = booleanPreferencesKey("is_premium_unlocked")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val CURRENT_DAY = intPreferencesKey("current_day")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val DARK_MODE_OVERRIDE = stringPreferencesKey("dark_mode_override")
        val LAST_HEART_MOOD = stringPreferencesKey("last_heart_mood")
        val LAST_OPENED_EPOCH_DAY = longPreferencesKey("last_opened_epoch_day")
        val RETURNING_AFTER_GAP = booleanPreferencesKey("returning_after_gap")
        val PENDING_WEBHOOK_SYNC = booleanPreferencesKey("pending_webhook_sync")
    }

    val preferencesFlow: Flow<StoredPreferences> = context.dataStore.data.map { prefs ->
        val darkOverride = when (prefs[Keys.DARK_MODE_OVERRIDE]) {
            "dark" -> true
            "light" -> false
            else -> null
        }
        StoredPreferences(
            isPremiumUnlocked = prefs[Keys.IS_PREMIUM_UNLOCKED] ?: false,
            userEmail = prefs[Keys.USER_EMAIL] ?: "",
            userName = prefs[Keys.USER_NAME] ?: "",
            currentDay = (prefs[Keys.CURRENT_DAY] ?: SacredCalendar.today().dayOfYear365).coerceIn(1, 365),
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 8,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
            darkModeOverride = darkOverride,
            lastHeartMood = prefs[Keys.LAST_HEART_MOOD] ?: "",
            lastOpenedEpochDay = prefs[Keys.LAST_OPENED_EPOCH_DAY] ?: 0L,
            isReturningAfterGap = prefs[Keys.RETURNING_AFTER_GAP] ?: false,
            pendingWebhookSync = prefs[Keys.PENDING_WEBHOOK_SYNC] ?: false
        )
    }

    suspend fun recordAppOpen() {
        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val todayCalendarDay = SacredCalendar.today().dayOfYear365
        context.dataStore.edit { prefs ->
            val previous = prefs[Keys.LAST_OPENED_EPOCH_DAY] ?: 0L
            val returningAfterGap = previous > 0L && (todayEpochDay - previous) > 1L
            prefs[Keys.RETURNING_AFTER_GAP] = returningAfterGap
            prefs[Keys.LAST_OPENED_EPOCH_DAY] = todayEpochDay
            prefs[Keys.CURRENT_DAY] = todayCalendarDay
        }
    }

    suspend fun setCurrentDay(dayNumber: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_DAY] = dayNumber.coerceIn(1, 365)
        }
    }

    suspend fun unlockPremium(email: String, firstName: String, webhookSynced: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM_UNLOCKED] = true
            prefs[Keys.USER_EMAIL] = email.trim()
            prefs[Keys.USER_NAME] = firstName.trim()
            prefs[Keys.PENDING_WEBHOOK_SYNC] = !webhookSynced
        }
    }

    suspend fun setHeartMood(moodLabel: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_HEART_MOOD] = moodLabel
        }
    }

    suspend fun setNotificationSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
            prefs[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setDarkModeOverride(override: Boolean?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE_OVERRIDE] = when (override) {
                true -> "dark"
                false -> "light"
                null -> "system"
            }
        }
    }
}
