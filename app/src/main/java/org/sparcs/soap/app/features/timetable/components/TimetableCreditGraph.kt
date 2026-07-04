package org.sparcs.soap.app.features.timetable.components

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.app.domain.enums.otl.LectureType
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.theme_light_background

val lectureColors = mapOf(
    LectureType.BR to Color(0xFF298DFF),
    LectureType.BE to Color(0xFF3EB74A),
    LectureType.MR to Color(0xFFFD8D02),
    LectureType.ME to Color(0xFFA765A3),
    LectureType.HSE to Color(0xFFE2455C),
    LectureType.ETC to Color(0xFF47D0BA)
)

@Composable
fun TimetableCreditGraph(
    timetable: Timetable,
    modifier: Modifier = Modifier,
) {
    val lectureTypes = listOf(
        LectureType.BR, LectureType.BE, LectureType.MR,
        LectureType.ME, LectureType.HSE, LectureType.ETC
    )

    val animatedCredits = lectureTypes.map { type ->
        animateFloatAsState(
            targetValue = timetable.getCreditsFor(type).toFloat(),
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            label = "credit_${type.name}"
        )
    }

    val animatedTotal = animatedCredits.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    val isLight = MaterialTheme.colorScheme.background == theme_light_background
    val density = LocalDensity.current
    val tickTextSize = with(density) { 11.sp.toPx() }
    val tickOffset = with(density) { 14.dp.toPx() }
    
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier
            .fillMaxWidth()
            .glassBorder(shape = RoundedCornerShape(20.dp))
    ) {
        Column(modifier = modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                val widthPx = size.width
                val heightPx = size.height

                var startX = 0f

                lectureTypes.forEachIndexed { index, type ->
                    val credits = animatedCredits[index].value
                    if (credits > 0) {
                        val blockWidth = (credits / animatedTotal) * widthPx

                        val isFirst = startX == 0f
                        val isLast = (startX + blockWidth) >= widthPx - 1f

                        drawRoundRect(
                            color = lectureColors[type] ?: Color.Gray.copy(0.5f),
                            topLeft = Offset(startX, 0f),
                            size = Size(blockWidth, heightPx / 2),
                            cornerRadius = when {
                                isFirst && isLast -> CornerRadius(8f, 8f)
                                isFirst -> CornerRadius(8f, 0f)
                                isLast -> CornerRadius(0f, 8f)
                                else -> CornerRadius(0f, 0f)
                            }
                        )
                        startX += blockWidth
                    }
                }

                drawRoundRect(
                    color = Color.Gray.copy(0.5f),
                    style = Stroke(width = 2f),
                    cornerRadius = CornerRadius(8f, 8f),
                    topLeft = Offset(0f, 0f),
                    size = Size(widthPx, heightPx / 2)
                )

                val stepCredit = 5
                val actualTotal = animatedTotal.toInt()
                val tickValues = (0..actualTotal step stepCredit).toMutableList()
                if (actualTotal > 0 && actualTotal % stepCredit != 0) {
                    tickValues.add(actualTotal)
                }

                tickValues.forEach { tick ->
                    val x = (tick.toFloat() / animatedTotal) * widthPx
                    drawLine(
                        color = if(isLight) Color.DarkGray.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(x, 0f),
                        end = Offset(x, heightPx / 2),
                        strokeWidth = 1f
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "$tick",
                            x,
                            heightPx / 2 + tickOffset,
                            Paint().apply {
                                textSize = tickTextSize
                                color = if(isLight) android.graphics.Color.GRAY else android.graphics.Color.LTGRAY
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                lectureTypes.forEachIndexed { index, type ->
                    val currentCredits = animatedCredits[index].value
                    if (currentCredits > 0 || timetable.getCreditsFor(type) > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = lectureColors[type] ?: Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${stringResource(type.labelRes)}(${currentCredits.toInt()})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        Column {
            Timetable.mockList().forEach {
                TimetableCreditGraph(it)
            }
        }
    }
}