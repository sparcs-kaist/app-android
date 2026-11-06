@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package org.sparcs.soap.app.features.taxiPreview.components

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.formattedDate
import org.sparcs.soap.app.shared.extensions.isDateInToday
import org.sparcs.soap.app.shared.extensions.isDateInTomorrow
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiTonalLabel
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.isDepartingSoon
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.rememberCurrentMinute
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.taxiCountdown
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.lastSeatContainer
import org.sparcs.soap.app.theme.ui.onLastSeatContainer
import java.util.Date

/**
 * When the ride leaves: the day on one line ("Tomorrow · Mon, October 26"), the time large beneath
 * it, and, once departure is under 12 hours away, how long until then beside the time — in a warm
 * colour within the hour.
 */
@Composable
fun TaxiDepartureHeader(departAt: Date, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val now by rememberCurrentMinute()
    val time = remember(departAt) { DateFormat.getTimeFormat(context).format(departAt) }
    val day = remember(departAt, now) {
        // "Today" and "Tomorrow" come localized from the platform.
        val relativeDay = if (isDateInToday(departAt) || isDateInTomorrow(departAt)) {
            DateUtils.getRelativeTimeSpanString(departAt.time, now, DateUtils.DAY_IN_MILLIS).toString()
        } else null
        listOfNotNull(relativeDay, departAt.formattedDate()).joinToString(" · ")
    }
    val countdown = remember(departAt, now) { taxiCountdown(departAt, now) }
    val label = stringResource(R.string.depart_at)

    Column(
        modifier.clearAndSetSemantics {
            contentDescription = listOfNotNull(label, time, day, countdown).joinToString(", ")
        }
    ) {
        Text(
            day,
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                time,
                style = MaterialTheme.typography.displayMediumEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (countdown != null) {
                val scheme = MaterialTheme.colorScheme
                val soon = isDepartingSoon(departAt, now)
                TaxiTonalLabel(
                    text = countdown,
                    container = if (soon) scheme.lastSeatContainer else scheme.tertiaryContainer,
                    content = if (soon) scheme.onLastSeatContainer else scheme.onTertiaryContainer,
                    icon = Icons.Outlined.Schedule,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                TaxiDepartureHeader(Date(System.currentTimeMillis() + 25 * 60 * 1000))
                TaxiDepartureHeader(Date(System.currentTimeMillis() + 5 * 60 * 60 * 1000))
                TaxiDepartureHeader(Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000))
            }
        }
    }
}
