package org.sparcs.soap.widgets.buddyTimetableWidget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.features.settings.components.SettingsGroup
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.theme.ui.Theme

/** Whether the timetable widget draws its classes as silhouettes, as a settings switch row. */
@Composable
internal fun WidgetSilhouetteRow(enabled: Boolean, onChange: (Boolean) -> Unit) {
    SettingsSwitchItem(
        text = stringResource(R.string.widget_silhouette),
        supportingText = stringResource(R.string.widget_silhouette_description),
        checked = enabled,
        onCheckedChange = onChange,
        leadingContent = { Icon(Icons.Outlined.ViewWeek, contentDescription = null) },
    )
}

@Preview(showBackground = true)
@Composable
private fun WidgetSilhouetteRowPreview() {
    Theme { SettingsGroup { row { WidgetSilhouetteRow(true, {}) } } }
}
