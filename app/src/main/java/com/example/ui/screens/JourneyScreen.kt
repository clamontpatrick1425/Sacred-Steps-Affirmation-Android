package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Affirmation
import com.example.domain.model.UserProgress
import com.example.journal.MonthlyTransformationJournal
import com.example.ui.theme.LocalSacredColors
import com.example.util.SacredCalendar

@Composable
fun JourneyScreen(
    affirmations: List<Affirmation>,
    userProgress: UserProgress,
    journaledDays: Set<Int>,
    monthlyJournals: List<MonthlyTransformationJournal>,
    monthlyJournalResponses: Map<String, String>,
    onSelectDay: (Int) -> Unit,
    onSaveMonthlyJournalResponse: (monthNumber: Int, entryKey: String, content: String) -> Unit,
    onOpenUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayInfo = remember { SacredCalendar.today() }
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(todayInfo.monthNumber) }
    var openedJournalMonth by rememberSaveable { mutableStateOf<Int?>(null) }
    val monthRowState = rememberLazyListState(
        initialFirstVisibleItemIndex = (todayInfo.monthNumber - 1).coerceAtLeast(0)
    )

    val openedJournal = openedJournalMonth?.let { monthNum ->
        monthlyJournals.firstOrNull { it.monthNumber == monthNum }
    }
    if (openedJournal != null) {
        MonthlyJournalDetailView(
            journal = openedJournal,
            responses = monthlyJournalResponses,
            onSaveResponse = onSaveMonthlyJournalResponse,
            onBack = { openedJournalMonth = null },
            modifier = modifier
        )
        return
    }

    val sacredColors = LocalSacredColors.current
    val monthNames = listOf(
        1 to "Jan", 2 to "Feb", 3 to "Mar", 4 to "Apr",
        5 to "May", 6 to "Jun", 7 to "Jul", 8 to "Aug",
        9 to "Sep", 10 to "Oct", 11 to "Nov", 12 to "Dec"
    )
    val filteredDays = affirmations.filter { it.monthNumber == selectedMonth }

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
                JourneyHeroCard(
                    daysCompleted = userProgress.daysCompleted,
                    journalCount = journaledDays.size,
                    isPremiumUnlocked = userProgress.isPremiumUnlocked,
                    onOpenUnlock = onOpenUnlock
                )
            }

            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("journey_tab_365")
                    ) {
                        Text(text = stringResource(R.string.journey_tab_calendar))
                    }
                    SegmentedButton(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("journey_tab_journals")
                    ) {
                        Text(text = stringResource(R.string.journey_tab_journals))
                    }
                }
            }

            if (selectedSection == 0) {
                item {
                    LazyRow(
                        state = monthRowState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(monthNames) { (monthNum, shortName) ->
                            FilterChip(
                                selected = selectedMonth == monthNum,
                                onClick = { selectedMonth = monthNum },
                                label = {
                                    Text(
                                        text = "M$monthNum · $shortName",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("month_chip_$monthNum")
                            )
                        }
                    }
                }

                items(filteredDays, key = { it.dayNumber }) { item ->
                    val isToday = item.dayNumber == todayInfo.dayOfYear365
                    val isCurrent = item.dayNumber == userProgress.currentDay || isToday
                    val hasJournal = item.dayNumber in journaledDays
                    val dayDateInfo = remember(item.dayNumber) {
                        SacredCalendar.fromDayOfYear365(item.dayNumber)
                    }

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isCurrent) sacredColors.warmCard else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) sacredColors.dawnGold else sacredColors.hairline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onSelectDay(item.dayNumber) }
                            .testTag("journey_day_${item.dayNumber}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (item.isCompleted) {
                                        sacredColors.sageContainer
                                    } else {
                                        sacredColors.warmCard
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${item.dayNumber}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = buildString {
                                            if (isToday) append("Today · ")
                                            append("${dayDateInfo.weekdayShort}, ${dayDateInfo.monthShortName} ${dayDateInfo.dayOfMonth} · Week ${item.weekNumber}")
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isToday) sacredColors.dawnGold else sacredColors.subtleText,
                                        fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hasJournal) {
                                    Icon(
                                        imageVector = Icons.Rounded.EditNote,
                                        contentDescription = "Has private journal entry",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (item.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Walked in grace",
                                        tint = sacredColors.sage,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (item.isLocked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Unlock 365-day path",
                                        tint = sacredColors.dawnGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item(key = "monthly_journals_intro") {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = sacredColors.warmCard,
                        border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoStories,
                                    contentDescription = null,
                                    tint = sacredColors.dawnGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.monthly_journals_intro_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = stringResource(R.string.monthly_journals_intro_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = sacredColors.subtleText
                            )
                        }
                    }
                }

                items(monthlyJournals, key = { "monthly_journal_${it.monthNumber}" }) { journal ->
                    val writtenCount = monthlyJournalResponses.count { (key, value) ->
                        key.startsWith("${journal.monthNumber}:") && value.isNotBlank()
                    }
                    MonthlyJournalCard(
                        journal = journal,
                        isCurrentMonth = journal.monthNumber == todayInfo.monthNumber,
                        writtenEntriesCount = writtenCount,
                        onOpen = { openedJournalMonth = journal.monthNumber }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun JourneyHeroCard(
    daysCompleted: Int,
    journalCount: Int,
    isPremiumUnlocked: Boolean,
    onOpenUnlock: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    val progressFraction = (daysCompleted / 365f).coerceIn(0f, 1f)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.hairline)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_journey_path),
                    contentDescription = "Peaceful stepping stone trail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.days_walked_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "$daysCompleted of 365 Steps · $journalCount Journaled Days",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = sacredColors.sage,
                    trackColor = sacredColors.sageContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "One honest day at a time. Missed days never erase your progress.",
                        style = MaterialTheme.typography.bodySmall,
                        color = sacredColors.subtleText,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isPremiumUnlocked) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onOpenUnlock,
                            modifier = Modifier.height(48.dp),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Unlock All",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyJournalCard(
    journal: MonthlyTransformationJournal,
    isCurrentMonth: Boolean,
    writtenEntriesCount: Int,
    onOpen: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (isCurrentMonth) sacredColors.warmCard else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isCurrentMonth) 1.5.dp else 1.dp,
            color = if (isCurrentMonth) sacredColors.dawnGold else sacredColors.hairline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onOpen)
            .testTag("monthly_journal_card_${journal.monthNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (writtenEntriesCount > 0) sacredColors.sageContainer else sacredColors.dawnGoldContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = journal.monthName.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (writtenEntriesCount > 0) sacredColors.sage else sacredColors.dawnGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = journal.monthNumber.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = buildString {
                        if (isCurrentMonth) append("Current Month · ")
                        append("${journal.monthName} · ${journal.days.size} Days · ${journal.weeks.size} Weeks")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = journal.themeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = journal.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = sacredColors.subtleText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (writtenEntriesCount > 0) {
                    Text(
                        text = "$writtenEntriesCount private reflection${if (writtenEntriesCount == 1) "" else "s"} saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.sage,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open ${journal.monthName} ${journal.themeTitle} Journal",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
