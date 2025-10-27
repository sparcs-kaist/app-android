package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Enums.LectureType
import com.example.soap.Domain.Models.OTL.LectureCreditData
import com.example.soap.Domain.Models.OTL.Timetable
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme

val lectureColors = mapOf(
    LectureType.BR to Color(0xFF298DFF),
    LectureType.BE to Color(0xFF3EB74A),
    LectureType.MR to Color(0xFFD2833B),
    LectureType.ME to Color(0xFFBB4ECE),
    LectureType.HSE to Color(0xFFD75050),
    LectureType.ETC to Color(0xFF47D0BA)
)

@Composable
fun TimetableCreditGraph(
    timetable: Timetable,
    modifier: Modifier = Modifier
) {
    val data = listOf(
        LectureCreditData(LectureType.BR, timetable.getCreditsFor(LectureType.BR)),
        LectureCreditData(LectureType.BE, timetable.getCreditsFor(LectureType.BE)),
        LectureCreditData(LectureType.MR, timetable.getCreditsFor(LectureType.MR)),
        LectureCreditData(LectureType.ME, timetable.getCreditsFor(LectureType.ME)),
        LectureCreditData(LectureType.HSE, timetable.getCreditsFor(LectureType.HSE)),
        LectureCreditData(LectureType.ETC, timetable.getCreditsFor(LectureType.ETC))
    )

    val totalCredits = data.sumOf { it.credits }.coerceAtLeast(1)
    val stepCredit = 5
    val tickValues = (0..totalCredits step stepCredit).toList().let {
        if (totalCredits % stepCredit != 0 && totalCredits !in it) it + totalCredits else it
    }

    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
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
                val filteredData = data.filter { it.credits > 0 }
                filteredData.forEachIndexed { index, element ->
                    val blockWidth = (element.credits.toFloat() / totalCredits) * widthPx
                    val isLast = index == filteredData.lastIndex
                    val cornerRadius = when {
                        index == 0 && isLast -> CornerRadius(8f, 8f)
                        index == 0 -> CornerRadius(8f, 0f)
                        isLast -> CornerRadius(0f, 8f)
                        else -> CornerRadius(0f, 0f)
                    }
                    drawRoundRect(
                        color = lectureColors[element.lectureType] ?: Color.Gray.copy(0.5f),
                        topLeft = Offset(startX, 0f),
                        size = Size(blockWidth, heightPx / 2),
                        cornerRadius = cornerRadius
                    )
                    startX += blockWidth
                }

                drawRoundRect(
                    color = Color.Gray.copy(0.5f),
                    style = Stroke(width = 2f),
                    cornerRadius = CornerRadius(8f, 8f),
                    topLeft = Offset(0f, 0f),
                    size = Size(widthPx, heightPx / 2)
                )

                tickValues.forEach { tick ->
                    val x = (tick.toFloat() / totalCredits) * widthPx
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(x, 0f),
                        end = Offset(x, heightPx / 2),
                        strokeWidth = 1f
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "$tick",
                            x,
                            heightPx / 2 + 25f,
                            android.graphics.Paint().apply {
                                textSize = 28f
                                color = android.graphics.Color.DKGRAY
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                data.forEach { element ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = lectureColors[element.lectureType] ?: Color.Gray,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = element.lectureType.name + "(${element.credits})", style = MaterialTheme.typography.bodySmall)

                    }
                }
            }
        }
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
private fun Preview() {
    Theme {
        Column {
            Timetable.mockList().forEach {
                TimetableCreditGraph(it)
            }
        }
    }
}