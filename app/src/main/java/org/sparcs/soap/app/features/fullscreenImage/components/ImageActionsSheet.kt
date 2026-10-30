package org.sparcs.soap.app.features.fullscreenImage.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.features.fullscreenImage.ImageAction
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageActionsSheet(onAction: (ImageAction) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ImageActionsList(onAction = onAction)
    }
}

@Composable
private fun ImageActionsList(onAction: (ImageAction) -> Unit) {
    Column(Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
        ImageAction.entries.forEach { action ->
            ListItem(
                headlineContent = { Text(stringResource(action.label)) },
                leadingContent = { Icon(action.icon, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onAction(action) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageActionsListPreview() {
    Theme { ImageActionsList(onAction = {}) }
}
