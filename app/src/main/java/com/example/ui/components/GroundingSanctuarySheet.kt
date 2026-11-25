package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AccentScriptStyle
import com.example.ui.theme.LocalSacredColors
import kotlinx.coroutines.delay

/**
 * 1-tap grounding sanctuary accessible from every screen.
 * Designed to slow down panic or cravings with calm pacing and zero judgment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundingSanctuarySheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sacredColors = LocalSacredColors.current

    // 4-4-6 breathing cadence: 0 = Inhale (4s), 1 = Hold (4s), 2 = Exhale (6s)
    var phaseIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            phaseIndex = 0
            delay(4000L)
            phaseIndex = 1
            delay(4000L)
            phaseIndex = 2
            delay(6000L)
        }
    }

    val targetScale = when (phaseIndex) {
        0 -> 1.18f
        1 -> 1.18f
        else -> 0.84f
    }
    val animationDuration = when (phaseIndex) {
        0 -> 3800
        1 -> 600
        else -> 5800
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
        label = "breathingScale"
    )

    val phaseText = when (phaseIndex) {
        0 -> "Breathe in slowly…"
        1 -> "Hold gently…"
        else -> "Release slowly…"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Spa,
                    contentDescription = null,
                    tint = sacredColors.sage,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GROUNDING SANCTUARY",
                    style = MaterialTheme.typography.labelMedium,
                    color = sacredColors.sage
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.grounding_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.grounding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = sacredColors.subtleText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Breathing pulse circle
            Box(
                modifier = Modifier.size(176.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .scale(animatedScale)
                        .background(
                            color = sacredColors.sageContainer,
                            shape = CircleShape
                        )
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, sacredColors.sage),
                    modifier = Modifier.size(116.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Air,
                            contentDescription = "Guided breathing",
                            tint = sacredColors.sage,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = phaseText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Anchor Prayer Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = sacredColors.warmCard,
                border = BorderStroke(1.dp, sacredColors.hairline)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.grounding_prayer_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = sacredColors.dawnGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.grounding_prayer_body),
                        style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5-4-3-2-1 Gentle Grounding Prompts
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, sacredColors.hairline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Five Quiet Anchors Around You",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    GroundingAnchorRow("5", "Notice five ordinary things in the room right now.")
                    GroundingAnchorRow("4", "Feel four solid surfaces—your feet on the floor, your hands resting.")
                    GroundingAnchorRow("3", "Listen for three soft sounds around or outside.")
                    GroundingAnchorRow("2", "Take two slow, unhurried breaths.")
                    GroundingAnchorRow("1", "Remember one truth: you do not have to carry this alone.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = sacredColors.hairline)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = sacredColors.subtleText,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.grounding_support_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = sacredColors.subtleText
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Peace to your next minute.",
                style = AccentScriptStyle,
                color = sacredColors.sage
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("close_grounding_button"),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.action_close_grounding),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun GroundingAnchorRow(number: String, text: String) {
    val sacredColors = LocalSacredColors.current
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = sacredColors.dawnGoldContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
