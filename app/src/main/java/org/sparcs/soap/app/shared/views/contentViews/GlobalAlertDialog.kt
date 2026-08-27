package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.theme.ui.Theme

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
            containerColor = MaterialTheme.colorScheme.background,
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

@Preview(showBackground = true)
@Composable
private fun GlobalAlertDialogPreview() {
    Theme {
        var isPresented by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { isPresented = true }) {
                Text("Show Alert Dialog")
            }

            GlobalAlertDialog(
                isPresented = isPresented,
                state = AlertState(
                    titleResId = R.string.error,
                    messageResId = R.string.error_unknown_try_again,
                    message = "404 Not Found"
                ),
                onDismiss = { isPresented = false }
            )
        }
    }
}