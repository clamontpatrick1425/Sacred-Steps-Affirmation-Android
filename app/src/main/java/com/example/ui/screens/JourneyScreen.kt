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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Affirmation
import com.example.domain.model.UserProgress
import com.example.domain.model.WorkbookModule
import com.example.ui.theme.AccentScriptStyle
import com.example.ui.theme.LocalSacredColors

@Composable
fun JourneyScreen(
    affirmations: List<Affirmation>,
    userProgress: UserProgress,
    journaledDays: Set<Int>,
    workbookModules: List<WorkbookModule>,
    workbookNotes: Map<String, String>,
    onSelectDay: (Int) -> Unit,
    onSaveWorkbookNote: (moduleId: String, promptKey: String, content: String) -> Unit,
    onOpenUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(1) }
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
                        modifier = Modifier.testTag("journey_tab_workbooks")
                    ) {
                        Text(text = stringResource(R.string.journey_tab_workbooks))
                    }
                }
            }

            if (selectedSection == 0) {
                item {
                    LazyRow(
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
                    val isCurrent = item.dayNumber == userProgress.currentDay
                    val hasJournal = item.dayNumber in journaledDays

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
                                        text = "Week ${item.weekNumber} · ${item.monthName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = sacredColors.subtleText
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
                items(workbookModules, key = { it.id.name }) { module ->
                    WorkbookAccordionCard(
                        module = module,
                        notes = workbookNotes,
                        onSaveNote = onSaveWorkbookNote
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
private fun WorkbookAccordionCard(
    module: WorkbookModule,
    notes: Map<String, String>,
    onSaveNote: (moduleId: String, promptKey: String, content: String) -> Unit
) {
    val sacredColors = LocalSacredColors.current
    var expanded by rememberSaveable(module.id.name) { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.hairline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(20.dp)
                    .testTag("workbook_${module.id.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                module.isCelebratoryOnly -> Icons.Rounded.AutoAwesome
                                module.traumaInformedNotice != null -> Icons.Rounded.Spa
                                else -> Icons.Rounded.Book
                            },
                            contentDescription = null,
                            tint = if (module.isCelebratoryOnly) sacredColors.dawnGold else sacredColors.sage,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${module.workbookNumber} · ${module.subtitle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (module.isCelebratoryOnly) sacredColors.dawnGold else sacredColors.sage
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = sacredColors.subtleText
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse workbook" else "Expand workbook",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    module.traumaInformedNotice?.let { notice ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = sacredColors.sageContainer
                        ) {
                            Text(
                                text = notice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (module.isCelebratoryOnly) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = sacredColors.dawnGoldContainer
                        ) {
                            Text(
                                text = stringResource(R.string.new_creation_notice),
                                style = AccentScriptStyle,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    module.prompts.forEach { prompt ->
                        val compositeKey = "${module.id.name}:${prompt.key}"
                        val savedValue = notes[compositeKey] ?: ""
                        var draft by rememberSaveable(compositeKey, savedValue) {
                            mutableStateOf(savedValue)
                        }

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = sacredColors.warmCard,
                            border = BorderStroke(1.dp, sacredColors.hairline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = prompt.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prompt.prompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = draft,
                                    onValueChange = { draft = it },
                                    placeholder = { Text(prompt.placeholder) },
                                    minLines = 2,
                                    maxLines = 5,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            onSaveNote(module.id.name, prompt.key, draft)
                                        },
                                        modifier = Modifier.height(48.dp),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "Save Note",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
