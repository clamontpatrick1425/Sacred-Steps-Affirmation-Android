package com.sacredsteps.app.audio.service

import android.content.Context
import com.sacredsteps.app.audio.model.AudioPlaylistManifest
import com.sacredsteps.app.audio.model.DailyPodcast
import com.sacredsteps.app.audio.model.MonthlyPodcast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class PodcastRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var cachedManifest: AudioPlaylistManifest? = null

    suspend fun getManifest(): AudioPlaylistManifest = withContext(Dispatchers.IO) {
        cachedManifest?.let { return@withContext it }
        val inputStream = context.assets.open("sacred_steps_audio_playlist_manifest.json")
        val content = InputStreamReader(inputStream).readText()
        val parsed = json.decodeFromString<AudioPlaylistManifest>(content)
        cachedManifest = parsed
        parsed
    }

    suspend fun getMonthlyPodcasts(): List<MonthlyPodcast> {
        return getManifest().monthlyPodcasts
    }

    suspend fun getDailyPodcasts(monthNumber: Int? = null): List<DailyPodcast> {
        val list = getManifest().dailyPodcasts
        return if (monthNumber != null) {
            list.filter { it.monthNumber == monthNumber }
        } else {
            list
        }
    }

    suspend fun getDayOfYearPodcast(dayOfYear: Int): DailyPodcast? {
        return getManifest().dailyPodcasts.find { it.dayOfYear == dayOfYear }
    }
}
