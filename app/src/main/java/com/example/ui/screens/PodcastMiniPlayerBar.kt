package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.podcast.PodcastPlaybackStatus
import com.example.podcast.PodcastUiState
import com.example.ui.theme.LocalSacredColors

/**
 * Persistent bottom mini-player docked above navigation whenever an audio track
 * is active in the background via PodcastPlaybackService.
 */
@Composable
fun PodcastMiniPlayerBar(
    uiState: PodcastUiState,
    onTogglePlayPause: () -> Unit,
    onExpandPlayer: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = uiState.selectedTrack ?: return
    val sacredColors = LocalSacredColors.current
    val activeHost = uiState.activeTurn?.host ?: uiState.leadHost
    val activeSubtitle = uiState.activeTurn?.let {
        "${activeHost.displayName} · ${it.sectionLabel}"
    } ?: track.subtitle

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onExpandPlayer)
            .testTag("podcast_mini_player_bar"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = stringResource(R.string.podcast_mini_expand_desc),
                    tint = sacredColors.dawnGold,
                    modifier = Modifier.size(22.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = sacredColors.dawnGold,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activeSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("podcast_mini_play_pause")
                ) {
                    when (uiState.playbackStatus) {
                        PodcastPlaybackStatus.SYNTHESIZING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.2.dp,
                                color = sacredColors.dawnGold
                            )
                        }
                        PodcastPlaybackStatus.PLAYING -> {
                            Icon(
                                imageVector = Icons.Rounded.Pause,
                                contentDescription = stringResource(R.string.podcast_pause),
                                tint = sacredColors.dawnGold
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = stringResource(R.string.podcast_play),
                                tint = sacredColors.dawnGold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("podcast_mini_close")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.podcast_mini_close_desc),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { uiState.turnProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = sacredColors.dawnGold,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
            )
        }
    }
}
