package com.example.podcast

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PodcastRepository(
    private val context: Context
) {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Volatile
    private var cachedEpisodes: List<WeeklyPodcastEpisode>? = null

    suspend fun loadAllEpisodes(): List<WeeklyPodcastEpisode> = withContext(Dispatchers.IO) {
        cachedEpisodes?.let { return@withContext it }

        val json = context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
        val adapter = moshi.adapter(WeeklyPodcastCatalog::class.java)
        val parsed = adapter.fromJson(json)?.episodes.orEmpty().sortedBy { it.weekNumber }
        cachedEpisodes = parsed
        parsed
    }

    companion object {
        const val ASSET_FILE_NAME = "weekly_podcasts.json"
    }
}
