package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun ThemePhotoSection(onPick: () -> Unit) {
    Column {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onPick),
            leadingContent = {
                Icon(
                    Icons.Outlined.Photo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            headlineContent = {
                Text(stringResource(R.string.theme_photo_generate))
            }
        )
        Text(
            text = stringResource(R.string.theme_photo_description),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun ThemePhotoProgressDialog(onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.theme_photo_generating)) },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}

@Composable
internal fun ThemePhotoErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_photo_error_title)) },
        text = { Text(stringResource(R.string.theme_photo_error)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}

@Preview(showBackground = true)
@Composable
private fun ThemePhotoSectionPreview() {
    Theme { Column(Modifier.padding(16.dp)) { ThemePhotoSection {} } }
}

@Preview(showBackground = true)
@Composable
private fun ThemePhotoProgressPreview() {
    Theme {
        var showDialog by remember { mutableStateOf(false) }
        Button(onClick = { showDialog = true }, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.theme_photo_generating))
        }
        if (showDialog) {
            ThemePhotoProgressDialog(onCancel = { showDialog = false })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemePhotoErrorPreview() {
    Theme {
        var showDialog by remember { mutableStateOf(false) }
        Button(onClick = { showDialog = true }, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.theme_photo_error_title))
        }
        if (showDialog) {
            ThemePhotoErrorDialog(onDismiss = { showDialog = false })
        }
    }
}
