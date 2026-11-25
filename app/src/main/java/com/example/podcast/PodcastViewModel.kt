package com.example.podcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PodcastUiState(
    val isLoadingCatalog: Boolean = true,
    val episodes: List<WeeklyPodcastEpisode> = emptyList(),
    val months: List<String> = emptyList(),
    val selectedMonth: String? = null,
    val selectedEpisode: WeeklyPodcastEpisode? = null,
    val dialogueTurns: List<PodcastDialogueTurn> = emptyList(),
    val activeTurnIndex: Int = 0,
    val playbackStatus: PodcastPlaybackStatus = PodcastPlaybackStatus.IDLE,
    val leadHost: PodcastHost = PodcastHost.GRACE,
    val cachedTurnIndices: Set<Int> = emptySet(),
    val isPreparingEpisode: Boolean = false,
    val prepareProgress: Float = 0f,
    val turnProgressFraction: Float = 0f,
    val isMurfConfigured: Boolean = false,
    val errorMessage: String? = null,
    val statusBanner: String? = null
) {
    val filteredEpisodes: List<WeeklyPodcastEpisode>
        get() = if (selectedMonth == null) {
            episodes
        } else {
            episodes.filter { it.monthName.equals(selectedMonth, ignoreCase = true) }
        }

    val activeTurn: PodcastDialogueTurn?
        get() = dialogueTurns.getOrNull(activeTurnIndex)
}

class PodcastViewModel(
    private val podcastRepository: PodcastRepository,
    private val murfAudioRepository: MurfAudioRepository,
    private val audioPlayer: PodcastAudioPlayer = PodcastAudioPlayer()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PodcastUiState(
            isMurfConfigured = murfAudioRepository.isApiKeyConfigured()
        )
    )
    val uiState: StateFlow<PodcastUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var prefetchJob: Job? = null
    private var progressJob: Job? = null
    private var prepareAllJob: Job? = null

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            runCatching { podcastRepository.loadAllEpisodes() }
                .onSuccess { episodes ->
                    val orderedMonths = episodes.map { it.monthName }.distinct()
                    _uiState.update {
                        it.copy(
                            isLoadingCatalog = false,
                            episodes = episodes,
                            months = orderedMonths,
                            isMurfConfigured = murfAudioRepository.isApiKeyConfigured(),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingCatalog = false,
                            errorMessage = err.message ?: "Unable to load weekly podcasts."
                        )
                    }
                }
        }
    }

    fun selectMonthFilter(month: String?) {
        _uiState.update { it.copy(selectedMonth = month) }
    }

    fun openEpisode(episode: WeeklyPodcastEpisode, autoPlay: Boolean = false) {
        stopPlaybackInternal()
        val leadHost = _uiState.value.leadHost
        val turns = PodcastScriptBuilder.buildDialogueTurns(episode, leadHost)
        val cached = murfAudioRepository.getCachedTurnIndices(episode.weekNumber, turns)

        _uiState.update {
            it.copy(
                selectedEpisode = episode,
                dialogueTurns = turns,
                activeTurnIndex = 0,
                playbackStatus = PodcastPlaybackStatus.IDLE,
                cachedTurnIndices = cached,
                turnProgressFraction = 0f,
                errorMessage = null,
                statusBanner = null
            )
        }

        if (autoPlay) {
            playTurn(0)
        }
    }

    fun closeEpisodePlayer() {
        stopPlaybackInternal()
        prepareAllJob?.cancel()
        _uiState.update {
            it.copy(
                selectedEpisode = null,
                dialogueTurns = emptyList(),
                activeTurnIndex = 0,
                playbackStatus = PodcastPlaybackStatus.IDLE,
                isPreparingEpisode = false,
                prepareProgress = 0f,
                turnProgressFraction = 0f,
                errorMessage = null,
                statusBanner = null
            )
        }
    }

    /**
     * Switches which Murf voice opens the episode (Grace or Marcus) while keeping
     * strict voice alternation across every single consecutive turn.
     */
    fun swapAlternatingLeadHost() {
        val nextLead = _uiState.value.leadHost.alternate()
        val episode = _uiState.value.selectedEpisode
        val wasPlaying = _uiState.value.playbackStatus == PodcastPlaybackStatus.PLAYING ||
            _uiState.value.playbackStatus == PodcastPlaybackStatus.SYNTHESIZING
        val currentIdx = _uiState.value.activeTurnIndex

        stopPlaybackInternal()
        prepareAllJob?.cancel()

        if (episode != null) {
            val rebuiltTurns = PodcastScriptBuilder.buildDialogueTurns(episode, nextLead)
            val cached = murfAudioRepository.getCachedTurnIndices(episode.weekNumber, rebuiltTurns)
            _uiState.update {
                it.copy(
                    leadHost = nextLead,
                    dialogueTurns = rebuiltTurns,
                    cachedTurnIndices = cached,
                    playbackStatus = PodcastPlaybackStatus.IDLE,
                    isPreparingEpisode = false,
                    prepareProgress = 0f,
                    turnProgressFraction = 0f,
                    statusBanner = "${nextLead.displayName} now opens, alternating with ${nextLead.alternate().displayName}."
                )
            }
            if (wasPlaying) {
                playTurn(currentIdx)
            }
        } else {
            _uiState.update {
                it.copy(
                    leadHost = nextLead,
                    statusBanner = "${nextLead.displayName} will open episodes, alternating with ${nextLead.alternate().displayName}."
                )
            }
        }
    }

    fun togglePlayPause() {
        val state = _uiState.value
        when (state.playbackStatus) {
            PodcastPlaybackStatus.PLAYING -> {
                audioPlayer.pause()
                progressJob?.cancel()
                _uiState.update { it.copy(playbackStatus = PodcastPlaybackStatus.PAUSED) }
            }
            PodcastPlaybackStatus.PAUSED -> {
                audioPlayer.resume()
                _uiState.update { it.copy(playbackStatus = PodcastPlaybackStatus.PLAYING) }
                startProgressTicker()
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
        val episode = state.selectedEpisode ?: return
        val turn = state.dialogueTurns.getOrNull(turnIndex) ?: return

        stopPlaybackInternal()

        _uiState.update {
            it.copy(
                activeTurnIndex = turnIndex,
                playbackStatus = PodcastPlaybackStatus.SYNTHESIZING,
                turnProgressFraction = 0f,
                errorMessage = null,
                statusBanner = null
            )
        }

        playbackJob = viewModelScope.launch {
            val result = murfAudioRepository.getOrSynthesizeTurn(episode.weekNumber, turn)
            result.onSuccess { audioFile ->
                _uiState.update {
                    it.copy(cachedTurnIndices = it.cachedTurnIndices + turn.index)
                }

                // Prefetch the next alternating host's voice in the background for gapless transitions
                prefetchNextTurn(episode.weekNumber, state.dialogueTurns, turnIndex + 1)

                audioPlayer.playFile(
                    file = audioFile,
                    onPrepared = {
                        _uiState.update {
                            it.copy(
                                playbackStatus = PodcastPlaybackStatus.PLAYING,
                                errorMessage = null
                            )
                        }
                        startProgressTicker()
                    },
                    onCompleted = {
                        progressJob?.cancel()
                        val latest = _uiState.value
                        val nextIdx = turnIndex + 1
                        if (latest.selectedEpisode?.weekNumber == episode.weekNumber &&
                            nextIdx < latest.dialogueTurns.size
                        ) {
                            playTurn(nextIdx)
                        } else {
                            _uiState.update {
                                it.copy(
                                    playbackStatus = PodcastPlaybackStatus.IDLE,
                                    turnProgressFraction = 1f,
                                    statusBanner = "Week ${episode.weekNumber} complete. Take the next step."
                                )
                            }
                        }
                    },
                    onError = { errMsg ->
                        progressJob?.cancel()
                        _uiState.update {
                            it.copy(
                                playbackStatus = PodcastPlaybackStatus.ERROR,
                                errorMessage = errMsg
                            )
                        }
                    }
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

    /**
     * Synthesizes and caches every alternating turn of the currently selected episode via Murf.ai
     * so the full weekly episode plays seamlessly offline.
     */
    fun prepareFullEpisodeOffline() {
        val state = _uiState.value
        val episode = state.selectedEpisode ?: return
        val turns = state.dialogueTurns
        if (turns.isEmpty() || state.isPreparingEpisode) return

        prepareAllJob?.cancel()
        prepareAllJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPreparingEpisode = true,
                    prepareProgress = 0f,
                    errorMessage = null,
                    statusBanner = "Synthesizing alternating voices for Week ${episode.weekNumber}..."
                )
            }

            var completed = 0
            for (turn in turns) {
                val res = murfAudioRepository.getOrSynthesizeTurn(episode.weekNumber, turn)
                if (res.isFailure) {
                    _uiState.update {
                        it.copy(
                            isPreparingEpisode = false,
                            errorMessage = res.exceptionOrNull()?.message
                                ?: "Stopped preparing audio for Week ${episode.weekNumber}.",
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
                    isPreparingEpisode = false,
                    prepareProgress = 1f,
                    statusBanner = "All ${turns.size} alternating segments cached for offline listening."
                )
            }
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBanner = null, errorMessage = null) }
    }

    private fun prefetchNextTurn(
        weekNumber: Int,
        turns: List<PodcastDialogueTurn>,
        nextIndex: Int
    ) {
        val nextTurn = turns.getOrNull(nextIndex) ?: return
        if (murfAudioRepository.isTurnCached(weekNumber, nextTurn)) return
        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            murfAudioRepository.getOrSynthesizeTurn(weekNumber, nextTurn)
                .onSuccess {
                    _uiState.update { state ->
                        if (state.selectedEpisode?.weekNumber == weekNumber) {
                            state.copy(cachedTurnIndices = state.cachedTurnIndices + nextTurn.index)
                        } else {
                            state
                        }
                    }
                }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && audioPlayer.isPlaying()) {
                val dur = audioPlayer.durationMs().coerceAtLeast(1)
                val pos = audioPlayer.currentPositionMs().coerceIn(0, dur)
                _uiState.update {
                    it.copy(turnProgressFraction = pos.toFloat() / dur.toFloat())
                }
                delay(200L)
            }
        }
    }

    private fun stopPlaybackInternal() {
        playbackJob?.cancel()
        prefetchJob?.cancel()
        progressJob?.cancel()
        audioPlayer.release()
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaybackInternal()
        prepareAllJob?.cancel()
    }

    companion object {
        fun provideFactory(
            podcastRepository: PodcastRepository,
            murfAudioRepository: MurfAudioRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PodcastViewModel(
                    podcastRepository = podcastRepository,
                    murfAudioRepository = murfAudioRepository
                ) as T
            }
        }
    }
}
