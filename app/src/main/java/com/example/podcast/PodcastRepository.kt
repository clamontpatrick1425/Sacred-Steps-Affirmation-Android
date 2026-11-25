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
    private var cachedManifest: AudioPlaylistManifest? = null

    suspend fun loadManifest(): AudioPlaylistManifest = withContext(Dispatchers.IO) {
        cachedManifest?.let { return@withContext it }

        val json = context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
        val adapter = moshi.adapter(AudioPlaylistManifest::class.java)
        val parsed = adapter.fromJson(json)
            ?: throw IllegalStateException("Failed to parse $ASSET_FILE_NAME")
        val normalized = parsed.copy(
            monthlyPlaylists = parsed.monthlyPlaylists.sortedBy { it.monthNumber }
        )
        cachedManifest = normalized
        normalized
    }

    companion object {
        const val ASSET_FILE_NAME = "sacred_steps_audio_playlist_manifest.json"
    }
}
