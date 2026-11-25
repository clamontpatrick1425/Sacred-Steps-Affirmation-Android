package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.domain.model.UserProgress
import com.example.ui.theme.LocalSacredColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProgress: UserProgress,
    journalEntryCount: Int,
    onOpenUnlock: () -> Unit,
    onUpdateNotifications: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onSetDarkModeOverride: (Boolean?) -> Unit,
    onPrepareExport: ((String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sacredColors = LocalSacredColors.current
    var showTimePicker by remember { mutableStateOf(false) }
    var permissionDeniedNote by remember { mutableStateOf(false) }
    var pendingExportText by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedNote = false
            onUpdateNotifications(true, userProgress.reminderHour, userProgress.reminderMinute)
        } else {
            permissionDeniedNote = true
            onUpdateNotifications(false, userProgress.reminderHour, userProgress.reminderMinute)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val textToWrite = pendingExportText
        pendingExportText = null
        if (uri != null && textToWrite != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(textToWrite.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val formattedTime = remember(userProgress.reminderHour, userProgress.reminderMinute) {
        val hour12 = when {
            userProgress.reminderHour == 0 -> 12
            userProgress.reminderHour > 12 -> userProgress.reminderHour - 12
            else -> userProgress.reminderHour
        }
        val amPm = if (userProgress.reminderHour >= 12) "PM" else "AM"
        String.format(Locale.getDefault(), "%d:%02d %s", hour12, userProgress.reminderMinute, amPm)
    }

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
                Text(
                    text = "Sanctuary Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
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
                                imageVector = if (userProgress.isPremiumUnlocked) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.LockOpen
                                },
                                contentDescription = null,
                                tint = if (userProgress.isPremiumUnlocked) {
                                    sacredColors.sage
                                } else {
                                    sacredColors.dawnGold
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (userProgress.isPremiumUnlocked) {
                                    "Full 365-Day Journey Unlocked"
                                } else {
                                    "Days 1–7 Open · Unlock All 365 Days"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (userProgress.isPremiumUnlocked) {
                            Text(
                                text = "Registered to ${userProgress.userEmail}. Your 30-Day Redemption Prayer Guide & all 52 weeks are unlocked.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = sacredColors.subtleText
                            )
                        } else {
                            Text(
                                text = "Enter your email anytime to unlock Days 8–365 and receive your complimentary 30-Day Redemption Prayer Guide. Zero spam. No shame.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = sacredColors.subtleText
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onOpenUnlock,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_unlock_button"),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = stringResource(R.string.unlock_button_submit),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_notifications_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_notifications_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = sacredColors.subtleText
                                    )
                                }
                            }
                            Switch(
                                checked = userProgress.notificationsEnabled,
                                onCheckedChange = { enable ->
                                    if (!enable) {
                                        onUpdateNotifications(
                                            false,
                                            userProgress.reminderHour,
                                            userProgress.reminderMinute
                                        )
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            onUpdateNotifications(
                                                true,
                                                userProgress.reminderHour,
                                                userProgress.reminderMinute
                                            )
                                        } else {
                                            notificationPermissionLauncher.launch(
                                                Manifest.permission.POST_NOTIFICATIONS
                                            )
                                        }
                                    } else {
                                        onUpdateNotifications(
                                            true,
                                            userProgress.reminderHour,
                                            userProgress.reminderMinute
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("notifications_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = sacredColors.hairline)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = sacredColors.subtleText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reminder time: $formattedTime",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("change_reminder_time_button"),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("Change", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        if (permissionDeniedNote) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notification permission was declined. You can enable it anytime in your device app settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
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
                        Text(
                            text = stringResource(R.string.settings_export_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You currently have $journalEntryCount private daily reflection(s) stored locally on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = sacredColors.subtleText
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onPrepareExport { text ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Sacred Steps Private Journal")
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Export Sacred Steps Journal")
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_share_button"),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = {
                                    onPrepareExport { text ->
                                        pendingExportText = text
                                        createDocumentLauncher.launch("sacred-steps-journal.txt")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_save_file_button"),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save File", style = MaterialTheme.typography.labelLarge)
                            }
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
                                imageVector = Icons.Rounded.BrightnessMedium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sanctuary Appearance",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = userProgress.darkModeOverride == null,
                                onClick = { onSetDarkModeOverride(null) },
                                label = { Text("System") }
                            )
                            FilterChip(
                                selected = userProgress.darkModeOverride == false,
                                onClick = { onSetDarkModeOverride(false) },
                                label = { Text("Warm Day") }
                            )
                            FilterChip(
                                selected = userProgress.darkModeOverride == true,
                                onClick = { onSetDarkModeOverride(true) },
                                label = { Text("Evening") }
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = sacredColors.warmCard,
                    border = BorderStroke(1.dp, sacredColors.hairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FavoriteBorder,
                                contentDescription = null,
                                tint = sacredColors.sage,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_disclaimer_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.settings_disclaimer_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = sacredColors.subtleText
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = userProgress.reminderHour,
            initialMinute = userProgress.reminderMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = "Choose Reminder Time",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        onUpdateNotifications(
                            true,
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
