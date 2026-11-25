package com.example.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AffirmationRepository
import com.example.data.repository.JournalRepository
import com.example.domain.model.Affirmation
import com.example.domain.model.HeartMood
import com.example.domain.model.JournalEntry
import com.example.domain.model.UserProgress
import com.example.journal.MonthlyJournalRepository
import com.example.journal.MonthlyTransformationJournal
import com.example.notifications.NotificationScheduler
import com.example.util.SacredCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SacredStepsUiState(
    val isLoading: Boolean = true,
    val affirmations: List<Affirmation> = emptyList(),
    val currentAffirmation: Affirmation? = null,
    val isCurrentDayLocked: Boolean = false,
    val userProgress: UserProgress = UserProgress(),
    val journalEntriesForCurrentDay: List<JournalEntry> = emptyList(),
    val allJournalEntries: List<JournalEntry> = emptyList(),
    val journaledDays: Set<Int> = emptySet(),
    val monthlyJournals: List<MonthlyTransformationJournal> = emptyList(),
    val monthlyJournalResponses: Map<String, String> = emptyMap(),
    val showGroundingSheet: Boolean = false,
    val showUnlockDialog: Boolean = false,
    val pendingUnlockTargetDay: Int? = null,
    val isUnlockSubmitting: Boolean = false,
    val unlockErrorMessage: String? = null,
    val snackbarMessage: String? = null
)

private data class ModalUiState(
    val showGroundingSheet: Boolean = false,
    val showUnlockDialog: Boolean = false,
    val pendingUnlockTargetDay: Int? = null,
    val isUnlockSubmitting: Boolean = false,
    val unlockErrorMessage: String? = null,
    val snackbarMessage: String? = null
)

class DailyAffirmationViewModel(
    private val affirmationRepository: AffirmationRepository,
    private val journalRepository: JournalRepository,
    private val monthlyJournalRepository: MonthlyJournalRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val modalState = MutableStateFlow(ModalUiState())
    private val monthlyJournalsState = MutableStateFlow<List<MonthlyTransformationJournal>>(emptyList())

    init {
        fetchTodayAffirmation()
        loadMonthlyJournals()
    }

    private fun loadMonthlyJournals() {
        viewModelScope.launch {
            runCatching { monthlyJournalRepository.loadAllJournals() }
                .onSuccess { journals -> monthlyJournalsState.value = journals }
        }
    }

    private val coreDataFlow = combine(
        affirmationRepository.allAffirmations,
        affirmationRepository.userProgress,
        journalRepository.allJournalEntries,
        journalRepository.journaledDays,
        journalRepository.monthlyJournalResponsesMap
    ) { affirmations, progress, entries, journaledDays, monthlyResponses ->
        CoreDataSnapshot(
            affirmations = affirmations,
            progress = progress,
            entries = entries,
            journaledDays = journaledDays,
            monthlyResponses = monthlyResponses
        )
    }

    val uiState: StateFlow<SacredStepsUiState> = combine(
        coreDataFlow,
        monthlyJournalsState,
        modalState
    ) { core, monthlyJournals, modals ->
        val selectedDay = core.progress.currentDay.coerceIn(1, 365)
        val currentAffirmation = core.affirmations.find { it.dayNumber == selectedDay }
            ?: core.affirmations.firstOrNull()
        val entriesForDay = core.entries.filter { it.dayNumber == selectedDay }

        SacredStepsUiState(
            isLoading = core.affirmations.isEmpty(),
            affirmations = core.affirmations,
            currentAffirmation = currentAffirmation,
            isCurrentDayLocked = currentAffirmation?.isLocked ?: false,
            userProgress = core.progress.copy(currentDay = selectedDay),
            journalEntriesForCurrentDay = entriesForDay,
            allJournalEntries = core.entries,
            journaledDays = core.journaledDays,
            monthlyJournals = monthlyJournals,
            monthlyJournalResponses = core.monthlyResponses,
            showGroundingSheet = modals.showGroundingSheet,
            showUnlockDialog = modals.showUnlockDialog,
            pendingUnlockTargetDay = modals.pendingUnlockTargetDay,
            isUnlockSubmitting = modals.isUnlockSubmitting,
            unlockErrorMessage = modals.unlockErrorMessage,
            snackbarMessage = modals.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SacredStepsUiState()
    )

    fun fetchTodayAffirmation() {
        viewModelScope.launch {
            affirmationRepository.ensureSeededAndRecordOpen()
        }
    }

    fun isDayLocked(dayNumber: Int): Boolean {
        return !SacredCalendar.isDayInFreeWindow(dayNumber) && !uiState.value.userProgress.isPremiumUnlocked
    }

    fun selectDay(dayNumber: Int, onUnlockedNavigation: (() -> Unit)? = null) {
        val clamped = dayNumber.coerceIn(1, 365)
        viewModelScope.launch {
            affirmationRepository.selectDay(clamped)
            onUnlockedNavigation?.invoke()
        }
    }

    fun selectHeartMood(mood: HeartMood) {
        viewModelScope.launch {
            affirmationRepository.setHeartMood(mood.label)
        }
    }

    fun toggleCurrentDayWalked() {
        val current = uiState.value.currentAffirmation ?: return
        val nextCompleted = !current.isCompleted
        viewModelScope.launch {
            affirmationRepository.toggleDayCompleted(current.dayNumber, nextCompleted)
            val msg = if (nextCompleted) {
                "Day ${current.dayNumber} marked walked in grace."
            } else {
                "Day ${current.dayNumber} unmarked."
            }
            modalState.update { it.copy(snackbarMessage = msg) }
        }
    }

    fun saveJournalEntry(dayNumber: Int, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val mood = uiState.value.userProgress.lastHeartMood
        viewModelScope.launch {
            journalRepository.saveJournalEntry(
                dayNumber = dayNumber,
                entryText = trimmed,
                heartMood = mood
            )
            modalState.update {
                it.copy(snackbarMessage = "Saved safely to your private journal.")
            }
        }
    }

    fun deleteJournalEntry(id: Int) {
        viewModelScope.launch {
            journalRepository.deleteJournalEntry(id)
            modalState.update {
                it.copy(snackbarMessage = "Reflection removed.")
            }
        }
    }

    fun saveMonthlyJournalResponse(monthNumber: Int, entryKey: String, content: String) {
        viewModelScope.launch {
            journalRepository.saveMonthlyJournalResponse(monthNumber, entryKey, content)
            modalState.update {
                it.copy(snackbarMessage = "Journal reflection saved privately.")
            }
        }
    }

    fun openGrounding() {
        modalState.update { it.copy(showGroundingSheet = true) }
    }

    fun dismissGrounding() {
        modalState.update { it.copy(showGroundingSheet = false) }
    }

    fun openUnlockDialog(targetDay: Int? = null) {
        modalState.update {
            it.copy(
                showUnlockDialog = true,
                pendingUnlockTargetDay = targetDay,
                unlockErrorMessage = null
            )
        }
    }

    fun dismissUnlockDialog() {
        modalState.update {
            it.copy(
                showUnlockDialog = false,
                pendingUnlockTargetDay = null,
                unlockErrorMessage = null
            )
        }
    }

    fun submitUnlockEmail(
        email: String,
        firstName: String,
        espProvider: String = "ConvertKit"
    ) {
        val cleanedEmail = email.trim()
        if (cleanedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
            modalState.update {
                it.copy(unlockErrorMessage = "Please enter a valid email address so we can send your Prayer Guide.")
            }
            return
        }

        val targetDay = modalState.value.pendingUnlockTargetDay
        modalState.update { it.copy(isUnlockSubmitting = true, unlockErrorMessage = null) }

        viewModelScope.launch {
            val webhookSynced = affirmationRepository.unlockFullJourney(
                email = cleanedEmail,
                firstName = firstName,
                espProvider = espProvider
            )
            if (targetDay != null && targetDay in 1..365) {
                affirmationRepository.selectDay(targetDay)
            }
            val message = if (webhookSynced) {
                "All 365 days are open via $espProvider. Your 30-Day Redemption Prayer Guide is on its way."
            } else {
                "Welcome to the community. All 365 days are unlocked on this device."
            }
            modalState.update {
                it.copy(
                    isUnlockSubmitting = false,
                    showUnlockDialog = false,
                    pendingUnlockTargetDay = null,
                    snackbarMessage = message
                )
            }
        }
    }

    fun updateNotificationSchedule(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            affirmationRepository.updateNotificationPreferences(enabled, hour, minute)
            if (enabled) {
                notificationScheduler.scheduleDailyReminder(hour, minute)
                modalState.update {
                    it.copy(snackbarMessage = "Daily grace reminder scheduled.")
                }
            } else {
                notificationScheduler.cancelDailyReminder()
                modalState.update {
                    it.copy(snackbarMessage = "Daily reminders paused.")
                }
            }
        }
    }

    fun setDarkModeOverride(override: Boolean?) {
        viewModelScope.launch {
            affirmationRepository.setDarkModeOverride(override)
        }
    }

    fun prepareJournalExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val text = journalRepository.buildExportableJournalText()
            onReady(text)
        }
    }

    fun consumeSnackbar() {
        modalState.update { it.copy(snackbarMessage = null) }
    }

    private data class CoreDataSnapshot(
        val affirmations: List<Affirmation>,
        val progress: UserProgress,
        val entries: List<JournalEntry>,
        val journaledDays: Set<Int>,
        val monthlyResponses: Map<String, String>
    )

    companion object {
        fun provideFactory(
            affirmationRepository: AffirmationRepository,
            journalRepository: JournalRepository,
            monthlyJournalRepository: MonthlyJournalRepository,
            notificationScheduler: NotificationScheduler
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DailyAffirmationViewModel(
                    affirmationRepository = affirmationRepository,
                    journalRepository = journalRepository,
                    monthlyJournalRepository = monthlyJournalRepository,
                    notificationScheduler = notificationScheduler
                ) as T
            }
        }
    }
}
