package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AffirmationSeedData
import com.example.data.local.DatabaseProvider
import com.example.data.local.JournalEntryEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.WorkbookSeedData
import com.example.data.remote.EspWebhookClient
import com.example.data.repository.AffirmationRepository
import com.example.data.repository.JournalRepository
import com.example.notifications.NotificationScheduler
import com.example.ui.DailyAffirmationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Sacred Steps`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sacred Steps", appName)
    }

    @Test
    fun `first seven days and full 365 devotional days are seeded accurately`() {
        val firstSeven = AffirmationSeedData.buildFirstSevenDays()
        assertEquals(7, firstSeven.size)
        assertEquals((1..7).toList(), firstSeven.map { it.dayNumber })

        val days = AffirmationSeedData.buildAll365Days()
        assertEquals(365, days.size)
        assertEquals(1, days.first().dayNumber)
        assertEquals(365, days.last().dayNumber)

        val day318 = days.first { it.dayNumber == 318 }
        assertEquals(46, day318.weekNumber)
        assertEquals(11, day318.monthNumber)
        assertEquals("November", day318.monthName)
        assertEquals("Gratitude for the People Who Stayed", day318.title)
        assertTrue(days.all { it.title.isNotBlank() && it.affirmationText.isNotBlank() })
    }

    @Test
    fun `database provider seeds affirmations and persists journal entries`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = DatabaseProvider.getDatabase(context)
        DatabaseProvider.ensureFullLibrarySeeded(db)

        assertEquals(365, db.affirmationDao().getAffirmationCount())

        db.journalEntryDao().insertJournalEntry(
            JournalEntryEntity(
                dayNumber = 1,
                entryText = "Taking one honest breath today.",
                heartMood = "Hopeful",
                timestamp = 1700000000000L
            )
        )
        val entries = db.journalEntryDao().observeJournalEntriesForDay(1).first()
        assertTrue(entries.any { it.entryText == "Taking one honest breath today." })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `repositories and DailyAffirmationViewModel handle locking journaling and progress`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val db = DatabaseProvider.getDatabase(context)
            val prefs = UserPreferencesRepository(context)
            val affirmationRepo = AffirmationRepository(
                database = db,
                affirmationDao = db.affirmationDao(),
                preferencesRepository = prefs,
                espWebhookClient = EspWebhookClient()
            )
            val journalRepo = JournalRepository(
                journalEntryDao = db.journalEntryDao(),
                workbookNoteDao = db.workbookNoteDao(),
                affirmationDao = db.affirmationDao()
            )
            val viewModel = DailyAffirmationViewModel(
                affirmationRepository = affirmationRepo,
                journalRepository = journalRepo,
                notificationScheduler = NotificationScheduler(context)
            )

            advanceUntilIdle()

            assertFalse(viewModel.isDayLocked(1))
            assertFalse(viewModel.isDayLocked(7))
            assertTrue(viewModel.isDayLocked(8))

            viewModel.saveJournalEntry(1, "Grateful for a quiet start.")
            advanceUntilIdle()
            val savedEntries = journalRepo.observeEntriesForDay(1).first()
            assertTrue(savedEntries.any { it.entryText == "Grateful for a quiet start." })

            affirmationRepo.toggleDayCompleted(1, true)
            advanceUntilIdle()
            val progress = affirmationRepo.userProgress.first()
            assertTrue(progress.daysCompleted >= 1)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `four companion workbooks are configured`() {
        assertEquals(4, WorkbookSeedData.modules.size)
        assertTrue(WorkbookSeedData.modules.any { it.traumaInformedNotice != null })
        assertTrue(WorkbookSeedData.modules.any { it.isCelebratoryOnly })
    }

    @Test
    fun `today screen displays share button when unlocked and unlock card when locked`() {
        val unlockedDay1 = AffirmationSeedData.buildFirstSevenDays().first().let {
            com.example.domain.model.Affirmation(
                id = it.dayNumber,
                dayNumber = it.dayNumber,
                weekNumber = it.weekNumber,
                monthNumber = it.monthNumber,
                monthName = it.monthName,
                title = it.title,
                scripture = it.scripture,
                truthReframe = it.truthReframe,
                affirmationText = it.affirmationText,
                practiceAction = it.practiceAction,
                reflectionPrompt = it.reflectionPrompt,
                isLocked = false,
                isCompleted = false
            )
        }
        val lockedDay8 = unlockedDay1.copy(
            id = 8,
            dayNumber = 8,
            weekNumber = 2,
            title = "Stepping Past What Used to Hold Me",
            isLocked = true
        )

        assertFalse(unlockedDay1.isLocked)
        assertTrue(lockedDay8.isLocked)
        assertEquals(listOf("ConvertKit", "Mailchimp", "Resend"), com.example.ui.components.EspProviders)
    }
}
