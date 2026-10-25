package org.sparcs.soap.app.features.taxiPreview.components

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.formattedDate
import org.sparcs.soap.app.shared.extensions.isDateInToday
import org.sparcs.soap.app.shared.extensions.isDateInTomorrow
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiTonalLabel
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Date

/**
 * When the room leaves: the time large, in the system's 12- or 24-hour style, the day under it
 * ("Tomorrow · Mon, October 26"), and, within a day, how long until then on the right.
 */
@Composable
fun DepartureTimeRow(departAt: Date, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }
    val time = remember(departAt) { DateFormat.getTimeFormat(context).format(departAt) }
    val day = remember(departAt) {
        // "Today" and "Tomorrow" come localized from the platform.
        val relativeDay = if (isDateInToday(departAt) || isDateInTomorrow(departAt)) {
            DateUtils.getRelativeTimeSpanString(departAt.time, now, DateUtils.DAY_IN_MILLIS).toString()
        } else null
        listOfNotNull(relativeDay, departAt.formattedDate()).joinToString(" · ")
    }
    // "In 45 minutes" or "In 3 hours"; further out, the day line already says when.
    val untilDeparture = departAt.time - now
    val countdown = remember(departAt) {
        if (untilDeparture <= 0 || untilDeparture >= DateUtils.DAY_IN_MILLIS) null
        else DateUtils.getRelativeTimeSpanString(
            departAt.time, now,
            if (untilDeparture < DateUtils.HOUR_IN_MILLIS) DateUtils.MINUTE_IN_MILLIS else DateUtils.HOUR_IN_MILLIS
        ).toString()
    }
    val label = stringResource(R.string.depart_at)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = listOfNotNull(label, time, day, countdown).joinToString(", ") }
    ) {
        Icon(
            Icons.Outlined.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(time, style = MaterialTheme.typography.titleLarge)
            Text(
                day,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (countdown != null) {
            TaxiTonalLabel(
                text = countdown,
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        DepartureTimeRow(Date(System.currentTimeMillis() + 25 * 60 * 1000))
    }
}
