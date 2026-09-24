package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.timetable.TimetableLoadState
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme
import java.text.DateFormat
import java.util.Date

@Composable
fun TimetableOfflineStatus(state: TimetableLoadState, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    ElevatedCard(
        modifier = modifier.fillMaxWidth().glassBorder(shape = RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.timetable_offline),
                style = MaterialTheme.typography.bodyMedium,
            )
            state.lastUpdated?.let {
                Text(
                    stringResource(R.string.timetable_last_updated, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(it)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(locale = "ko", showBackground = true)
@Preview(locale = "en", showBackground = true)
@Composable
private fun TimetableOfflineStatusPreview() {
    Theme {
        TimetableOfflineStatus(TimetableLoadState(isOffline = true, isShowingSavedData = true, lastUpdated = Date(0)))
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableOfflineCacheMissPreview() {
    Theme {
        TimetableOfflineStatus(TimetableLoadState(isOffline = true))
    }
}
