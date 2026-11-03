package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun DiscardPostDialog(isEditing: Boolean, onDiscard: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isEditing) R.string.compose_discard_changes else R.string.compose_discard_post)) },
        text = { Text(stringResource(if (isEditing) R.string.compose_discard_changes_message else R.string.discard_this_post)) },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(stringResource(R.string.exit), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.keep_editing))
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    Theme { DiscardPostDialog(isEditing = false, onDiscard = {}, onDismiss = {}) }
}

@Preview
@Composable
private fun EditingPreview() {
    Theme { DiscardPostDialog(isEditing = true, onDiscard = {}, onDismiss = {}) }
}
