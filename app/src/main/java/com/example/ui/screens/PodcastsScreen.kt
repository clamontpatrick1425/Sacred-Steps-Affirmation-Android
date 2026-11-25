package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.podcast.PodcastHost
import com.example.podcast.PodcastUiState
import com.example.podcast.WeeklyPodcastEpisode
import com.example.ui.theme.LocalSacredColors

@Composable
fun PodcastsScreen(
    uiState: PodcastUiState,
    currentWeekNumber: Int,
    onSelectMonth: (String?) -> Unit,
    onOpenEpisode: (WeeklyPodcastEpisode, Boolean) -> Unit,
    onCloseEpisode: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPreviousTurn: () -> Unit,
    onNextTurn: () -> Unit,
    onSelectTurn: (Int) -> Unit,
    onSwapLeadHost: () -> Unit,
    onPrepareOffline: () -> Unit
) {
    val activeEpisode = uiState.selectedEpisode
    if (activeEpisode != null) {
        PodcastEpisodePlayerView(
            episode = activeEpisode,
            uiState = uiState,
            onBack = onCloseEpisode,
            onTogglePlayPause = onTogglePlayPause,
            onPreviousTurn = onPreviousTurn,
            onNextTurn = onNextTurn,
            onSelectTurn = onSelectTurn,
            onSwapLeadHost = onSwapLeadHost,
            onPrepareOffline = onPrepareOffline
        )
        return
    }

    val sacredColors = LocalSacredColors.current
    val spotlightEpisode = uiState.episodes.firstOrNull { it.weekNumber == currentWeekNumber }
        ?: uiState.episodes.firstOrNull()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (uiState.isLoadingCatalog) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .testTag("podcasts_catalog_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "podcast_hero") {
                    PodcastHeroHeader(
                        leadHost = uiState.leadHost,
                        episodeCount = uiState.episodes.size,
                        onSwapLeadHost = onSwapLeadHost
                    )
                }

                if (spotlightEpisode != null && uiState.selectedMonth == null) {
                    item(key = "spotlight_week") {
                        SpotlightEpisodeCard(
                            episode = spotlightEpisode,
                            onListenNow = { onOpenEpisode(spotlightEpisode, true) },
                            onReadScript = { onOpenEpisode(spotlightEpisode, false) }
                        )
                    }
                }

                item(key = "month_filters") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("podcast_month_filters")
                    ) {
                        item(key = "filter_all") {
                            FilterChip(
                                selected = uiState.selectedMonth == null,
                                onClick = { onSelectMonth(null) },
                                label = { Text(stringResource(R.string.podcast_filter_all)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = sacredColors.dawnGoldContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("podcast_filter_all")
                            )
                        }
                        items(uiState.months, key = { it }) { month ->
                            FilterChip(
                                selected = uiState.selectedMonth == month,
                                onClick = { onSelectMonth(month) },
                                label = { Text(month) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = sacredColors.dawnGoldContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("podcast_filter_${month.lowercase()}")
                            )
                        }
                    }
                }

                item(key = "episodes_section_title") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.selectedMonth?.let {
                                stringResource(R.string.podcast_month_episodes_title, it)
                            } ?: stringResource(R.string.podcast_all_52_weeks_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(
                                R.string.podcast_episode_count_label,
                                uiState.filteredEpisodes.size
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = sacredColors.subtleText
                        )
                    }
                }

                items(
                    items = uiState.filteredEpisodes,
                    key = { ep -> "episode_week_${ep.weekNumber}" }
                ) { episode ->
                    WeeklyPodcastEpisodeCard(
                        episode = episode,
                        isCurrentWeek = episode.weekNumber == currentWeekNumber,
                        onOpen = { onOpenEpisode(episode, false) },
                        onPlay = { onOpenEpisode(episode, true) }
                    )
                }

                item(key = "catalog_bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PodcastHeroHeader(
    leadHost: PodcastHost,
    episodeCount: Int,
    onSwapLeadHost: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val partnerHost = leadHost.alternate()

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("podcast_hero_header")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Podcasts,
                        contentDescription = null,
                        tint = sacredColors.dawnGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(R.string.podcast_hero_eyebrow, episodeCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = sacredColors.dawnGold
                    )
                }

                Text(
                    text = stringResource(R.string.podcast_hero_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = stringResource(R.string.podcast_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                )

                // Alternating Voice Cast Pill
                Surface(
                    onClick = onSwapLeadHost,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("podcast_catalog_swap_voices")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RecordVoiceOver,
                                contentDescription = null,
                                tint = sacredColors.dawnGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "${leadHost.displayName} ↔ ${partnerHost.displayName}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Murf Voices · ${leadHost.murfVoiceId} & ${partnerHost.murfVoiceId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = stringResource(R.string.podcast_swap_voices_desc),
                            tint = sacredColors.dawnGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotlightEpisodeCard(
    episode: WeeklyPodcastEpisode,
    onListenNow: () -> Unit,
    onReadScript: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = sacredColors.dawnGoldContainer,
        border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onReadScript)
            .testTag("podcast_spotlight_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Headphones,
                    contentDescription = null,
                    tint = sacredColors.dawnGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.podcast_this_week_spotlight, episode.weekNumber),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = episode.weekTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = episode.weeklyIntroduction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onListenNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("podcast_spotlight_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.podcast_listen_now))
                }
            }
        }
    }
}

@Composable
private fun WeeklyPodcastEpisodeCard(
    episode: WeeklyPodcastEpisode,
    isCurrentWeek: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isCurrentWeek) 1.5.dp else 1.dp,
            color = if (isCurrentWeek) sacredColors.dawnGold else sacredColors.hairline
        ),
        tonalElevation = if (isCurrentWeek) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onOpen)
            .testTag("podcast_episode_card_${episode.weekNumber}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Editorial Week Number Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isCurrentWeek) sacredColors.dawnGoldContainer else sacredColors.warmCard
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WK",
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold
                    )
                    Text(
                        text = episode.weekNumber.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = episode.monthTheme,
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.dawnGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = episode.weekTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = episode.weeklyIntroduction,
                    style = MaterialTheme.typography.bodySmall,
                    color = sacredColors.subtleText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 48dp Play Trigger
            Surface(
                onClick = onPlay,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("podcast_play_week_${episode.weekNumber}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(
                            R.string.podcast_play_week_desc,
                            episode.weekNumber,
                            episode.weekTitle
                        ),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
