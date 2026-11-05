@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package org.sparcs.soap.app.features.taxiPreview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/**
 * Origin over destination, joined by a line: the origin marked with a location arrow, the
 * destination with a filled dot. Each marker sits level with its name's first line, so names can
 * wrap and the markers still line up with the text.
 */
@Composable
fun TaxiRouteStops(
    source: String,
    destination: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLargeEmphasized,
    markerSize: Dp = 20.dp,
    rowSpacing: Dp = 8.dp,
) {
    var column by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var sourceMarker by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var destinationMarker by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val lineColor = MaterialTheme.colorScheme.outline
    val lineInset = with(LocalDensity.current) { 4.dp.toPx() }
    val sourceLabel = stringResource(R.string.source)
    val destinationLabel = stringResource(R.string.destination)

    Column(
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
        modifier = modifier
            .clearAndSetSemantics { contentDescription = "$sourceLabel $source, $destinationLabel $destination" }
            .onGloballyPositioned { column = it }
            // One line between the two markers, however many lines the names take.
            .drawBehind {
                val space = column?.takeIf { it.isAttached } ?: return@drawBehind
                val from = sourceMarker?.takeIf { it.isAttached }?.let { space.localBoundingBoxOf(it) } ?: return@drawBehind
                val to = destinationMarker?.takeIf { it.isAttached }?.let { space.localBoundingBoxOf(it) } ?: return@drawBehind
                val top = from.bottom + lineInset
                val bottom = to.top - lineInset
                if (bottom > top) {
                    drawLine(
                        lineColor,
                        Offset(from.center.x, top),
                        Offset(from.center.x, bottom),
                        strokeWidth = 2.dp.toPx(),
                    )
                }
            },
    ) {
        Stop(source, style, markerSize, onMarkerPlaced = { sourceMarker = it }) {
            Icon(
                Icons.Rounded.NearMe,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(markerSize),
            )
        }
        Stop(destination, style, markerSize, onMarkerPlaced = { destinationMarker = it }) {
            // A dot as large as the arrow reads heavier, so it is drawn smaller in the same space.
            Box(Modifier.size(markerSize), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(markerSize * 0.55f)
                        .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun Stop(
    name: String,
    style: TextStyle,
    markerSize: Dp,
    onMarkerPlaced: (LayoutCoordinates) -> Unit,
    marker: @Composable () -> Unit,
) {
    val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // As tall as the name's first line, so the marker is centred on it.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = markerSize, height = lineHeight)
                .onGloballyPositioned(onMarkerPlaced),
        ) {
            marker()
        }
        Text(name, style = style)
    }
}

@Preview(showBackground = true, widthDp = 280)
@Composable
private fun Preview() {
    Theme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                TaxiRouteStops(source = "대전역", destination = "카이스트 본원")
                TaxiRouteStops(
                    source = "KAIST Main Campus",
                    destination = "Government Complex Express Bus Terminal",
                )
            }
        }
    }
}
