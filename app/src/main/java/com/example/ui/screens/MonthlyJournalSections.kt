package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.journal.MonthlyJournalDay
import com.example.journal.MonthlyTransformationJournal
import com.example.ui.theme.AccentScriptStyle
import com.example.ui.theme.LocalSacredColors
import com.example.util.SacredCalendar

@Composable
internal fun MonthlyJournalWelcomeSection(
    journal: MonthlyTransformationJournal,
    onStartDailyPages: () -> Unit
) {
    val sacredColors = LocalSacredColors.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, sacredColors.hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "WELCOME LETTER",
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Welcome to Your ${journal.monthName} Journey",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = journal.welcomeLetter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = sacredColors.warmCard,
            border = BorderStroke(1.dp, sacredColors.hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MONTHLY THEME INTRODUCTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.sage,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${journal.themeTitle}: ${journal.subtitle}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = journal.themeIntro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, sacredColors.hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "THE SACRED S.T.E.P. METHOD™",
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.SemiBold
                )
                StepMethodGuideRow("S — Scripture", "Anchor your mind and heart in God's Word (KJV).")
                StepMethodGuideRow("T — Truth", "Name the lie, fear, or shame and dismantle it with truth.")
                StepMethodGuideRow("E — Embrace", "Speak your personal \"I am\" declaration out loud.")
                StepMethodGuideRow("P — Practice", "Take one honest, doable step in real life today.")
            }
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = sacredColors.dawnGoldContainer,
            border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "OPENING PRAYER FOR ${journal.monthName.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = journal.openingPrayer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onStartDailyPages,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("journal_begin_daily_pages_button"),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Begin Day 1 of ${journal.monthName}")
                }
            }
        }
    }
}

@Composable
private fun StepMethodGuideRow(title: String, body: String) {
    val sacredColors = LocalSacredColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = sacredColors.subtleText
        )
    }
}

@Composable
internal fun MonthlyJournalDailyPageCard(
    monthNumber: Int,
    day: MonthlyJournalDay,
    responses: Map<String, String>,
    onSaveResponse: (monthNumber: Int, entryKey: String, content: String) -> Unit
) {
    val sacredColors = LocalSacredColors.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Day Header + Scripture + Meaning + Application
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, sacredColors.hairline),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_journal_day_card_${monthNumber}_${day.dayNumber}")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val weekday = SacredCalendar.weekdayForMonthDay(monthNumber, day.dayNumber)
                Text(
                    text = "DAY ${day.dayNumber} · ${weekday.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = day.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = sacredColors.warmCard,
                    border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "S · SCRIPTURE (KJV)",
                            style = MaterialTheme.typography.labelSmall,
                            color = sacredColors.dawnGold,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = day.scripture,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "What This Means",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = day.whatThisMeans,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = sacredColors.hairline)

                Text(
                    text = "How I Apply This Today",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = day.howIApplyToday,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Sacred S.T.E.P. Method Breakdown
        Surface(
            shape = MaterialTheme.shapes.large,
            color = sacredColors.warmCard,
            border = BorderStroke(1.dp, sacredColors.hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "THE SACRED S.T.E.P. METHOD™",
                    style = MaterialTheme.typography.labelMedium,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "T · Truth",
                            style = MaterialTheme.typography.labelMedium,
                            color = sacredColors.sage,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lie: ${day.lieReplaced}",
                            style = MaterialTheme.typography.bodySmall,
                            color = sacredColors.subtleText
                        )
                        Text(
                            text = "Truth: ${day.truthAnchor}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = sacredColors.dawnGoldContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "E · Embrace (Speak Out Loud)",
                            style = MaterialTheme.typography.labelMedium,
                            color = sacredColors.dawnGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = day.embraceAffirmation,
                            style = AccentScriptStyle,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = sacredColors.sageContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "P · Practice (One Micro-Step Today)",
                            style = MaterialTheme.typography.labelMedium,
                            color = sacredColors.sage,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = day.practiceAction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Personal "In my own words" Affirmation Input
        JournalPromptInputCard(
            monthNumber = monthNumber,
            entryKey = "day_${day.dayNumber}_embrace",
            badge = "EMBRACE IN YOUR OWN WORDS",
            title = "My Personal \"I Am\" Declaration",
            prompt = "Rewrite today's affirmation in your own honest words:",
            placeholder = "In my own words, I am…",
            savedValue = responses["$monthNumber:day_${day.dayNumber}_embrace"].orEmpty(),
            onSave = onSaveResponse
        )

        // 3 Daily Reflection Questions
        day.reflectionQuestions.forEachIndexed { idx, question ->
            val qKey = "day_${day.dayNumber}_q${idx + 1}"
            JournalPromptInputCard(
                monthNumber = monthNumber,
                entryKey = qKey,
                badge = "REFLECTION QUESTION ${idx + 1} OF ${day.reflectionQuestions.size}",
                title = question,
                prompt = "",
                placeholder = "Write honestly here without shame or rush…",
                savedValue = responses["$monthNumber:$qKey"].orEmpty(),
                onSave = onSaveResponse
            )
        }

        // Daily Prayer
        Surface(
            shape = MaterialTheme.shapes.large,
            color = sacredColors.dawnGoldContainer,
            border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = sacredColors.dawnGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "DAY ${day.dayNumber} PRAYER",
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = day.prayer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Open Journal Space
        JournalPromptInputCard(
            monthNumber = monthNumber,
            entryKey = "day_${day.dayNumber}_journal",
            badge = "JOURNAL SPACE",
            title = "Free Reflection for Day ${day.dayNumber}",
            prompt = "Use this space to pray on paper, release a burden, or record what shifted in your heart today.",
            placeholder = "Write freely here…",
            savedValue = responses["$monthNumber:day_${day.dayNumber}_journal"].orEmpty(),
            minLines = 3,
            onSave = onSaveResponse
        )
    }
}

@Composable
internal fun MonthlyJournalClosingSection(
    journal: MonthlyTransformationJournal,
    responses: Map<String, String>,
    onSaveResponse: (monthNumber: Int, entryKey: String, content: String) -> Unit
) {
    val sacredColors = LocalSacredColors.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = sacredColors.warmCard,
            border = BorderStroke(1.dp, sacredColors.hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "END OF MONTH TRANSFORMATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = journal.endOfMonthTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Take a quiet breath and look back over ${journal.monthName} with grace, never guilt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = sacredColors.subtleText
                )
            }
        }

        journal.endOfMonthPrompts.forEach { prompt ->
            val key = "eom_${prompt.key}"
            JournalPromptInputCard(
                monthNumber = journal.monthNumber,
                entryKey = key,
                badge = "MONTHLY REFLECTION",
                title = prompt.title,
                prompt = prompt.helper,
                placeholder = "Write your reflection…",
                savedValue = responses["${journal.monthNumber}:$key"].orEmpty(),
                onSave = onSaveResponse
            )
        }

        // Final Monthly Challenge — Sacred Declaration
        Surface(
            shape = MaterialTheme.shapes.large,
            color = sacredColors.dawnGoldContainer,
            border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = sacredColors.dawnGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "FINAL MONTHLY CHALLENGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = sacredColors.dawnGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = journal.declarationTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                journal.declarationAnchors.forEach { anchor ->
                    Text(
                        text = "• $anchor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        JournalPromptInputCard(
            monthNumber = journal.monthNumber,
            entryKey = "declaration_personal",
            badge = "MY PERSONAL DECLARATION",
            title = "Write Your Own \"I Am\" Statements",
            prompt = "Add the truths God has written on your heart this month:",
            placeholder = "I am…\nI am…\nI am…",
            savedValue = responses["${journal.monthNumber}:declaration_personal"].orEmpty(),
            minLines = 3,
            onSave = onSaveResponse
        )

        // My Support Plan
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, sacredColors.hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = sacredColors.sage,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.monthly_journal_support_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = sacredColors.sageContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.monthly_journal_support_crisis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                SupportPlanField(
                    monthNumber = journal.monthNumber,
                    entryKey = "support_friend",
                    label = "Trusted Friend or Mentor",
                    savedValue = responses["${journal.monthNumber}:support_friend"].orEmpty(),
                    onSave = onSaveResponse
                )
                SupportPlanField(
                    monthNumber = journal.monthNumber,
                    entryKey = "support_sponsor",
                    label = "Sponsor or Recovery Group Leader",
                    savedValue = responses["${journal.monthNumber}:support_sponsor"].orEmpty(),
                    onSave = onSaveResponse
                )
                SupportPlanField(
                    monthNumber = journal.monthNumber,
                    entryKey = "support_counselor",
                    label = "Counselor, Therapist, or Pastor",
                    savedValue = responses["${journal.monthNumber}:support_counselor"].orEmpty(),
                    onSave = onSaveResponse
                )
                SupportPlanField(
                    monthNumber = journal.monthNumber,
                    entryKey = "support_safe_action",
                    label = "Safe Place or Grounding Action When Struggling",
                    savedValue = responses["${journal.monthNumber}:support_safe_action"].orEmpty(),
                    onSave = onSaveResponse
                )
            }
        }

        // Closing Message & Closing Prayer
        Surface(
            shape = MaterialTheme.shapes.large,
            color = sacredColors.warmCard,
            border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CLOSING MESSAGE & PRAYER",
                    style = MaterialTheme.typography.labelSmall,
                    color = sacredColors.dawnGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = journal.closingMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = sacredColors.hairline)
                Text(
                    text = journal.closingPrayer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SupportPlanField(
    monthNumber: Int,
    entryKey: String,
    label: String,
    savedValue: String,
    onSave: (monthNumber: Int, entryKey: String, content: String) -> Unit
) {
    var draft by rememberSaveable(monthNumber, entryKey, savedValue) { mutableStateOf(savedValue) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (draft != savedValue) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onSave(monthNumber, entryKey, draft) },
                    modifier = Modifier.height(48.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = stringResource(R.string.monthly_journal_save_button))
                }
            }
        }
    }
}

@Composable
internal fun JournalPromptInputCard(
    monthNumber: Int,
    entryKey: String,
    badge: String,
    title: String,
    prompt: String,
    placeholder: String,
    savedValue: String,
    minLines: Int = 2,
    onSave: (monthNumber: Int, entryKey: String, content: String) -> Unit
) {
    val sacredColors = LocalSacredColors.current
    var draft by rememberSaveable(monthNumber, entryKey, savedValue) {
        mutableStateOf(savedValue)
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                color = sacredColors.dawnGold,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (prompt.isNotBlank()) {
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = sacredColors.subtleText
                )
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(placeholder) },
                minLines = minLines,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_input_${monthNumber}_$entryKey")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onSave(monthNumber, entryKey, draft) },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("journal_save_${monthNumber}_$entryKey"),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(R.string.monthly_journal_save_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
