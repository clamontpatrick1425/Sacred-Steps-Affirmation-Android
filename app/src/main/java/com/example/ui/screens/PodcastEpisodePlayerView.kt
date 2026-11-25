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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.podcast.PodcastDialogueTurn
import com.example.podcast.PodcastHost
import com.example.podcast.PodcastPlaybackStatus
import com.example.podcast.PodcastUiState
import com.example.podcast.WeeklyPodcastEpisode
import com.example.ui.theme.LocalSacredColors

@Composable
fun PodcastEpisodePlayerView(
    episode: WeeklyPodcastEpisode,
    uiState: PodcastUiState,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPreviousTurn: () -> Unit,
    onNextTurn: () -> Unit,
    onSelectTurn: (Int) -> Unit,
    onSwapLeadHost: () -> Unit,
    onPrepareOffline: () -> Unit
) {
    BackHandler(onBack = onBack)

    val sacredColors = LocalSacredColors.current
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.activeTurnIndex) {
        // Offset by 2 because header deck + transcript header precede turn items
        val targetIndex = (uiState.activeTurnIndex + 1).coerceAtLeast(0)
        if (uiState.playbackStatus == PodcastPlaybackStatus.PLAYING ||
            uiState.playbackStatus == PodcastPlaybackStatus.SYNTHESIZING
        ) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
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
                        onClick = onBack,
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
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.podcast_back_to_episodes),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.podcast_all_episodes),
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
                    episode = episode,
                    uiState = uiState,
                    onTogglePlayPause = onTogglePlayPause,
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

            item(key = "transcript_heading") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.podcast_transcript_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.podcast_turn_counter,
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
                key = { turn -> "turn_${episode.weekNumber}_${turn.index}_${turn.voiceId}" }
            ) { turn ->
                val isActive = turn.index == uiState.activeTurnIndex
                val isCached = turn.index in uiState.cachedTurnIndices
                PodcastTurnCard(
                    turn = turn,
                    isActive = isActive,
                    isPlaying = isActive && uiState.playbackStatus == PodcastPlaybackStatus.PLAYING,
                    isSynthesizing = isActive && uiState.playbackStatus == PodcastPlaybackStatus.SYNTHESIZING,
                    isCached = isCached,
                    onClick = { onSelectTurn(turn.index) }
                )
            }

            item(key = "reflection_questions_summary") {
                PodcastReflectionCard(episode = episode)
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PodcastStudioDeck(
    episode: WeeklyPodcastEpisode,
    uiState: PodcastUiState,
    onTogglePlayPause: () -> Unit,
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
                            text = "WEEK ${episode.weekNumber} OF 52 · ${episode.monthName.uppercase()}",
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
                        text = episode.weekTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = episode.monthTheme,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    )
                }

                // Active Speaker Pill
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
                                text = activeTurn?.sectionLabel ?: episode.weekTitle,
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

                // Segment progress indicator
                LinearProgressIndicator(
                    progress = { uiState.turnProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = speakerAccent,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                )

                // Transport Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
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
                                alpha = if (uiState.activeTurnIndex > 0) 1f else 0.4f
                            ),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = sacredColors.dawnGold,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        modifier = Modifier
                            .size(62.dp)
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
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = stringResource(R.string.podcast_play),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

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
                                alpha = if (uiState.activeTurnIndex < uiState.dialogueTurns.lastIndex) 1f else 0.4f
                            ),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Offline Pre-Generation Action
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
                        enabled = !uiState.isPreparingEpisode && !allCached,
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
                                uiState.isPreparingEpisode -> stringResource(
                                    R.string.podcast_preparing_percent,
                                    (uiState.prepareProgress * 100).toInt()
                                )
                                else -> stringResource(R.string.podcast_prepare_full_episode)
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.isPreparingEpisode) {
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
private fun PodcastTurnCard(
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
        tonalElevation = if (isActive) 3.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("podcast_turn_card_${turn.index}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(hostContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = turn.host.displayName.take(1),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = turn.host.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "· ${turn.voiceId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = sacredColors.subtleText
                        )
                    }
                    Text(
                        text = turn.sectionLabel,
                        style = MaterialTheme.typography.labelMedium,
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
                            contentDescription = stringResource(R.string.podcast_cached_segment_desc),
                            tint = sacredColors.sage,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = turn.spokenText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PodcastReflectionCard(episode: WeeklyPodcastEpisode) {
    val sacredColors = LocalSacredColors.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.hairline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("podcast_reflection_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.podcast_reflection_questions_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            episode.reflectionQuestions.forEachIndexed { idx, question ->
                Text(
                    text = "${idx + 1}. $question",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
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
                        text = stringResource(R.string.podcast_next_week_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold
                    )
                    Text(
                        text = episode.nextWeekTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
