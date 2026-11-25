package com.example.domain.model

/**
 * Core 365-day devotional model aligned with the Sacred S.T.E.P. Method:
 * Scripture (Anchor), Truth (Reframe the lie), Embrace (Affirmation), Practice (One small action).
 */
data class Affirmation(
    val id: Int,
    val dayNumber: Int, // 1 to 365
    val weekNumber: Int, // 1 to 52
    val monthNumber: Int, // 1 to 12
    val monthName: String,
    val title: String,
    val scripture: String,
    val truthReframe: String,
    val affirmationText: String,
    val practiceAction: String,
    val reflectionPrompt: String,
    val isLocked: Boolean,
    val isCompleted: Boolean = false
)

data class JournalEntry(
    val id: Int = 0,
    val dayNumber: Int,
    val entryText: String,
    val heartMood: String = "",
    val timestamp: Long
)

data class UserProgress(
    val isPremiumUnlocked: Boolean = false,
    val userEmail: String = "",
    val userName: String = "",
    val daysCompleted: Int = 0,
    val currentDay: Int = com.example.util.SacredCalendar.today().dayOfYear365,
    val notificationsEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val darkModeOverride: Boolean? = null,
    val lastHeartMood: String = "",
    val isReturningAfterGap: Boolean = false
)

enum class HeartMood(val label: String, val gentleResponse: String) {
    AT_PEACE(
        label = "At Peace",
        gentleResponse = "Hold this stillness gently. Let gratitude settle into your bones today."
    ),
    WEARY(
        label = "Weary",
        gentleResponse = "You do not have to be strong right now. Just take the next honest breath."
    ),
    GRATEFUL(
        label = "Grateful",
        gentleResponse = "Look how far grace has carried you. Let that quiet wonder guide your steps."
    ),
    HEAVY(
        label = "Heavy",
        gentleResponse = "I am glad you did not hide how you feel. Grace meets you right inside the weight."
    ),
    HOPEFUL(
        label = "Hopeful",
        gentleResponse = "Even a small flicker of hope is enough light for today's step."
    )
}
