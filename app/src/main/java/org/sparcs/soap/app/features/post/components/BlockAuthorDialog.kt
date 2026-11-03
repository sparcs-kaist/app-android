package org.sparcs.soap.app.features.post.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun BlockAuthorDialog(nickname: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (nickname.isBlank()) stringResource(R.string.block_author_confirm_title)
                else stringResource(R.string.block_author_named_confirm_title, nickname)
            )
        },
        text = { Text(stringResource(R.string.block_author_confirm_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(R.string.block_author)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun BlockAuthorDialogPreview() {
    Theme { BlockAuthorDialog(nickname = "신나는 포도", onConfirm = {}, onDismiss = {}) }
}
