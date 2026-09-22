package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TimetableTheme.displayName(): String = if (!isBuiltIn) name else stringResource(
    when (id) {
        "builtin.legacy" -> R.string.theme_legacy
        "builtin.olive" -> R.string.theme_olive
        "builtin.cherryBlossom" -> R.string.theme_cherryblossom
        "builtin.spring" -> R.string.theme_spring
        "builtin.summer" -> R.string.theme_summer
        "builtin.autumn" -> R.string.theme_autumn
        "builtin.winter" -> R.string.theme_winter
        "builtin.ocean" -> R.string.theme_ocean
        "builtin.sunset" -> R.string.theme_sunset
        "builtin.monochrome" -> R.string.theme_monochrome
        else -> R.string.theme_default
    }
)

@Composable
internal fun ThemeSettingsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
internal fun ThemeSettingsAction(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showChevron: Boolean = true,
) {
    val color =
        if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
            alpha = 0.38f
        )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
        Spacer(Modifier.width(8.dp))
        if (showChevron) Icon(
            Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun ThemeSectionTitle(title: String) {
    Text(
        title,
        Modifier.padding(top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true, locale = "en")
@Preview(showBackground = true, locale = "ko")
@Composable
private fun ThemeSettingsComponentsPreview() {
    Theme {
        Column {
            ThemeSettingsSectionTitle(stringResource(R.string.theme_mine))
            ThemeSettingsAction(stringResource(R.string.theme_new), Icons.Default.Add, {})
            ThemeSectionTitle(stringResource(R.string.theme_palette))
        }
    }
}
