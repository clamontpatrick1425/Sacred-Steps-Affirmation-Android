package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.podcast.PodcastViewModel
import com.example.ui.DailyAffirmationViewModel
import com.example.ui.SacredStepsApp
import com.example.ui.theme.SacredStepsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as SacredStepsApplication).container

        setContent {
            val viewModel: DailyAffirmationViewModel = viewModel(
                factory = DailyAffirmationViewModel.provideFactory(
                    affirmationRepository = appContainer.affirmationRepository,
                    journalRepository = appContainer.journalRepository,
                    notificationScheduler = appContainer.notificationScheduler
                )
            )
            val podcastViewModel: PodcastViewModel = viewModel(
                factory = PodcastViewModel.provideFactory(
                    podcastRepository = appContainer.podcastRepository,
                    murfAudioRepository = appContainer.murfAudioRepository
                )
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val podcastUiState by podcastViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = uiState.userProgress.darkModeOverride ?: isSystemInDarkTheme()

            SacredStepsTheme(darkTheme = darkTheme) {
                SacredStepsApp(
                    uiState = uiState,
                    viewModel = viewModel,
                    podcastUiState = podcastUiState,
                    podcastViewModel = podcastViewModel
                )
            }
        }
    }
}
