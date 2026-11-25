package com.example.data.repository

import com.example.data.local.AffirmationDao
import com.example.data.local.AffirmationEntity
import com.example.data.local.DatabaseProvider
import com.example.data.local.SacredStepsDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.EspWebhookClient
import com.example.domain.model.Affirmation
import com.example.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class AffirmationRepository(
    private val database: SacredStepsDatabase,
    private val affirmationDao: AffirmationDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val espWebhookClient: EspWebhookClient
) {
    suspend fun ensureSeededAndRecordOpen() {
        DatabaseProvider.ensureFullLibrarySeeded(database)
        preferencesRepository.recordAppOpen()
    }

    val userProgress: Flow<UserProgress> = combine(
        preferencesRepository.preferencesFlow,
        affirmationDao.observeCompletedCount()
    ) { prefs, completedCount ->
        UserProgress(
            isPremiumUnlocked = prefs.isPremiumUnlocked,
            userEmail = prefs.userEmail,
            userName = prefs.userName,
            daysCompleted = completedCount,
            currentDay = prefs.currentDay,
            notificationsEnabled = prefs.notificationsEnabled,
            reminderHour = prefs.reminderHour,
            reminderMinute = prefs.reminderMinute,
            darkModeOverride = prefs.darkModeOverride,
            lastHeartMood = prefs.lastHeartMood,
            isReturningAfterGap = prefs.isReturningAfterGap
        )
    }

    val allAffirmations: Flow<List<Affirmation>> = combine(
        affirmationDao.observeAllAffirmations(),
        preferencesRepository.preferencesFlow
    ) { entities, prefs ->
        entities.map { it.toDomain(isPremiumUnlocked = prefs.isPremiumUnlocked) }
    }

    fun observeAffirmationByDay(dayNumber: Int): Flow<Affirmation?> = combine(
        affirmationDao.observeAffirmationByDay(dayNumber.coerceIn(1, 365)),
        preferencesRepository.preferencesFlow
    ) { entity, prefs ->
        entity?.toDomain(isPremiumUnlocked = prefs.isPremiumUnlocked)
    }

    suspend fun isDayLocked(dayNumber: Int): Boolean {
        if (dayNumber <= 7) return false
        return !preferencesRepository.preferencesFlow.first().isPremiumUnlocked
    }

    suspend fun selectDay(dayNumber: Int) {
        preferencesRepository.setCurrentDay(dayNumber)
    }

    suspend fun setHeartMood(moodLabel: String) {
        preferencesRepository.setHeartMood(moodLabel)
    }

    suspend fun toggleDayCompleted(dayNumber: Int, completed: Boolean) {
        affirmationDao.setDayCompleted(dayNumber, completed)
    }

    suspend fun unlockFullJourney(
        email: String,
        firstName: String,
        espProvider: String = "ConvertKit"
    ): Boolean {
        val synced = espWebhookClient.sendPrayerGuideSignup(
            email = email,
            firstName = firstName,
            espProvider = espProvider
        )
        preferencesRepository.unlockPremium(
            email = email,
            firstName = firstName,
            webhookSynced = synced
        )
        return synced
    }

    suspend fun updateNotificationPreferences(enabled: Boolean, hour: Int, minute: Int) {
        preferencesRepository.setNotificationSettings(enabled, hour, minute)
    }

    suspend fun setDarkModeOverride(override: Boolean?) {
        preferencesRepository.setDarkModeOverride(override)
    }

    private fun AffirmationEntity.toDomain(isPremiumUnlocked: Boolean): Affirmation {
        return Affirmation(
            id = dayNumber,
            dayNumber = dayNumber,
            weekNumber = weekNumber,
            monthNumber = monthNumber,
            monthName = monthName,
            title = title,
            scripture = scripture,
            truthReframe = truthReframe,
            affirmationText = affirmationText,
            practiceAction = practiceAction,
            reflectionPrompt = reflectionPrompt,
            isLocked = dayNumber > 7 && !isPremiumUnlocked,
            isCompleted = isCompleted
        )
    }
}
