package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentScriptStyle
import com.example.ui.theme.LocalSacredColors

val EspProviders = listOf("ConvertKit", "Mailchimp", "Resend")

/**
 * Editorial card inviting readers on Day 8+ to unlock the full 365-day path
 * and receive the complimentary 30-Day Redemption Prayer Guide.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockWalkCard(
    targetDay: Int?,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (email: String, firstName: String, espProvider: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sacredColors = LocalSacredColors.current
    var firstName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var selectedEsp by rememberSaveable { mutableStateOf(EspProviders.first()) }
    var espExpanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, sacredColors.dawnGold.copy(alpha = 0.6f)),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(sacredColors.dawnGold)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = sacredColors.dawnGoldContainer
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockOpen,
                            contentDescription = null,
                            tint = sacredColors.dawnGold,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Text(
                        text = if (targetDay != null) {
                            "DAY $targetDay · STEP BEYOND DAY 7"
                        } else {
                            "SACRED STEPS FULL ACCESS"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = sacredColors.dawnGold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Unlock the Full 365-Day Walk",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Days 1–7 stay open for you anytime. Join our quiet community to unlock all 365 daily steps and receive your free 30-Day Redemption Prayer Guide.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = sacredColors.subtleText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = sacredColors.warmCard,
                    border = BorderStroke(1.dp, sacredColors.hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = sacredColors.dawnGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zero spam. No shame. Unsubscribe anytime.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Your Name (Optional)") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address *") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_email_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = espExpanded,
                    onExpandedChange = {
                        if (!isSubmitting) espExpanded = !espExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedEsp,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isSubmitting,
                        label = { Text("Email Delivery Partner") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = espExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("unlock_esp_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = espExpanded,
                        onDismissRequest = { espExpanded = false }
                    ) {
                        EspProviders.forEach { provider ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = provider,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedEsp = provider
                                    espExpanded = false
                                },
                                modifier = Modifier.testTag("esp_option_${provider.lowercase()}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onSubmit(email, firstName, selectedEsp) },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("join_community_unlock_button"),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Join Community & Unlock",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Welcome home, friend.",
                    style = AccentScriptStyle,
                    color = sacredColors.sage,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
