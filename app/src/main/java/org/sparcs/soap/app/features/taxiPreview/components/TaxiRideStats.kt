package org.sparcs.soap.app.features.taxiPreview.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoute
import org.sparcs.soap.app.shared.views.contentViews.SkeletonBlock
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.taxiTime
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Date

@Composable
fun TaxiRideStats(departAt: Date, route: TaxiRoute?, modifier: Modifier = Modifier) {
    val shown = route ?: TaxiRoute.placeholder
    val arrival = shown.estimatedArrival(departAt).taxiTime()
    val duration = formatDuration(shown.durationSeconds)
    val distance = formatDistance(shown.distanceMeters)
    val arrivesAround = stringResource(R.string.taxi_arrives_around, arrival)
    val placeholderAlpha by animateFloatAsState(if (route == null) 1f else 0f, label = "statsPlaceholder")

    Box(
        modifier = modifier.clearAndSetSemantics {
            if (route != null) contentDescription = listOf(arrivesAround, duration, distance).joinToString(", ")
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.alpha(1f - placeholderAlpha)
        ) {
            Stat(Icons.Outlined.SportsScore, "~$arrival")
            Stat(Icons.Outlined.DirectionsCar, duration)
            Stat(Icons.Outlined.Route, distance)
        }
        if (placeholderAlpha > 0f) {
            SkeletonBlock(
                Modifier
                    .matchParentSize()
                    .alpha(placeholderAlpha),
                RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun formatDuration(seconds: Int): String {
    val minutes = (seconds + 30) / 60
    return if (minutes < 60) stringResource(R.string.taxi_duration_minutes, minutes)
    else stringResource(R.string.taxi_duration_hours_minutes, minutes / 60, minutes % 60)
}

@Composable
private fun formatDistance(meters: Int): String =
    if (meters < 1000) stringResource(R.string.taxi_distance_m, meters)
    else stringResource(R.string.taxi_distance_km, meters / 1000.0)

@Composable
private fun Stat(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            TaxiRideStats(Date(), TaxiRoute(emptyList(), distanceMeters = 12_400, durationSeconds = 1_260))
            TaxiRideStats(Date(), route = null)
        }
    }
}
