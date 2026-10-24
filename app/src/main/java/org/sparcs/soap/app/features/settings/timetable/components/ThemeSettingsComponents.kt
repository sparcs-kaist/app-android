package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

@Preview(showBackground = true, locale = "en")
@Preview(showBackground = true, locale = "ko")
@Composable
private fun ThemeSettingsComponentsPreview() {
    Theme {
        Column {
            ThemeSettingsSectionTitle(stringResource(R.string.theme_mine))
        }
    }
}
