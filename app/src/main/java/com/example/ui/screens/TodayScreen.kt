package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Affirmation
import com.example.domain.model.HeartMood
import com.example.domain.model.JournalEntry
import com.example.domain.model.UserProgress
import com.example.ui.components.UnlockWalkCard
import com.example.ui.theme.AccentScriptStyle
import com.example.ui.theme.AffirmationSerifStyle
import com.example.ui.theme.LocalSacredColors
import com.example.util.SacredCalendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
    affirmation: Affirmation?,
    userProgress: UserProgress,
    journalEntries: List<JournalEntry>,
    isUnlockSubmitting: Boolean,
    unlockErrorMessage: String?,
    onSelectDay: (Int) -> Unit,
    onSelectHeartMood: (HeartMood) -> Unit,
    onToggleWalked: () -> Unit,
    onSaveJournal: (Int, String) -> Unit,
    onDeleteJournal: (Int) -> Unit,
    onSubmitUnlock: (email: String, firstName: String, espProvider: String) -> Unit,
    onOpenUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sacredColors = LocalSacredColors.current
    var journalDraft by rememberSaveable(affirmation?.dayNumber) { mutableStateOf("") }
    val dateFormatter = remember { SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()) }

    if (affirmation == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Preparing your sanctuary…",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    val selectedMood = HeartMood.entries.find { it.label == userProgress.lastHeartMood }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SanctuaryHeaderBanner(
                    affirmation = affirmation,
                    userProgress = userProgress
                )
            }

            item {
                val todayInfo = remember { SacredCalendar.today() }
                DayNavigationStrip(
                    dayNumber = affirmation.dayNumber,
                    todayDayNumber = todayInfo.dayOfYear365,
                    isPremiumUnlocked = userProgress.isPremiumUnlocked,
                    onPrevious = { onSelectDay(affirmation.dayNumber - 1) },
                    onNext = { onSelectDay(affirmation.dayNumber + 1) },
                    onJumpToToday = { onSelectDay(todayInfo.dayOfYear365) },
                    onOpenUnlock = onOpenUnlock
                )
            }

            if (affirmation.isLocked) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = affirmation.title,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Day ${affirmation.dayNumber} is part of the full 365-day walk.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = sacredColors.subtleText
                        )
                    }
                }

                item {
                    UnlockWalkCard(
                        targetDay = affirmation.dayNumber,
                        isSubmitting = isUnlockSubmitting,
                        errorMessage = unlockErrorMessage,
                        onSubmit = onSubmitUnlock
                    )
                }

                item {
                    val todayDay = remember { SacredCalendar.today().dayOfYear365 }
                    OutlinedButton(
                        onClick = { onSelectDay(todayDay) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("return_to_today_button"),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, sacredColors.hairline)
                    ) {
                        Text(
                            text = "Return to Today",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, sacredColors.hairline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.heart_checkin_prompt),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HeartMood.entries.forEach { mood ->
                                    val selected = mood == selectedMood
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectHeartMood(mood) },
                                        label = {
                                            Text(
                                                text = mood.label,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = sacredColors.sageContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.testTag("heart_mood_${mood.name.lowercase()}")
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = selectedMood != null,
                                enter = fadeIn() + expandVertically()
                            ) {
                                selectedMood?.let { mood ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = sacredColors.sageContainer
                                    ) {
                                        Text(
                                            text = mood.gentleResponse,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = affirmation.title,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "The Sacred S.T.E.P. Method",
                                style = AccentScriptStyle,
                                color = sacredColors.sage
                            )
                            OutlinedButton(
                                onClick = {
                                    val shareBody = buildString {
                                        appendLine("Sacred Steps to Redemption · Day ${affirmation.dayNumber} of 365")
                                        appendLine(affirmation.title)
                                        appendLine()
                                        appendLine(affirmation.scripture)
                                        appendLine()
                                        appendLine("“${affirmation.affirmationText}”")
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Day ${affirmation.dayNumber}: ${affirmation.title}")
                                        putExtra(Intent.EXTRA_TEXT, shareBody)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(sendIntent, "Share Today's Step")
                                    )
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("share_today_button"),
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.7f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Share today's affirmation",
                                    tint = sacredColors.dawnGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share Today",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    StepMethodCard(
                        stepBadge = stringResource(R.string.step_label_scripture),
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        body = affirmation.scripture,
                        accentColor = MaterialTheme.colorScheme.primary,
                        isSerif = true
                    )
                }

                item {
                    StepMethodCard(
                        stepBadge = stringResource(R.string.step_label_truth),
                        icon = Icons.Rounded.Lightbulb,
                        body = affirmation.truthReframe,
                        accentColor = sacredColors.sage
                    )
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = sacredColors.warmCard,
                        border = BorderStroke(1.dp, sacredColors.hairline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .fillMaxHeight()
                                    .background(sacredColors.dawnGold)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.WbSunny,
                                        contentDescription = null,
                                        tint = sacredColors.dawnGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.step_label_embrace),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = sacredColors.dawnGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "“${affirmation.affirmationText}”",
                                    style = AffirmationSerifStyle,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    StepMethodCard(
                        stepBadge = stringResource(R.string.step_label_practice),
                        icon = Icons.Rounded.Favorite,
                        body = affirmation.practiceAction,
                        accentColor = sacredColors.sage
                    )
                }

                item {
                    val walked = affirmation.isCompleted
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (walked) sacredColors.sageContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (walked) sacredColors.sage else sacredColors.hairline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (walked) {
                                        "Day ${affirmation.dayNumber} · Walked in Grace"
                                    } else {
                                        "Honor today's step"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${userProgress.daysCompleted} total days walked in grace",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = sacredColors.subtleText
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = onToggleWalked,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("mark_day_walked_button"),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (walked) sacredColors.sage else MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (walked) {
                                        Icons.Rounded.CheckCircle
                                    } else {
                                        Icons.Rounded.RadioButtonUnchecked
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (walked) "Walked" else "Complete",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, sacredColors.hairline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.reflection_section_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = affirmation.reflectionPrompt,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = journalDraft,
                                onValueChange = { journalDraft = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.journal_placeholder),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                minLines = 4,
                                maxLines = 8,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("journal_entry_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Stored privately on this device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = sacredColors.subtleText
                                )
                                Button(
                                    onClick = {
                                        if (journalDraft.isNotBlank()) {
                                            onSaveJournal(affirmation.dayNumber, journalDraft)
                                            journalDraft = ""
                                        }
                                    },
                                    enabled = journalDraft.isNotBlank(),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("save_journal_button"),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = stringResource(R.string.action_save_journal),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

                if (journalEntries.isNotEmpty()) {
                    item {
                        Text(
                            text = "Your Reflections for Day ${affirmation.dayNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(journalEntries, key = { it.id }) { entry ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = sacredColors.warmCard,
                            border = BorderStroke(1.dp, sacredColors.hairline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = dateFormatter.format(Date(entry.timestamp)),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = sacredColors.subtleText
                                        )
                                        if (entry.heartMood.isNotBlank()) {
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = sacredColors.sageContainer
                                            ) {
                                                Text(
                                                    text = entry.heartMood,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteJournal(entry.id) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("delete_journal_${entry.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete reflection",
                                            tint = sacredColors.subtleText
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    color = sacredColors.hairline,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                                Text(
                                    text = entry.entryText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SanctuaryHeaderBanner(
    affirmation: Affirmation,
    userProgress: UserProgress
) {
    val sacredColors = LocalSacredColors.current
    val greeting = when {
        userProgress.isReturningAfterGap -> stringResource(R.string.welcome_back_greeting)
        userProgress.userName.isNotBlank() -> "Welcome back, ${userProgress.userName}. Today is a fresh step."
        else -> "Peace for today's step."
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, sacredColors.hairline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_sanctuary_dawn),
                contentDescription = "Warm dawn over calm sage hills",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.86f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val dateInfo = remember(affirmation.dayNumber) {
                    SacredCalendar.fromDayOfYear365(affirmation.dayNumber)
                }
                val isToday = remember(affirmation.dayNumber) {
                    affirmation.dayNumber == SacredCalendar.today().dayOfYear365
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = buildString {
                            if (isToday) append("Today · ")
                            append("${dateInfo.weekdayFull}, ${dateInfo.monthName} ${dateInfo.dayOfMonth} · Week ${affirmation.weekNumber} of 52")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sacred Steps to Redemption",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayNavigationStrip(
    dayNumber: Int,
    todayDayNumber: Int,
    isPremiumUnlocked: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToToday: () -> Unit,
    onOpenUnlock: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val nextLocked = dayNumber < 365 && !isPremiumUnlocked && !SacredCalendar.isDayInFreeWindow(dayNumber + 1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = dayNumber > 1,
            modifier = Modifier
                .height(48.dp)
                .testTag("prev_day_button"),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, sacredColors.hairline)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "Previous day",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Prev", style = MaterialTheme.typography.labelLarge)
        }

        if (dayNumber != todayDayNumber) {
            Surface(
                onClick = onJumpToToday,
                shape = MaterialTheme.shapes.small,
                color = sacredColors.sageContainer,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .testTag("jump_to_today_button")
            ) {
                Text(
                    text = "Back to Today",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        } else if (!isPremiumUnlocked) {
            Surface(
                onClick = onOpenUnlock,
                shape = MaterialTheme.shapes.small,
                color = sacredColors.dawnGoldContainer,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .testTag("unlock_pill_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = sacredColors.dawnGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unlock 365 Days",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Text(
                text = "Day $dayNumber of 365",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedButton(
            onClick = onNext,
            enabled = dayNumber < 365,
            modifier = Modifier
                .height(48.dp)
                .testTag("next_day_button"),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, sacredColors.hairline)
        ) {
            Text(
                text = "Next",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (nextLocked) {
                    Icons.Rounded.Lock
                } else {
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight
                },
                contentDescription = "Next day",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StepMethodCard(
    stepBadge: String,
    icon: ImageVector,
    body: String,
    accentColor: Color,
    isSerif: Boolean = false
) {
    val sacredColors = LocalSacredColors.current
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.hairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stepBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = body,
                style = if (isSerif) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
