package org.sparcs.soap.app.features.taxiPreview.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiTonalLabel
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.departureCountdown
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.isDepartingSoon
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.isWithinCountdown
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.rememberTaxiClock
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.taxiDayString
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.taxiTime
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Date

@Composable
fun TaxiDepartureHeader(departAt: Date, modifier: Modifier = Modifier) {
    val now = rememberTaxiClock()
    val day = departAt.taxiDayString(now)
    val time = departAt.taxiTime()
    val countdown = if (departAt.isWithinCountdown(now)) departAt.departureCountdown(now) else null

    Column(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = listOfNotNull(day, time, countdown).joinToString(", ")
        }
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (countdown != null) {
                val scheme = MaterialTheme.colorScheme
                val (container, content) =
                    if (departAt.isDepartingSoon(now)) scheme.errorContainer to scheme.onErrorContainer
                    else scheme.tertiaryContainer to scheme.onTertiaryContainer
                TaxiTonalLabel(countdown, container, content, icon = Icons.Rounded.Schedule)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val now = System.currentTimeMillis()
        Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(16.dp)) {
            TaxiDepartureHeader(Date(now + 25 * DateUtils.MINUTE_IN_MILLIS))
            TaxiDepartureHeader(Date(now + 5 * DateUtils.HOUR_IN_MILLIS))
            TaxiDepartureHeader(Date(now + 3 * DateUtils.DAY_IN_MILLIS))
        }
    }
}
