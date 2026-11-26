package com.example.podcast

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.util.SacredCalendar
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

data class PodcastUiState(
    val isLoadingCatalog: Boolean = true,
    val manifestInfo: ManifestInfo? = null,
    val monthlyPlaylists: List<MonthlyAudioPlaylist> = emptyList(),
    val selectedMonthNumber: Int = SacredCalendar.today().monthNumber,
    val todayDayOfYear: Int = SacredCalendar.today().dayOfYear365,
    val selectedTrack: PlayablePodcastTrack? = null,
    val isFullPlayerExpanded: Boolean = false,
    val dialogueTurns: List<PodcastDialogueTurn> = emptyList(),
    val activeTurnIndex: Int = 0,
    val playbackStatus: PodcastPlaybackStatus = PodcastPlaybackStatus.IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val activePresenter: PodcastHost = PodcastHost.forDayOfYear(SacredCalendar.today().dayOfYear365),
    val cachedTurnIndices: Set<Int> = emptySet(),
    val isPreparingTrack: Boolean = false,
    val prepareProgress: Float = 0f,
    val turnProgressFraction: Float = 0f,
    val isMurfConfigured: Boolean = false,
    val errorMessage: String? = null,
    val statusBanner: String? = null
) {
    val selectedMonthPlaylist: MonthlyAudioPlaylist?
        get() = monthlyPlaylists.firstOrNull { it.monthNumber == selectedMonthNumber }
            ?: monthlyPlaylists.firstOrNull()

    val todayDailyTrack: PlayablePodcastTrack.DailyEpisode?
        get() {
            for (playlist in monthlyPlaylists) {
                val match = playlist.dailyPodcasts.firstOrNull { it.dayOfYear == todayDayOfYear }
                if (match != null) {
                    return PlayablePodcastTrack.DailyEpisode(playlist = playlist, daily = match)
                }
            }
            val firstMonth = monthlyPlaylists.firstOrNull() ?: return null
            val firstDay = firstMonth.dailyPodcasts.firstOrNull() ?: return null
            return PlayablePodcastTrack.DailyEpisode(playlist = firstMonth, daily = firstDay)
        }

    val activeTurn: PodcastDialogueTurn?
        get() = dialogueTurns.getOrNull(activeTurnIndex)
}

class PodcastViewModel(
    private val appContext: Context,
    private val podcastRepository: PodcastRepository,
    private val murfAudioRepository: MurfAudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PodcastUiState(
            isMurfConfigured = murfAudioRepository.isApiKeyConfigured()
        )
    )
    val uiState: StateFlow<PodcastUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controllerDeferred = CompletableDeferred<MediaController>()
    private var activeMediaController: MediaController? = null
    private var playingBundledFullAudio: Boolean = false
    private var activePlaySessionId: Long = 0L

    private var playbackJob: Job? = null
    private var prefetchJob: Job? = null
    private var progressJob: Job? = null
    private var prepareAllJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val current = _uiState.value
            if (current.selectedTrack == null) return
            if (isPlaying) {
                val dur = activeMediaController?.duration?.coerceAtLeast(0L) ?: current.durationMs
                val pos = activeMediaController?.currentPosition?.coerceAtLeast(0L) ?: current.currentPositionMs
                _uiState.update {
                    it.copy(
                        playbackStatus = PodcastPlaybackStatus.PLAYING,
                        durationMs = dur,
                        currentPositionMs = pos,
                        turnProgressFraction = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f,
                        errorMessage = null
                    )
                }
                startProgressTicker()
            } else if (current.playbackStatus == PodcastPlaybackStatus.PLAYING) {
                val state = activeMediaController?.playbackState
                if (state != Player.STATE_ENDED && state != Player.STATE_IDLE) {
                    progressJob?.cancel()
                    _uiState.update { it.copy(playbackStatus = PodcastPlaybackStatus.PAUSED) }
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val controller = activeMediaController ?: return
            val dur = controller.duration.coerceAtLeast(0L)
            val pos = controller.currentPosition.coerceAtLeast(0L)

            when (playbackState) {
                Player.STATE_READY -> {
                    _uiState.update {
                        it.copy(
                            durationMs = dur,
                            currentPositionMs = pos,
                            turnProgressFraction = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    progressJob?.cancel()
                    handleSegmentCompleted()
                }
                else -> Unit
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            progressJob?.cancel()
            _uiState.update {
                it.copy(
                    playbackStatus = PodcastPlaybackStatus.ERROR,
                    errorMessage = error.localizedMessage ?: "Audio playback error."
                )
            }
        }
    }

    init {
        initMediaController()
        loadAudioManifest()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, PodcastPlaybackService::class.java)
        )
        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { controller ->
                        activeMediaController = controller
                        controller.addListener(playerListener)
                        controller.setPlaybackSpeed(_uiState.value.playbackSpeed)
                        if (!controllerDeferred.isCompleted) {
                            controllerDeferred.complete(controller)
                        }
                    }
            },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    private suspend fun awaitMediaController(): MediaController? {
        activeMediaController?.let { return it }
        return withTimeoutOrNull(4_000L) {
            controllerDeferred.await()
        }
    }

    private fun loadAudioManifest() {
        viewModelScope.launch {
            runCatching { podcastRepository.loadManifest() }
                .onSuccess { manifest ->
                    val today = SacredCalendar.today()
                    _uiState.update {
                        it.copy(
                            isLoadingCatalog = false,
                            manifestInfo = manifest.manifestInfo,
                            monthlyPlaylists = manifest.monthlyPlaylists,
                            selectedMonthNumber = today.monthNumber,
                            todayDayOfYear = today.dayOfYear365,
                            activePresenter = PodcastHost.forDayOfYear(today.dayOfYear365),
                            isMurfConfigured = murfAudioRepository.isApiKeyConfigured(),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingCatalog = false,
                            errorMessage = err.message ?: "Unable to load audio playlist manifest."
                        )
                    }
                }
        }
    }

    fun selectMonth(monthNumber: Int) {
        _uiState.update { it.copy(selectedMonthNumber = monthNumber.coerceIn(1, 12)) }
    }

    fun openTrack(track: PlayablePodcastTrack, autoPlay: Boolean = false) {
        val current = _uiState.value
        if (current.selectedTrack?.trackId == track.trackId && current.dialogueTurns.isNotEmpty()) {
            _uiState.update { it.copy(isFullPlayerExpanded = true) }
            if (autoPlay && current.playbackStatus != PodcastPlaybackStatus.PLAYING &&
                current.playbackStatus != PodcastPlaybackStatus.SYNTHESIZING
            ) {
                togglePlayPause()
            }
            return
        }

        stopPlaybackInternal()
        prepareAllJob?.cancel()

        // Automatically select this episode's dedicated presenter from the 5-voice rotation
        val assignedPresenter = PodcastHost.presenterForTrack(track)
        val turns = PodcastScriptBuilder.buildDialogueTurns(track, assignedPresenter)
        val cached = murfAudioRepository.getCachedTurnIndices(track.trackId, turns)

        _uiState.update {
            it.copy(
                selectedTrack = track,
                isFullPlayerExpanded = true,
                activePresenter = assignedPresenter,
                dialogueTurns = turns,
                activeTurnIndex = 0,
                playbackStatus = PodcastPlaybackStatus.IDLE,
                currentPositionMs = 0L,
                durationMs = 0L,
                cachedTurnIndices = cached,
                isPreparingTrack = false,
                prepareProgress = 0f,
                turnProgressFraction = 0f,
                errorMessage = null,
                statusBanner = null
            )
        }

        if (autoPlay) {
            playTurn(0)
        }
    }

    /**
     * Minimizes the full player sheet into the persistent MiniPlayerBar while audio continues
     * playing cleanly in the background via PodcastPlaybackService.
     */
    fun minimizePlayer() {
        _uiState.update { it.copy(isFullPlayerExpanded = false) }
    }

    fun expandPlayer() {
        if (_uiState.value.selectedTrack != null) {
            _uiState.update { it.copy(isFullPlayerExpanded = true) }
        }
    }

    /**
     * Stops playback completely and dismisses both the full player and the MiniPlayerBar.
     */
    fun dismissPlayer() {
        stopPlaybackInternal()
        prepareAllJob?.cancel()
        _uiState.update {
            it.copy(
                selectedTrack = null,
                isFullPlayerExpanded = false,
                dialogueTurns = emptyList(),
                activeTurnIndex = 0,
                playbackStatus = PodcastPlaybackStatus.IDLE,
                currentPositionMs = 0L,
                durationMs = 0L,
                isPreparingTrack = false,
                prepareProgress = 0f,
                turnProgressFraction = 0f,
                errorMessage = null,
                statusBanner = null
            )
        }
    }

    /**
     * Lets the listener cycle the single presenter on the current episode across the
     * top 5 Murf studio voices (Natalie -> Terrell -> Julia -> Miles -> Amara).
     */
    fun cyclePresenterForCurrentTrack() {
        val nextPresenter = _uiState.value.activePresenter.next()
        val track = _uiState.value.selectedTrack ?: return
        val wasPlaying = _uiState.value.playbackStatus == PodcastPlaybackStatus.PLAYING ||
            _uiState.value.playbackStatus == PodcastPlaybackStatus.SYNTHESIZING
        val currentIdx = _uiState.value.activeTurnIndex

        stopPlaybackInternal()
        prepareAllJob?.cancel()

        val rebuiltTurns = PodcastScriptBuilder.buildDialogueTurns(track, nextPresenter)
        val cached = murfAudioRepository.getCachedTurnIndices(track.trackId, rebuiltTurns)
        _uiState.update {
            it.copy(
                activePresenter = nextPresenter,
                dialogueTurns = rebuiltTurns,
                cachedTurnIndices = cached,
                playbackStatus = PodcastPlaybackStatus.IDLE,
                currentPositionMs = 0L,
                durationMs = 0L,
                isPreparingTrack = false,
                prepareProgress = 0f,
                turnProgressFraction = 0f,
                statusBanner = "Presenter switched to ${nextPresenter.displayName} (${nextPresenter.roleLabel})."
            )
        }
        if (wasPlaying) {
            playTurn(currentIdx)
        }
    }

    fun togglePlayPause() {
        val state = _uiState.value
        val controller = activeMediaController
        when (state.playbackStatus) {
            PodcastPlaybackStatus.PLAYING -> {
                controller?.pause()
                progressJob?.cancel()
                _uiState.update { it.copy(playbackStatus = PodcastPlaybackStatus.PAUSED) }
            }
            PodcastPlaybackStatus.PAUSED -> {
                if (controller != null && controller.mediaItemCount > 0) {
                    controller.setPlaybackSpeed(state.playbackSpeed)
                    controller.play()
                    _uiState.update { it.copy(playbackStatus = PodcastPlaybackStatus.PLAYING) }
                    startProgressTicker()
                } else {
                    playTurn(state.activeTurnIndex)
                }
            }
            PodcastPlaybackStatus.SYNTHESIZING -> {
                stopPlaybackInternal()
                _uiState.update { it.copy(playbackStatus = PodcastPlaybackStatus.IDLE) }
            }
            PodcastPlaybackStatus.IDLE,
            PodcastPlaybackStatus.ERROR -> {
                playTurn(state.activeTurnIndex)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = activeMediaController ?: return
        val dur = controller.duration.coerceAtLeast(_uiState.value.durationMs).coerceAtLeast(1L)
        val target = positionMs.coerceIn(0L, dur)
        controller.seekTo(target)
        _uiState.update {
            it.copy(
                currentPositionMs = target,
                turnProgressFraction = (target.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            )
        }
    }

    fun skipForward15() {
        val controller = activeMediaController ?: return
        controller.seekForward()
        syncPositionFromController(controller)
    }

    fun skipBack15() {
        val controller = activeMediaController ?: return
        controller.seekBack()
        syncPositionFromController(controller)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        activeMediaController?.setPlaybackSpeed(clamped)
        _uiState.update { it.copy(playbackSpeed = clamped) }
    }

    fun playNextTurn() {
        val state = _uiState.value
        val nextIndex = state.activeTurnIndex + 1
        if (nextIndex < state.dialogueTurns.size) {
            playTurn(nextIndex)
        } else {
            stopPlaybackInternal()
            _uiState.update {
                it.copy(
                    playbackStatus = PodcastPlaybackStatus.IDLE,
                    turnProgressFraction = 1f,
                    statusBanner = "Episode complete. Take the next step in grace."
                )
            }
        }
    }

    fun playPreviousTurn() {
        val prevIndex = (_uiState.value.activeTurnIndex - 1).coerceAtLeast(0)
        playTurn(prevIndex)
    }

    fun playTurn(turnIndex: Int) {
        val state = _uiState.value
        val track = state.selectedTrack ?: return
        val turn = state.dialogueTurns.getOrNull(turnIndex) ?: return

        stopPlaybackInternal()
        val sessionId = ++activePlaySessionId
        playingBundledFullAudio = false

        _uiState.update {
            it.copy(
                activeTurnIndex = turnIndex,
                playbackStatus = PodcastPlaybackStatus.SYNTHESIZING,
                currentPositionMs = 0L,
                durationMs = 0L,
                turnProgressFraction = 0f,
                errorMessage = null,
                statusBanner = null
            )
        }

        playbackJob = viewModelScope.launch {
            val basePath = state.manifestInfo?.specs?.audioBasePath ?: "media/audio/v1/"
            val bundledFullAudio = if (turnIndex == 0) {
                murfAudioRepository.resolveBundledTrackAsset(
                    audioBasePath = basePath,
                    relativeAudioFile = track.audioFile,
                    trackId = track.trackId
                )
            } else {
                null
            }

            if (bundledFullAudio != null) {
                if (sessionId != activePlaySessionId) return@launch
                playingBundledFullAudio = true
                _uiState.update {
                    it.copy(cachedTurnIndices = state.dialogueTurns.indices.toSet())
                }
                startMedia3Playback(
                    file = bundledFullAudio,
                    track = track,
                    turn = turn,
                    sessionId = sessionId
                )
                return@launch
            }

            val result = murfAudioRepository.getOrSynthesizeTurn(track.trackId, turn)
            if (sessionId != activePlaySessionId) return@launch

            result.onSuccess { audioFile ->
                _uiState.update {
                    it.copy(cachedTurnIndices = it.cachedTurnIndices + turn.index)
                }

                // Prefetch the next chapter in the background for seamless transitions
                prefetchNextTurn(track.trackId, state.dialogueTurns, turnIndex + 1)

                startMedia3Playback(
                    file = audioFile,
                    track = track,
                    turn = turn,
                    sessionId = sessionId
                )
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        playbackStatus = PodcastPlaybackStatus.ERROR,
                        errorMessage = err.message ?: "Unable to synthesize Murf.ai voice audio."
                    )
                }
            }
        }
    }

    private suspend fun startMedia3Playback(
        file: File,
        track: PlayablePodcastTrack,
        turn: PodcastDialogueTurn,
        sessionId: Long
    ) {
        val controller = awaitMediaController()
        if (sessionId != activePlaySessionId) return

        if (controller == null) {
            _uiState.update {
                it.copy(
                    playbackStatus = PodcastPlaybackStatus.ERROR,
                    errorMessage = "Audio playback service is still starting. Tap Play to try again."
                )
            }
            return
        }

        val mediaItem = MediaItem.Builder()
            .setMediaId("${track.trackId}_ch_${turn.index}")
            .setUri(Uri.fromFile(file))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist("${turn.host.displayName} · Sacred Steps to Redemption")
                    .setAlbumTitle(track.subtitle)
                    .setSubtitle("${turn.timestampLabel} · ${turn.sectionLabel}")
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.setPlaybackSpeed(_uiState.value.playbackSpeed)
        controller.prepare()
        controller.play()
    }

    private fun handleSegmentCompleted() {
        val latest = _uiState.value
        val track = latest.selectedTrack ?: return
        val nextIdx = latest.activeTurnIndex + 1

        if (!playingBundledFullAudio && nextIdx < latest.dialogueTurns.size) {
            val sessionId = activePlaySessionId
            playbackJob = viewModelScope.launch {
                // Smooth natural pause between chapters by the same solo presenter
                delay(250L)
                if (sessionId == activePlaySessionId &&
                    _uiState.value.selectedTrack?.trackId == track.trackId
                ) {
                    playTurn(nextIdx)
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    playbackStatus = PodcastPlaybackStatus.IDLE,
                    currentPositionMs = it.durationMs,
                    turnProgressFraction = 1f,
                    statusBanner = "${track.title} complete. Take the next step."
                )
            }
        }
    }

    /**
     * Synthesizes and caches every chapter of the currently selected track via Murf.ai
     * so the full monthly orientation or daily episode plays seamlessly offline.
     */
    fun prepareFullTrackOffline() {
        val state = _uiState.value
        val track = state.selectedTrack ?: return
        val turns = state.dialogueTurns
        if (turns.isEmpty() || state.isPreparingTrack) return

        prepareAllJob?.cancel()
        prepareAllJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPreparingTrack = true,
                    prepareProgress = 0f,
                    errorMessage = null,
                    statusBanner = "Preparing ${state.activePresenter.displayName}'s voice for ${track.title}..."
                )
            }

            var completed = 0
            for (turn in turns) {
                val res = murfAudioRepository.getOrSynthesizeTurn(track.trackId, turn)
                if (res.isFailure) {
                    _uiState.update {
                        it.copy(
                            isPreparingTrack = false,
                            errorMessage = res.exceptionOrNull()?.message
                                ?: "Stopped preparing audio for ${track.title}.",
                            statusBanner = null
                        )
                    }
                    return@launch
                }
                completed++
                _uiState.update {
                    it.copy(
                        cachedTurnIndices = it.cachedTurnIndices + turn.index,
                        prepareProgress = completed.toFloat() / turns.size.toFloat()
                    )
                }
            }

            _uiState.update {
                it.copy(
                    isPreparingTrack = false,
                    prepareProgress = 1f,
                    statusBanner = "All ${turns.size} chapters cached for offline listening."
                )
            }
        }
    }

    private fun prefetchNextTurn(
        trackId: String,
        turns: List<PodcastDialogueTurn>,
        nextIndex: Int
    ) {
        val nextTurn = turns.getOrNull(nextIndex) ?: return
        if (murfAudioRepository.isTurnCached(trackId, nextTurn)) return
        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            murfAudioRepository.getOrSynthesizeTurn(trackId, nextTurn)
                .onSuccess {
                    _uiState.update { state ->
                        if (state.selectedTrack?.trackId == trackId) {
                            state.copy(cachedTurnIndices = state.cachedTurnIndices + nextTurn.index)
                        } else {
                            state
                        }
                    }
                }
        }
    }

    private fun syncPositionFromController(controller: MediaController) {
        val dur = controller.duration.coerceAtLeast(0L)
        val pos = controller.currentPosition.coerceAtLeast(0L)
        _uiState.update {
            it.copy(
                currentPositionMs = pos,
                durationMs = dur,
                turnProgressFraction = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
            )
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val controller = activeMediaController
                if (controller == null || !controller.isPlaying) {
                    break
                }
                syncPositionFromController(controller)
                delay(250L)
            }
        }
    }

    private fun stopPlaybackInternal() {
        activePlaySessionId++
        playbackJob?.cancel()
        prefetchJob?.cancel()
        progressJob?.cancel()
        activeMediaController?.let { controller ->
            runCatching {
                controller.stop()
                controller.clearMediaItems()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaybackInternal()
        prepareAllJob?.cancel()
        activeMediaController?.removeListener(playerListener)
        activeMediaController = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }

    companion object {
        fun provideFactory(
            appContext: Context,
            podcastRepository: PodcastRepository,
            murfAudioRepository: MurfAudioRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PodcastViewModel(
                    appContext = appContext.applicationContext,
                    podcastRepository = podcastRepository,
                    murfAudioRepository = murfAudioRepository
                ) as T
            }
        }
    }
}
