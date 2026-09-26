package org.sparcs.soap.app.features.timetable.creditCalculation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.formatters.formatGPA
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun GPATrendChart(state: CreditCalculationViewState) {
    val points =
        state.semesters.mapNotNull { semester -> state.summary(semester)?.gpa?.let { semester to it } }
    val titles = points.map { semesterTitle(it.first) }
    val labels =
        points.map { "${it.first.year.toString().takeLast(2)}${it.first.semesterType.shortCode}" }
    var selectedID by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedIndex = points.indexOfFirst { it.first.id == selectedID }.takeIf { it >= 0 }
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.background
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 11.sp, color = secondary)
    val valueStyle =
        TextStyle(fontSize = 13.sp, color = secondary, fontWeight = FontWeight.SemiBold)
    val description =
        if (selectedIndex != null) "${titles[selectedIndex]}: ${formatGPA(points[selectedIndex].second)}"
        else points.mapIndexed { index, point -> "${titles[index]}: ${formatGPA(point.second)}" }
            .joinToString()

    CreditCard {
        Text(
            stringResource(R.string.credit_trend), style = MaterialTheme.typography.bodyMedium,
            color = secondary, fontWeight = FontWeight.Medium
        )
        if (points.isEmpty()) {
            Box(Modifier
                .fillMaxWidth()
                .height(180.dp), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.credit_chart_empty),
                    color = secondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .semantics {
                        contentDescription = description
                        customActions = points.mapIndexed { index, point ->
                            CustomAccessibilityAction("${titles[index]}: ${formatGPA(point.second)}") {
                                selectedID = point.first.id
                                true
                            }
                        }
                    }
                    .pointerInput(points) {
                        detectTapGestures { position ->
                            val index =
                                ((position.x - 24.dp.toPx()) / (size.width - 32.dp.toPx()) * points.size)
                                    .toInt().coerceIn(points.indices)
                            selectedID = points[index].first.id.takeUnless { it == selectedID }
                        }
                    }
                    .pointerInput(points) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val index =
                                ((change.position.x - 24.dp.toPx()) / (size.width - 32.dp.toPx()) * points.size)
                                    .toInt().coerceIn(points.indices)
                            selectedID = points[index].first.id
                        }
                    }) {
                val left = 24.dp.toPx()
                val plotWidth = size.width - left - 8.dp.toPx()
                val top = 30.dp.toPx()
                val bottom = size.height - 24.dp.toPx()
                val plotHeight = bottom - top
                fun y(value: Double) = bottom - (value / 4.6 * plotHeight).toFloat()
                fun x(index: Int) = left + plotWidth * (index + 0.5f) / points.size
                for (value in 0..4) {
                    val label = textMeasurer.measure(value.toString(), labelStyle)
                    drawText(
                        label,
                        topLeft = Offset(0f, y(value.toDouble()) - label.size.height / 2)
                    )
                    drawLine(
                        grid,
                        Offset(left, y(value.toDouble())),
                        Offset(size.width, y(value.toDouble())),
                        strokeWidth = 0.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(
                                3.dp.toPx(),
                                3.dp.toPx()
                            )
                        )
                    )
                }
                val offsets =
                    points.mapIndexed { index, point -> Offset(x(index), y(point.second)) }
                selectedIndex?.let { index ->
                    drawLine(
                        secondary.copy(alpha = 0.4f),
                        Offset(x(index), top),
                        Offset(x(index), bottom),
                        1.dp.toPx()
                    )
                }
                offsets.zipWithNext().forEach { (start, end) ->
                    drawLine(primary, start, end, 2.dp.toPx(), cap = StrokeCap.Round)
                }
                offsets.forEach { offset ->
                    drawCircle(surface, 6.dp.toPx(), offset)
                    drawCircle(primary, 4.dp.toPx(), offset)
                }
                val labelStep = ((points.size + 7) / 8).coerceAtLeast(1)
                labels.forEachIndexed { index, label ->
                    if (index % labelStep == 0 || index == labels.lastIndex) {
                        val measured = textMeasurer.measure(label, labelStyle)
                        drawText(
                            measured,
                            topLeft = Offset(
                                x(index) - measured.size.width / 2,
                                bottom + 8.dp.toPx()
                            )
                        )
                    }
                }
                if (selectedIndex == null) {
                    val latest = textMeasurer.measure(formatGPA(points.last().second), valueStyle)
                    drawText(
                        latest, topLeft = Offset(
                            (x(points.lastIndex) - latest.size.width / 2).coerceIn(
                                0f,
                                (size.width - latest.size.width).coerceAtLeast(0f)
                            ),
                            y(points.last().second) - latest.size.height - 10.dp.toPx()
                        )
                    )
                } else {
                    val title = textMeasurer.measure(titles[selectedIndex], labelStyle)
                    val value =
                        textMeasurer.measure(formatGPA(points[selectedIndex].second), valueStyle)
                    val width = maxOf(title.size.width, value.size.width) + 16.dp.toPx()
                    val height = title.size.height + value.size.height + 14.dp.toPx()
                    val tooltipX = (x(selectedIndex) - width / 2).coerceIn(
                        0f,
                        (size.width - width).coerceAtLeast(0f)
                    )
                    drawRoundRect(
                        grid,
                        Offset(tooltipX, 1.dp.toPx()),
                        Size(width, height),
                        CornerRadius(8.dp.toPx())
                    )
                    drawRoundRect(
                        surface,
                        Offset(tooltipX, 0f),
                        Size(width, height - 1.dp.toPx()),
                        CornerRadius(8.dp.toPx())
                    )
                    drawText(title, topLeft = Offset(tooltipX + 8.dp.toPx(), 6.dp.toPx()))
                    drawText(
                        value,
                        topLeft = Offset(tooltipX + 8.dp.toPx(), 8.dp.toPx() + title.size.height)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GPATrendChartPreview() {
    Theme { GPATrendChart(creditPreviewState()) }
}
