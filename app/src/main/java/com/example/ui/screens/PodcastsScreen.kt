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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.podcast.DailyPodcastTrack
import com.example.podcast.MonthlyAudioPlaylist
import com.example.podcast.PlayablePodcastTrack
import com.example.podcast.PodcastHost
import com.example.podcast.PodcastUiState
import com.example.ui.theme.LocalSacredColors

@Composable
fun PodcastsScreen(
    uiState: PodcastUiState,
    onSelectMonth: (Int) -> Unit,
    onOpenTrack: (PlayablePodcastTrack, Boolean) -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val selectedMonthPlaylist = uiState.selectedMonthPlaylist
    val todayTrack = uiState.todayDailyTrack
    val totalMonthly = uiState.manifestInfo?.totals?.totalMonthlyPodcasts ?: uiState.monthlyPlaylists.size
    val totalDaily = uiState.manifestInfo?.totals?.totalDailyPodcasts
        ?: uiState.monthlyPlaylists.sumOf { it.dailyPodcasts.size }
    val totalTracks = uiState.manifestInfo?.totals?.totalAudioTracks ?: (totalMonthly + totalDaily)

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
                        totalMonthly = totalMonthly,
                        totalDaily = totalDaily,
                        totalTracks = totalTracks
                    )
                }

                if (todayTrack != null) {
                    item(key = "today_daily_spotlight") {
                        TodayDailySpotlightCard(
                            todayTrack = todayTrack,
                            onListenNow = { onOpenTrack(todayTrack, true) },
                            onOpenTrack = { onOpenTrack(todayTrack, false) }
                        )
                    }
                }

                item(key = "month_selector_row") {
                    val monthListState = rememberLazyListState()
                    LaunchedEffect(uiState.selectedMonthNumber, uiState.monthlyPlaylists.size) {
                        val idx = uiState.monthlyPlaylists.indexOfFirst {
                            it.monthNumber == uiState.selectedMonthNumber
                        }
                        if (idx >= 0) {
                            monthListState.animateScrollToItem(idx)
                        }
                    }
                    LazyRow(
                        state = monthListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("podcast_month_filters")
                    ) {
                        items(
                            items = uiState.monthlyPlaylists,
                            key = { "month_chip_${it.monthNumber}" }
                        ) { playlist ->
                            FilterChip(
                                selected = playlist.monthNumber == uiState.selectedMonthNumber,
                                onClick = { onSelectMonth(playlist.monthNumber) },
                                label = { Text(playlist.monthName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = sacredColors.dawnGoldContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag(
                                    "podcast_filter_${playlist.monthName.lowercase()}"
                                )
                            )
                        }
                    }
                }

                if (selectedMonthPlaylist != null) {
                    item(key = "monthly_orientation_${selectedMonthPlaylist.monthNumber}") {
                        val orientationTrack = PlayablePodcastTrack.MonthlyOrientation(selectedMonthPlaylist)
                        MonthlyOrientationFeatureCard(
                            playlist = selectedMonthPlaylist,
                            onOpen = { onOpenTrack(orientationTrack, false) },
                            onPlay = { onOpenTrack(orientationTrack, true) }
                        )
                    }

                    item(key = "daily_section_header_${selectedMonthPlaylist.monthNumber}") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.podcast_month_daily_title,
                                    selectedMonthPlaylist.monthName
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(
                                    R.string.podcast_daily_count_label,
                                    selectedMonthPlaylist.dailyPodcasts.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = sacredColors.subtleText
                            )
                        }
                    }

                    items(
                        items = selectedMonthPlaylist.dailyPodcasts,
                        key = { daily -> daily.trackId }
                    ) { daily ->
                        val dailyTrack = PlayablePodcastTrack.DailyEpisode(
                            playlist = selectedMonthPlaylist,
                            daily = daily
                        )
                        DailyPodcastTrackCard(
                            monthShortName = selectedMonthPlaylist.monthName.take(3).uppercase(),
                            daily = daily,
                            isToday = daily.dayOfYear == uiState.todayDayOfYear,
                            onOpen = { onOpenTrack(dailyTrack, false) },
                            onPlay = { onOpenTrack(dailyTrack, true) }
                        )
                    }
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
    totalMonthly: Int,
    totalDaily: Int,
    totalTracks: Int
) {
    val sacredColors = LocalSacredColors.current
    val presenterNames = PodcastHost.entries.joinToString(" · ") { it.displayName }

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
                        text = stringResource(
                            R.string.podcast_hero_eyebrow,
                            totalMonthly,
                            totalDaily,
                            totalTracks
                        ),
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

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("podcast_catalog_presenter_roster")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = null,
                            tint = sacredColors.dawnGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.podcast_roster_title),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = presenterNames,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayDailySpotlightCard(
    todayTrack: PlayablePodcastTrack.DailyEpisode,
    onListenNow: () -> Unit,
    onOpenTrack: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val daily = todayTrack.daily
    val presenter = PodcastHost.presenterForTrack(todayTrack)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = sacredColors.dawnGoldContainer,
        border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenTrack)
            .testTag("podcast_today_spotlight_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                        text = stringResource(
                            R.string.podcast_today_spotlight_label,
                            daily.dateString.uppercase(),
                            presenter.displayName.uppercase()
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = daily.durationFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = daily.episodeTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${daily.sacredStep.scriptureReference} · ${daily.sacredStep.truth}",
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
private fun MonthlyOrientationFeatureCard(
    playlist: MonthlyAudioPlaylist,
    onOpen: () -> Unit,
    onPlay: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val orientation = playlist.monthlyOrientationPodcast
    val presenter = PodcastHost.forMonth(playlist.monthNumber)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = sacredColors.sageContainer,
        border = BorderStroke(1.2.dp, sacredColors.sage.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onOpen)
            .testTag("podcast_monthly_orientation_card_${playlist.monthNumber}")
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = sacredColors.sage,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.podcast_monthly_orientation_badge,
                            orientation.trackId,
                            orientation.durationFormatted,
                            orientation.maxLimitMinutes
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "${playlist.monthName} Orientation",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = playlist.themeTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Host: ${presenter.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "· ${playlist.themeScripture}",
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.subtleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                onClick = onPlay,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(50.dp)
                    .testTag("podcast_play_monthly_${playlist.monthNumber}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(
                            R.string.podcast_play_monthly_desc,
                            playlist.monthName
                        ),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyPodcastTrackCard(
    monthShortName: String,
    daily: DailyPodcastTrack,
    isToday: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val presenter = PodcastHost.forDayOfYear(daily.dayOfYear)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isToday) 1.5.dp else 1.dp,
            color = if (isToday) sacredColors.dawnGold else sacredColors.hairline
        ),
        tonalElevation = if (isToday) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onOpen)
            .testTag("podcast_daily_card_${daily.dayOfYear}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isToday) sacredColors.dawnGoldContainer else sacredColors.warmCard
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthShortName,
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold
                    )
                    Text(
                        text = daily.dayOfMonth.toString().padStart(2, '0'),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isToday) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = sacredColors.dawnGoldContainer
                        ) {
                            Text(
                                text = stringResource(R.string.podcast_today_chip),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${presenter.displayName} · ${daily.weekTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Text(
                    text = daily.episodeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = daily.sacredStep.scriptureReference,
                        style = MaterialTheme.typography.bodySmall,
                        color = sacredColors.subtleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = sacredColors.subtleText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = daily.durationFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = sacredColors.subtleText
                        )
                    }
                }
            }

            Surface(
                onClick = onPlay,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("podcast_play_daily_${daily.dayOfYear}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(
                            R.string.podcast_play_daily_desc,
                            daily.dateString,
                            daily.episodeTitle
                        ),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
