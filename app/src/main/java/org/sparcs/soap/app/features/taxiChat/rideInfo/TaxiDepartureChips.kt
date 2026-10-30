package org.sparcs.soap.app.features.taxiChat.rideInfo

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaxiDepartureChips(departAt: Date, modifier: Modifier = Modifier) {
    val now = rememberTaxiClock()
    val scheme = MaterialTheme.colorScheme

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        TaxiTonalLabel(departAt.taxiDayString(now), scheme.surfaceContainerHighest, scheme.onSurfaceVariant, Icons.Outlined.CalendarToday)
        TaxiTonalLabel(departAt.taxiTime(), scheme.surfaceContainerHighest, scheme.onSurface, Icons.Outlined.Schedule)
        if (departAt.isWithinCountdown(now)) {
            val (container, content) =
                if (departAt.isDepartingSoon(now)) scheme.errorContainer to scheme.onErrorContainer
                else scheme.tertiaryContainer to scheme.onTertiaryContainer
            TaxiTonalLabel(departAt.departureCountdown(now), container, content, Icons.Outlined.Timer)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val now = System.currentTimeMillis()
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            TaxiDepartureChips(Date(now + 12 * DateUtils.MINUTE_IN_MILLIS))
            TaxiDepartureChips(Date(now + 5 * DateUtils.HOUR_IN_MILLIS))
            TaxiDepartureChips(Date(now + 3 * DateUtils.DAY_IN_MILLIS))
        }
    }
}
