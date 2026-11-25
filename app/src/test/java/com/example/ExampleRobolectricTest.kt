package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AffirmationSeedData
import com.example.data.local.DatabaseProvider
import com.example.data.local.JournalEntryEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.EspWebhookClient
import com.example.data.repository.AffirmationRepository
import com.example.data.repository.JournalRepository
import com.example.journal.MonthlyJournalRepository
import com.example.notifications.NotificationScheduler
import com.example.ui.DailyAffirmationViewModel
import com.example.util.SacredCalendar
import java.util.Calendar
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
                monthlyJournalResponseDao = db.monthlyJournalResponseDao(),
                affirmationDao = db.affirmationDao()
            )
            val monthlyJournalRepo = MonthlyJournalRepository(context)
            val viewModel = DailyAffirmationViewModel(
                affirmationRepository = affirmationRepo,
                journalRepository = journalRepo,
                monthlyJournalRepository = monthlyJournalRepo,
                notificationScheduler = NotificationScheduler(context)
            )

            advanceUntilIdle()

            val todayInfo = SacredCalendar.today()
            assertEquals(todayInfo.dayOfYear365, affirmationRepo.userProgress.first().currentDay)
            assertFalse(viewModel.isDayLocked(1))
            assertFalse(viewModel.isDayLocked(7))
            assertFalse(viewModel.isDayLocked(todayInfo.dayOfYear365))
            val lockedProbeDay = if (todayInfo.weekNumber == 2) 100 else 8
            assertTrue(viewModel.isDayLocked(lockedProbeDay))

            viewModel.saveJournalEntry(1, "Grateful for a quiet start.")
            viewModel.saveMonthlyJournalResponse(1, "day_1_embrace", "I am a new creation in Christ.")
            advanceUntilIdle()

            val savedEntries = journalRepo.observeEntriesForDay(1).first()
            assertTrue(savedEntries.any { it.entryText == "Grateful for a quiet start." })

            val monthlyMap = journalRepo.monthlyJournalResponsesMap.first()
            assertEquals("I am a new creation in Christ.", monthlyMap["1:day_1_embrace"])

            val exportText = journalRepo.buildExportableJournalText()
            assertTrue(exportText.contains("MONTHLY TRANSFORMATION JOURNAL NOTES"))
            assertTrue(exportText.contains("I am a new creation in Christ."))

            affirmationRepo.toggleDayCompleted(1, true)
            advanceUntilIdle()
            val progress = affirmationRepo.userProgress.first()
            assertTrue(progress.daysCompleted >= 1)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `all 12 monthly transformation journals load with January identity content and full themes`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MonthlyJournalRepository(context)
        val journals = repo.loadAllJournals()

        assertEquals(12, journals.size)
        assertEquals(
            listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ),
            journals.map { it.monthName }
        )

        val expectedDayCounts = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        assertEquals(expectedDayCounts, journals.map { it.days.size })
        assertEquals(365, journals.sumOf { it.days.size })

        val affirmationsByMonth = AffirmationSeedData.buildAll365Days()
            .groupBy { it.monthNumber }
            .toSortedMap()
            .values
            .map { it.size }
        assertEquals(expectedDayCounts, affirmationsByMonth)

        val january = journals.first()
        assertEquals("Identity", january.themeTitle)
        assertEquals("Discovering Who God Says I Am", january.subtitle)
        assertEquals(4, january.weeks.size)
        assertEquals(31, january.days.size)
        assertEquals("My Past Does Not Define Me", january.days.first().title)
        assertTrue(january.days.first().scripture.contains("Isaiah 43:18"))
        assertEquals("I Take My Next Sacred Step", january.days[29].title)
        assertTrue(january.days[29].scripture.contains("Psalm 37:23"))
        assertTrue(january.days.last().scripture.contains("Galatians 4:6"))

        assertTrue(journals.all { j ->
            j.welcomeLetter.isNotBlank() &&
                j.themeIntro.isNotBlank() &&
                j.themeScripture.isNotBlank() &&
                j.openingPrayer.isNotBlank() &&
                j.weeks.size == 4 &&
                j.weeks.first().dayStart == 1 &&
                j.weeks.last().dayEnd == j.days.size &&
                j.endOfMonthPrompts.size == 5 &&
                j.declarationAnchors.isNotEmpty() &&
                j.closingPrayer.isNotBlank()
        })
    }

    @Test
    fun `sacred calendar maps real calendar month and weekday accurately across 365 days`() {
        val nov24 = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.NOVEMBER)
            set(Calendar.DAY_OF_MONTH, 24)
        }
        val info = SacredCalendar.today(nov24)
        assertEquals(11, info.monthNumber)
        assertEquals("November", info.monthName)
        assertEquals(24, info.dayOfMonth)
        assertEquals(328, info.dayOfYear365)
        assertEquals(47, info.weekNumber)
        assertEquals("Tuesday", info.weekdayFull)
        assertTrue(SacredCalendar.isDayInFreeWindow(328, info))
        assertFalse(SacredCalendar.isDayInFreeWindow(100, info))

        val roundTrip = SacredCalendar.fromDayOfYear365(328, year = 2026)
        assertEquals("November", roundTrip.monthName)
        assertEquals(24, roundTrip.dayOfMonth)
        assertEquals("Tuesday", roundTrip.weekdayFull)
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
