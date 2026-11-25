package com.sacredsteps.app.audio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sacredsteps.app.audio.viewmodel.PlayerUiState

val SacredDeepBlue = Color(0xFF1C2A39)
val SacredGold = Color(0xFFD4AF37)
val SacredSand = Color(0xFFF9F6F0)
val SacredSage = Color(0xFF7A8B7B)

@Composable
fun MiniPlayerBar(
    uiState: PlayerUiState,
    onTogglePlayPause: () -> Unit,
    onExpandPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onExpandPlayer() },
        shape = RoundedCornerShape(12.dp),
        color = SacredDeepBlue,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.activeTrackTitle,
                    color = SacredGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = uiState.activeTrackSubtitle,
                    color = SacredSand.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = SacredGold
                )
            }
        }
    }
}

@Composable
fun FullPodcastPlayerSheet(
    uiState: PlayerUiState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBack: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SacredDeepBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = SacredSand)
                }
                Text("SACRED STEPS AUDIO", color = SacredGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Episode Badge & Title
            Surface(
                color = SacredGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = uiState.activeTrackSubtitle,
                    color = SacredGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.activeTrackTitle,
                color = SacredSand,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scrubber
            val duration = if (uiState.durationMs > 0) uiState.durationMs.toFloat() else 1f
            val currentPos = uiState.currentPositionMs.coerceIn(0L, uiState.durationMs).toFloat()

            Slider(
                value = currentPos,
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..duration,
                colors = SliderDefaults.colors(
                    thumbColor = SacredGold,
                    activeTrackColor = SacredGold,
                    inactiveTrackColor = SacredSand.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(uiState.currentPositionMs), color = SacredSand.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(formatDuration(uiState.durationMs), color = SacredSand.copy(alpha = 0.7f), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Player Controls (15s back, Play/Pause, 15s forward)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSkipBack, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Default.Replay, contentDescription = "Rewind 15s", tint = SacredSand, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(20.dp))
                FloatingActionButton(
                    onClick = onTogglePlayPause,
                    containerColor = SacredGold,
                    contentColor = SacredDeepBlue,
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                IconButton(onClick = onSkipForward, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Default.Forward10, contentDescription = "Forward 15s", tint = SacredSand, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback Speed Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.8f, 1.0f, 1.2f).forEach { speed ->
                    FilterChip(
                        selected = (uiState.playbackSpeed == speed),
                        onClick = { onSetSpeed(speed) },
                        label = { Text("${speed}x", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SacredGold,
                            selectedLabelColor = SacredDeepBlue,
                            containerColor = SacredDeepBlue,
                            labelColor = SacredSand
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Synchronized Sacred S.T.E.P. Card (If Daily Episode)
            uiState.activeDailyDetails?.let { daily ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SacredSand.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TODAY\'S SACRED S.T.E.P.", color = SacredGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• SCRIPTURE: ${daily.stepDetails.scripture}", color = SacredSand, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• TRUTH: ${daily.stepDetails.truth}", color = SacredSand, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• AFFIRMATION: \"${daily.stepDetails.affirmation}\"", color = SacredGold, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• SACRED STEP: ${daily.stepDetails.step}", color = SacredSand, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
