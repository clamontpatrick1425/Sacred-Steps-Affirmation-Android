package com.sacredsteps.app.audio.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.sacredsteps.app.audio.model.DailyPodcast
import com.sacredsteps.app.audio.model.MonthlyPodcast
import com.sacredsteps.app.audio.service.PodcastPlaybackService
import com.sacredsteps.app.audio.service.PodcastRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val activeTrackTitle: String = "Select an episode",
    val activeTrackSubtitle: String = "Sacred Steps to Redemption",
    val activeArtworkUrl: String = "",
    val activeDailyDetails: DailyPodcast? = null,
    val activeMonthlyDetails: MonthlyPodcast? = null
)

class PodcastViewModel(
    private val context: Context,
    private val repository: PodcastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val mediaController: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    init {
        initMediaController()
        startProgressTracker()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, PodcastPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                mediaController?.let { player ->
                    _uiState.value = _uiState.value.copy(
                        durationMs = player.duration.coerceAtLeast(0L),
                        currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                    )
                }
            }
        })
    }

    fun playDailyPodcast(podcast: DailyPodcast) {
        val player = mediaController ?: return
        val mediaItem = podcast.toMediaItem()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _uiState.value = _uiState.value.copy(
            activeTrackTitle = podcast.title,
            activeTrackSubtitle = "${podcast.dateLabel} • ${podcast.scriptureReference}",
            activeArtworkUrl = podcast.artworkAsset,
            activeDailyDetails = podcast,
            activeMonthlyDetails = null
        )
    }

    fun playMonthlyPodcast(monthly: MonthlyPodcast) {
        val player = mediaController ?: return
        val mediaItem = monthly.toMediaItem()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _uiState.value = _uiState.value.copy(
            activeTrackTitle = monthly.title,
            activeTrackSubtitle = "${monthly.monthName} • ${monthly.anchorScripture}",
            activeArtworkUrl = monthly.artworkAsset,
            activeDailyDetails = null,
            activeMonthlyDetails = monthly
        )
    }

    fun togglePlayPause() {
        val player = mediaController ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun skipForward15() {
        mediaController?.seekForward()
    }

    fun skipBack15() {
        mediaController?.seekBack()
    }

    fun setSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    private fun startProgressTracker() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { player ->
                    if (player.isPlaying) {
                        _uiState.value = _uiState.value.copy(
                            currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                            durationMs = player.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500)
            }
        }
    }

    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
