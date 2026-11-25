package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.journal.MonthlyTransformationJournal
import com.example.ui.theme.LocalSacredColors
import com.example.util.SacredCalendar

@Composable
fun MonthlyJournalDetailView(
    journal: MonthlyTransformationJournal,
    responses: Map<String, String>,
    onSaveResponse: (monthNumber: Int, entryKey: String, content: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val sacredColors = LocalSacredColors.current
    val todayInfo = remember { SacredCalendar.today() }
    val isCurrentMonth = journal.monthNumber == todayInfo.monthNumber
    val initialDay = if (isCurrentMonth && journal.days.isNotEmpty()) {
        todayInfo.dayOfMonth.coerceIn(1, journal.days.size)
    } else {
        1
    }
    val initialWeek = journal.weeks.firstOrNull { initialDay in it.dayStart..it.dayEnd }?.weekIndex ?: 1

    var selectedTab by rememberSaveable(journal.monthNumber) { mutableIntStateOf(if (isCurrentMonth) 1 else 0) }
    var selectedWeekIndex by rememberSaveable(journal.monthNumber) { mutableIntStateOf(initialWeek) }
    var selectedDayNumber by rememberSaveable(journal.monthNumber) { mutableIntStateOf(initialDay) }

    val activeWeek = journal.weeks.firstOrNull { it.weekIndex == selectedWeekIndex }
        ?: journal.weeks.firstOrNull()
    val weekDays = if (activeWeek != null) {
        journal.days.filter { it.dayNumber in activeWeek.dayStart..activeWeek.dayEnd }
    } else {
        journal.days
    }
    val activeDay = weekDays.firstOrNull { it.dayNumber == selectedDayNumber }
        ?: weekDays.firstOrNull()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("monthly_journal_detail_list_${journal.monthNumber}"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "journal_back_row") {
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
                        modifier = Modifier.testTag("monthly_journal_back_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.monthly_journal_back_desc),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.monthly_journal_all_months),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "${journal.days.size} Guided Days",
                        style = MaterialTheme.typography.labelMedium,
                        color = sacredColors.dawnGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item(key = "journal_cover_hero") {
                MonthlyJournalCoverCard(journal = journal)
            }

            item(key = "journal_section_tabs") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        modifier = Modifier.testTag("journal_tab_welcome")
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_journal_tab_welcome),
                            maxLines = 1
                        )
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        modifier = Modifier.testTag("journal_tab_daily")
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_journal_tab_daily),
                            maxLines = 1
                        )
                    }
                    SegmentedButton(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        modifier = Modifier.testTag("journal_tab_closing")
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_journal_tab_closing),
                            maxLines = 1
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    item(key = "front_matter_section") {
                        MonthlyJournalWelcomeSection(
                            journal = journal,
                            onStartDailyPages = { selectedTab = 1 }
                        )
                    }
                }

                1 -> {
                    item(key = "week_selector_row") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(journal.weeks, key = { "week_${it.weekIndex}" }) { week ->
                                FilterChip(
                                    selected = activeWeek?.weekIndex == week.weekIndex,
                                    onClick = {
                                        selectedWeekIndex = week.weekIndex
                                        selectedDayNumber = week.dayStart
                                    },
                                    label = {
                                        Text(
                                            text = "Week ${week.weekIndex}",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("journal_week_chip_${week.weekIndex}")
                                )
                            }
                        }
                    }

                    if (activeWeek != null) {
                        item(key = "week_intro_${activeWeek.weekIndex}") {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = sacredColors.warmCard,
                                border = BorderStroke(1.dp, sacredColors.hairline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "WEEK ${activeWeek.weekIndex} · DAYS ${activeWeek.dayStart}–${activeWeek.dayEnd}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = sacredColors.dawnGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = activeWeek.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = activeWeek.intro,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    item(key = "day_selector_row") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(weekDays, key = { "day_chip_${it.dayNumber}" }) { day ->
                                val hasWritten = hasDayResponses(
                                    monthNumber = journal.monthNumber,
                                    dayNumber = day.dayNumber,
                                    responses = responses
                                )
                                FilterChip(
                                    selected = activeDay?.dayNumber == day.dayNumber,
                                    onClick = { selectedDayNumber = day.dayNumber },
                                    leadingIcon = if (hasWritten) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = "Has saved reflection",
                                                tint = sacredColors.sage,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    label = {
                                        Text(
                                            text = "Day ${day.dayNumber}",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = sacredColors.dawnGoldContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("journal_day_chip_${day.dayNumber}")
                                )
                            }
                        }
                    }

                    if (activeDay != null) {
                        item(key = "daily_page_${journal.monthNumber}_${activeDay.dayNumber}") {
                            MonthlyJournalDailyPageCard(
                                monthNumber = journal.monthNumber,
                                day = activeDay,
                                responses = responses,
                                onSaveResponse = onSaveResponse
                            )
                        }
                    }

                    if (activeWeek != null) {
                        item(key = "week_checkin_${journal.monthNumber}_${activeWeek.weekIndex}") {
                            JournalPromptInputCard(
                                monthNumber = journal.monthNumber,
                                entryKey = "week_${activeWeek.weekIndex}_checkin",
                                badge = "END OF WEEK ${activeWeek.weekIndex} CHECK-IN",
                                title = "Pause & Reflect on ${activeWeek.title}",
                                prompt = activeWeek.checkInPrompt,
                                placeholder = "Record how God has met you across Week ${activeWeek.weekIndex}…",
                                savedValue = responses["${journal.monthNumber}:week_${activeWeek.weekIndex}_checkin"].orEmpty(),
                                onSave = onSaveResponse
                            )
                        }
                    }
                }

                else -> {
                    item(key = "end_of_month_section") {
                        MonthlyJournalClosingSection(
                            journal = journal,
                            responses = responses,
                            onSaveResponse = onSaveResponse
                        )
                    }
                }
            }

            item(key = "journal_bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MonthlyJournalCoverCard(journal: MonthlyTransformationJournal) {
    val sacredColors = LocalSacredColors.current
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_journal_cover_${journal.monthNumber}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        text = "MONTH ${journal.monthNumber.toString().padStart(2, '0')} · ${journal.monthName.uppercase()} TRANSFORMATION JOURNAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "${journal.monthName}: ${journal.themeTitle}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = journal.subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = journal.themeScripture,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

private fun hasDayResponses(
    monthNumber: Int,
    dayNumber: Int,
    responses: Map<String, String>
): Boolean {
    val prefix = "$monthNumber:day_${dayNumber}_"
    return responses.any { (key, value) -> key.startsWith(prefix) && value.isNotBlank() }
}
