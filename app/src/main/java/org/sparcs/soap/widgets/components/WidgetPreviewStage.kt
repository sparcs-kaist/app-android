package org.sparcs.soap.widgets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader

/**
 * The setup screen's Preview section: a tonal panel behind the widget, like the launcher's widget
 * picker, so the widget stands out from the page without a shadow or outline. It is inset like the
 * settings groups below it.
 */
@Composable
fun WidgetPreviewStage(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        SettingsSectionHeader(stringResource(R.string.preview))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.extraLarge)
                .padding(24.dp)
        ) {
            content()
        }
    }
}
