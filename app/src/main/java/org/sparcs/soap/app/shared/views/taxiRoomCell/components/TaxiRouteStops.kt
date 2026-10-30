package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TaxiRouteStops(
    source: String,
    destination: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    markerSize: Dp = 14.dp,
    rowSpacing: Dp = 6.dp,
    maxLines: Int = 1,
) {
    val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    val markerInset = ((lineHeight - markerSize) / 2).coerceAtLeast(0.dp)
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val markerColor = MaterialTheme.colorScheme.onSurface

    Column(
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
        modifier = modifier.clearAndSetSemantics { contentDescription = "$source → $destination" }
    ) {
        StopRow(source, style, maxLines, markerSize) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .width(markerSize)
                    .fillMaxHeight()
                    .drawBehind {
                        val gap = 2.dp.toPx()
                        val x = size.width / 2
                        val start = (markerInset + markerSize).toPx() + gap
                        val end = size.height + (rowSpacing + markerInset).toPx() - gap
                        if (end > start) drawLine(lineColor, Offset(x, start), Offset(x, end), strokeWidth = 2.dp.toPx())
                    }
            ) {
                Icon(
                    Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = markerColor,
                    modifier = Modifier
                        .padding(top = markerInset)
                        .size(markerSize)
                )
            }
        }

        StopRow(destination, style, maxLines, markerSize) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = markerInset)
                    .size(markerSize)
            ) {
                Box(
                    Modifier
                        .size(markerSize * 0.6f)
                        .background(markerColor, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun StopRow(
    name: String,
    style: TextStyle,
    maxLines: Int,
    markerSize: Dp,
    marker: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(markerSize / 2 + 4.dp),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        marker()
        Text(name, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(16.dp).width(240.dp)) {
            TaxiRouteStops(source = "대전역", destination = "카이스트 본원")
            TaxiRouteStops(
                source = "KAIST Main Campus",
                destination = "Government Complex Express Bus Terminal",
                style = MaterialTheme.typography.titleLarge,
                markerSize = 16.dp,
                rowSpacing = 8.dp,
                maxLines = 2,
            )
        }
    }
}
