package org.sparcs.soap.app.features.taxiPreview.components

import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoute
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

/** Stands in for the figures while the route loads: a typical ride's, only ever shown as a placeholder. */
private const val PLACEHOLDER_DURATION_SECONDS = 18 * 60
private const val PLACEHOLDER_DISTANCE_METERS = 8_900

/**
 * How long the taxi takes to reach the riders once it is called, which the route's driving time
 * leaves out: the middle of the usual two to five minutes.
 */
private const val PICKUP_WAIT_SECONDS = 3.5 * 60

/**
 * The ride's estimated arrival, driving time and distance, from the route. Until the route
 * arrives ([route] is null) it shows placeholders of the same size, so the sheet doesn't shift when
 * the figures come in.
 */
@Composable
fun TaxiRideStats(departAt: Date, route: TaxiRoute?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isLoading = route == null
    val duration = route?.durationSeconds ?: PLACEHOLDER_DURATION_SECONDS
    val distance = route?.distanceMeters ?: PLACEHOLDER_DISTANCE_METERS

    val arrival = remember(departAt, duration) {
        // Rounded to the nearest minute, since the time is shown without seconds.
        val arrivesAt = departAt.time + ((duration + PICKUP_WAIT_SECONDS) * 1000).roundToLong()
        val minute = 60_000L
        DateFormat.getTimeFormat(context).format(Date((arrivesAt + minute / 2) / minute * minute))
    }
    val durationText = remember(duration) { formatDuration(duration) }
    val distanceText = remember(distance) { formatDistance(distance) }
    val arrivalLabel = stringResource(R.string.taxi_arrives_around, arrival)

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clearAndSetSemantics {
            if (!isLoading) contentDescription = "$arrivalLabel, $durationText, $distanceText"
        },
    ) {
        Stat(Icons.Outlined.SportsScore, "~$arrival", isLoading)
        Stat(Icons.Outlined.DirectionsCar, durationText, isLoading)
        Stat(Icons.Outlined.Route, distanceText, isLoading)
    }
}

@Composable
private fun Stat(icon: ImageVector, text: String, isLoading: Boolean) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val content by animateColorAsState(if (isLoading) Color.Transparent else color, label = "stat")
    val placeholder by animateColorAsState(
        if (isLoading) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
        label = "placeholder",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(placeholder, MaterialTheme.shapes.small),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
    }
}

/** "18 min", or "1 hr, 5 min" for longer rides. */
private fun formatDuration(seconds: Int): String {
    val minutes = ((seconds + 30) / 60).coerceAtLeast(1)
    val format = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
    return if (minutes < 60) {
        format.format(Measure(minutes, MeasureUnit.MINUTE))
    } else {
        val measures = listOfNotNull(
            Measure(minutes / 60, MeasureUnit.HOUR),
            (minutes % 60).takeIf { it > 0 }?.let { Measure(it, MeasureUnit.MINUTE) },
        )
        format.formatMeasures(*measures.toTypedArray())
    }
}

/** "850 m" under a kilometre, otherwise "8.9 km". */
private fun formatDistance(meters: Int): String {
    val locale = Locale.getDefault()
    val number = NumberFormat.getInstance(locale).apply { maximumFractionDigits = 1 }
    val format = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT, number)
    return if (meters < 1_000) format.format(Measure(meters, MeasureUnit.METER))
    else format.format(Measure(meters / 1_000.0, MeasureUnit.KILOMETER))
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TaxiRideStats(Date(), TaxiRoute(emptyList(), distanceMeters = 12_400, durationSeconds = 23 * 60))
                TaxiRideStats(Date(), null)
            }
        }
    }
}
