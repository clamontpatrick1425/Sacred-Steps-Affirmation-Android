package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.LocalSacredColors

/**
 * Modal wrapper around [UnlockWalkCard] when triggered from Settings or Journey pills.
 */
@Composable
fun UnlockJourneyDialog(
    targetDay: Int?,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (email: String, firstName: String, espProvider: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sacredColors = LocalSacredColors.current

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            UnlockWalkCard(
                targetDay = targetDay,
                isSubmitting = isSubmitting,
                errorMessage = errorMessage,
                onSubmit = onSubmit
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("unlock_later_button")
            ) {
                Text(
                    text = stringResource(R.string.unlock_button_later),
                    style = MaterialTheme.typography.labelMedium,
                    color = sacredColors.subtleText
                )
            }
        }
    }
}
