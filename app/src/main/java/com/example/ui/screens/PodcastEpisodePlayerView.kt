package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.podcast.PlayablePodcastTrack
import com.example.podcast.PodcastDialogueTurn
import com.example.podcast.PodcastHost
import com.example.podcast.PodcastPlaybackStatus
import com.example.podcast.PodcastUiState
import com.example.ui.theme.LocalSacredColors
import java.util.Locale

private val PLAYBACK_SPEEDS = listOf(0.8f, 1.0f, 1.2f)

@Composable
fun PodcastEpisodePlayerView(
    track: PlayablePodcastTrack,
    uiState: PodcastUiState,
    onMinimize: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipBack15: () -> Unit,
    onSkipForward15: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onPreviousTurn: () -> Unit,
    onNextTurn: () -> Unit,
    onSelectTurn: (Int) -> Unit,
    onSwapLeadHost: () -> Unit,
    onPrepareOffline: () -> Unit
) {
    BackHandler(onBack = onMinimize)

    val sacredColors = LocalSacredColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("podcast_player_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "player_top_nav") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        onClick = onMinimize,
                        shape = RoundedCornerShape(14.dp),
                        color = sacredColors.warmCard,
                        border = BorderStroke(1.dp, sacredColors.hairline),
                        modifier = Modifier.testTag("podcast_player_back_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.podcast_minimize_player_desc),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.podcast_minimize_player),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        onClick = onSwapLeadHost,
                        shape = RoundedCornerShape(14.dp),
                        color = sacredColors.dawnGoldContainer,
                        border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("podcast_swap_voices_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = stringResource(R.string.podcast_swap_voices_desc),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(
                                    R.string.podcast_lead_voice_label,
                                    uiState.leadHost.displayName
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item(key = "player_studio_deck") {
                PodcastStudioDeck(
                    track = track,
                    uiState = uiState,
                    onTogglePlayPause = onTogglePlayPause,
                    onSeek = onSeek,
                    onSkipBack15 = onSkipBack15,
                    onSkipForward15 = onSkipForward15,
                    onSetSpeed = onSetSpeed,
                    onPreviousTurn = onPreviousTurn,
                    onNextTurn = onNextTurn,
                    onPrepareOffline = onPrepareOffline
                )
            }

            if (!uiState.isMurfConfigured) {
                item(key = "murf_key_notice") {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = sacredColors.dawnGoldContainer,
                        border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("podcast_murf_setup_notice")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.podcast_murf_setup_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.podcast_murf_setup_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                item(key = "player_error") {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (uiState.statusBanner != null) {
                item(key = "player_status_banner") {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = sacredColors.sageContainer,
                        border = BorderStroke(1.dp, sacredColors.sage.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.statusBanner,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            item(key = "chapters_heading") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.podcast_chapters_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.podcast_chapter_counter,
                            uiState.activeTurnIndex + 1,
                            uiState.dialogueTurns.size.coerceAtLeast(1)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = sacredColors.subtleText
                    )
                }
            }

            items(
                items = uiState.dialogueTurns,
                key = { turn -> "chapter_${track.trackId}_${turn.index}_${turn.voiceId}" }
            ) { turn ->
                val isActive = turn.index == uiState.activeTurnIndex
                val isCached = turn.index in uiState.cachedTurnIndices
                PodcastChapterRow(
                    turn = turn,
                    isActive = isActive,
                    isPlaying = isActive && uiState.playbackStatus == PodcastPlaybackStatus.PLAYING,
                    isSynthesizing = isActive && uiState.playbackStatus == PodcastPlaybackStatus.SYNTHESIZING,
                    isCached = isCached,
                    onClick = { onSelectTurn(turn.index) }
                )
            }

            item(key = "track_companion_summary") {
                when (track) {
                    is PlayablePodcastTrack.DailyEpisode -> DailySacredStepReferenceCard(track)
                    is PlayablePodcastTrack.MonthlyOrientation -> MonthlyRoadmapReferenceCard(track)
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PodcastStudioDeck(
    track: PlayablePodcastTrack,
    uiState: PodcastUiState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipBack15: () -> Unit,
    onSkipForward15: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onPreviousTurn: () -> Unit,
    onNextTurn: () -> Unit,
    onPrepareOffline: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val activeTurn = uiState.activeTurn
    val activeHost = activeTurn?.host ?: uiState.leadHost
    val isGraceSpeaking = activeHost == PodcastHost.GRACE
    val speakerAccent = if (isGraceSpeaking) sacredColors.dawnGold else sacredColors.sage
    val allCached = uiState.dialogueTurns.isNotEmpty() &&
        uiState.cachedTurnIndices.size >= uiState.dialogueTurns.size

    val pulseTransition = rememberInfiniteTransition(label = "speakerPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakerPulseAlpha"
    )

    val durationFloat = if (uiState.durationMs > 0L) uiState.durationMs.toFloat() else 1f
    val currentPosFloat = uiState.currentPositionMs.coerceIn(0L, uiState.durationMs.coerceAtLeast(1L)).toFloat()

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("podcast_studio_deck")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = sacredColors.dawnGold.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = track.badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = null,
                            tint = speakerAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.podcast_alternating_voices_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = track.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        speakerAccent.copy(
                            alpha = if (uiState.playbackStatus == PodcastPlaybackStatus.PLAYING) {
                                pulseAlpha
                            } else {
                                0.5f
                            }
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(speakerAccent.copy(alpha = 0.28f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeHost.displayName} · ${activeHost.murfVoiceId}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = activeTurn?.let { "${it.timestampLabel} · ${it.sectionLabel}" }
                                    ?: track.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = when (uiState.playbackStatus) {
                                PodcastPlaybackStatus.SYNTHESIZING -> stringResource(R.string.podcast_status_synthesizing)
                                PodcastPlaybackStatus.PLAYING -> stringResource(R.string.podcast_status_playing)
                                PodcastPlaybackStatus.PAUSED -> stringResource(R.string.podcast_status_paused)
                                PodcastPlaybackStatus.ERROR -> stringResource(R.string.podcast_status_error)
                                PodcastPlaybackStatus.IDLE -> stringResource(R.string.podcast_status_ready)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = speakerAccent
                        )
                    }
                }

                // Interactive Media3 Scrubber with MM:SS timestamps
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Slider(
                        value = currentPosFloat,
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..durationFloat,
                        enabled = uiState.durationMs > 0L,
                        colors = SliderDefaults.colors(
                            thumbColor = sacredColors.dawnGold,
                            activeTrackColor = speakerAccent,
                            inactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f),
                            disabledThumbColor = sacredColors.dawnGold.copy(alpha = 0.45f),
                            disabledActiveTrackColor = speakerAccent.copy(alpha = 0.45f),
                            disabledInactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("podcast_scrubber_slider")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatAudioDuration(uiState.currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                        )
                        Text(
                            text = if (uiState.durationMs > 0L) {
                                formatAudioDuration(uiState.durationMs)
                            } else {
                                track.durationFormatted
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                        )
                    }
                }

                // Transport Controls: Prev Chapter, -15s, Play/Pause, +15s, Next Chapter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousTurn,
                        enabled = uiState.activeTurnIndex > 0,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("podcast_previous_turn_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = stringResource(R.string.podcast_prev_segment),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = if (uiState.activeTurnIndex > 0) 1f else 0.35f
                            ),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipBack15,
                        enabled = uiState.durationMs > 0L,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("podcast_skip_back_15_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay,
                            contentDescription = stringResource(R.string.podcast_rewind_15),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = if (uiState.durationMs > 0L) 0.95f else 0.35f
                            ),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = sacredColors.dawnGold,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("podcast_play_pause_button")
                    ) {
                        when (uiState.playbackStatus) {
                            PodcastPlaybackStatus.SYNTHESIZING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 2.8.dp,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                            PodcastPlaybackStatus.PLAYING -> {
                                Icon(
                                    imageVector = Icons.Rounded.Pause,
                                    contentDescription = stringResource(R.string.podcast_pause),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = stringResource(R.string.podcast_play),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onSkipForward15,
                        enabled = uiState.durationMs > 0L,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("podcast_skip_forward_15_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward10,
                            contentDescription = stringResource(R.string.podcast_forward_15),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = if (uiState.durationMs > 0L) 0.95f else 0.35f
                            ),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextTurn,
                        enabled = uiState.activeTurnIndex < uiState.dialogueTurns.lastIndex,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("podcast_next_turn_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = stringResource(R.string.podcast_next_segment),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = if (uiState.activeTurnIndex < uiState.dialogueTurns.lastIndex) 1f else 0.35f
                            ),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Playback Speed Selector Chips (0.8x, 1.0x, 1.2x)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PLAYBACK_SPEEDS.forEach { speed ->
                        val selected = uiState.playbackSpeed == speed
                        FilterChip(
                            selected = selected,
                            onClick = { onSetSpeed(speed) },
                            label = {
                                Text(
                                    text = "${speed}x",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sacredColors.dawnGold,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                selectedBorderColor = sacredColors.dawnGold
                            ),
                            modifier = Modifier.testTag("podcast_speed_${speed}x")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.podcast_cached_count,
                            uiState.cachedTurnIndices.size,
                            uiState.dialogueTurns.size
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )

                    FilledTonalButton(
                        onClick = onPrepareOffline,
                        enabled = !uiState.isPreparingTrack && !allCached,
                        modifier = Modifier.testTag("podcast_prepare_offline_button")
                    ) {
                        Icon(
                            imageVector = if (allCached) {
                                Icons.Rounded.DownloadDone
                            } else {
                                Icons.Rounded.DownloadForOffline
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                allCached -> stringResource(R.string.podcast_offline_ready)
                                uiState.isPreparingTrack -> stringResource(
                                    R.string.podcast_preparing_percent,
                                    (uiState.prepareProgress * 100).toInt()
                                )
                                else -> stringResource(R.string.podcast_prepare_full_episode)
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.isPreparingTrack) {
                    LinearProgressIndicator(
                        progress = { uiState.prepareProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = sacredColors.dawnGold,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PodcastChapterRow(
    turn: PodcastDialogueTurn,
    isActive: Boolean,
    isPlaying: Boolean,
    isSynthesizing: Boolean,
    isCached: Boolean,
    onClick: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val isGrace = turn.host == PodcastHost.GRACE
    val hostColor = if (isGrace) sacredColors.dawnGold else sacredColors.sage
    val hostContainer = if (isGrace) sacredColors.dawnGoldContainer else sacredColors.sageContainer

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isActive) MaterialTheme.colorScheme.surface else sacredColors.warmCard,
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) hostColor else sacredColors.hairline
        ),
        tonalElevation = if (isActive) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("podcast_chapter_row_${turn.index}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = hostContainer
            ) {
                Text(
                    text = turn.timestampLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = turn.sectionLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Voice: ${turn.host.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = hostColor
                )
            }

            when {
                isSynthesizing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = hostColor
                    )
                }
                isPlaying -> {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = stringResource(R.string.podcast_status_playing),
                        tint = hostColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                isCached -> {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.podcast_offline_ready),
                        tint = sacredColors.sage,
                        modifier = Modifier.size(18.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.podcast_play),
                        tint = sacredColors.subtleText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailySacredStepReferenceCard(track: PlayablePodcastTrack.DailyEpisode) {
    val sacredColors = LocalSacredColors.current
    val step = track.daily.sacredStep

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.hairline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("podcast_daily_step_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.podcast_sacred_step_summary_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            StepSummaryItem(
                label = stringResource(R.string.step_label_scripture),
                body = step.scriptureReference
            )
            StepSummaryItem(
                label = stringResource(R.string.step_label_truth),
                body = step.truth
            )
            StepSummaryItem(
                label = stringResource(R.string.step_label_embrace),
                body = step.embraceAffirmation,
                italic = true
            )
            StepSummaryItem(
                label = stringResource(R.string.step_label_practice),
                body = step.practiceAction
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = sacredColors.dawnGoldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.podcast_closing_prayer_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = step.closingPrayer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StepSummaryItem(
    label: String,
    body: String,
    italic: Boolean = false
) {
    val sacredColors = LocalSacredColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = sacredColors.dawnGold,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MonthlyRoadmapReferenceCard(track: PlayablePodcastTrack.MonthlyOrientation) {
    val sacredColors = LocalSacredColors.current
    val playlist = track.playlist

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.hairline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("podcast_monthly_roadmap_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.podcast_monthly_roadmap_title, playlist.monthName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = MaterialTheme.shapes.small,
                color = sacredColors.dawnGoldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.podcast_anchor_scripture_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = playlist.themeScripture,
                        style = MaterialTheme.typography.titleSmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            playlist.weeksOverview.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = sacredColors.warmCard
                    ) {
                        Text(
                            text = "WK ${week.weekNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = sacredColors.dawnGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = week.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

fun formatAudioDuration(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
