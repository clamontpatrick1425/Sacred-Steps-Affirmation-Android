package com.example

import android.app.Application
import com.example.data.local.DatabaseProvider
import com.example.data.local.SacredStepsDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.EspWebhookClient
import com.example.data.repository.AffirmationRepository
import com.example.data.repository.JournalRepository
import com.example.journal.MonthlyJournalRepository
import com.example.notifications.NotificationScheduler
import com.example.podcast.MurfAudioRepository
import com.example.podcast.PodcastRepository

class AppContainer(application: Application) {
    private val database: SacredStepsDatabase by lazy {
        DatabaseProvider.getDatabase(application)
    }

    private val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(application)
    }

    private val espWebhookClient: EspWebhookClient by lazy {
        EspWebhookClient()
    }

    val notificationScheduler: NotificationScheduler by lazy {
        NotificationScheduler(application)
    }

    val affirmationRepository: AffirmationRepository by lazy {
        AffirmationRepository(
            database = database,
            affirmationDao = database.affirmationDao(),
            preferencesRepository = preferencesRepository,
            espWebhookClient = espWebhookClient
        )
    }

    val journalRepository: JournalRepository by lazy {
        JournalRepository(
            journalEntryDao = database.journalEntryDao(),
            monthlyJournalResponseDao = database.monthlyJournalResponseDao(),
            affirmationDao = database.affirmationDao()
        )
    }

    val monthlyJournalRepository: MonthlyJournalRepository by lazy {
        MonthlyJournalRepository(application)
    }

    val podcastRepository: PodcastRepository by lazy {
        PodcastRepository(application)
    }

    val murfAudioRepository: MurfAudioRepository by lazy {
        MurfAudioRepository(application)
    }
}

class SacredStepsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
