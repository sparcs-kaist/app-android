package org.sparcs.soap.App.Shared.Views.ContentViews

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.R

@Composable
fun GlobalAlertDialog(
    isPresented: Boolean,
    state: AlertState?,
    onDismiss: () -> Unit
) {
    if (isPresented && state != null) {
        val mainDescription = state.messageResId?.let { stringResource(it) }
            ?: stringResource(R.string.error_unknown_try_again)

        val fullMessage = if (!state.message.isNullOrBlank()) {
            "$mainDescription (${state.message})"
        } else {
            mainDescription
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(state.titleResId)) },
            text = {
                Text(fullMessage)
            }
        )
    }
}