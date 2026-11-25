package com.example.podcast

import android.media.AudioAttributes
import android.media.MediaPlayer
import java.io.File

/**
 * Lightweight sequential MP3 player for cached Murf.ai podcast segments.
 * Handles clean lifecycle transitions without leaking MediaPlayer instances.
 */
class PodcastAudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var prepared: Boolean = false

    fun playFile(
        file: File,
        onPrepared: (durationMs: Int) -> Unit = {},
        onCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        release()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    prepared = true
                    onPrepared(mp.duration.coerceAtLeast(1))
                    mp.start()
                }
                setOnCompletionListener {
                    onCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    onError("Audio playback error ($what / $extra)")
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            release()
            onError(e.message ?: "Unable to play podcast audio.")
        }
    }

    fun pause() {
        runCatching {
            val mp = mediaPlayer
            if (mp != null && prepared && mp.isPlaying) {
                mp.pause()
            }
        }
    }

    fun resume() {
        runCatching {
            val mp = mediaPlayer
            if (mp != null && prepared && !mp.isPlaying) {
                mp.start()
            }
        }
    }

    fun currentPositionMs(): Int {
        return runCatching {
            val mp = mediaPlayer
            if (mp != null && prepared) mp.currentPosition else 0
        }.getOrDefault(0)
    }

    fun durationMs(): Int {
        return runCatching {
            val mp = mediaPlayer
            if (mp != null && prepared) mp.duration.coerceAtLeast(1) else 1
        }.getOrDefault(1)
    }

    fun isPlaying(): Boolean {
        return runCatching {
            val mp = mediaPlayer
            mp != null && prepared && mp.isPlaying
        }.getOrDefault(false)
    }

    fun release() {
        prepared = false
        val mp = mediaPlayer
        mediaPlayer = null
        if (mp != null) {
            runCatching { mp.stop() }
            runCatching { mp.reset() }
            runCatching { mp.release() }
        }
    }
}
