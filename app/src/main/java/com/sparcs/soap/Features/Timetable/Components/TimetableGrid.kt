package com.sparcs.soap.Features.Timetable.Components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.Domain.Enums.OTL.DayType
import com.sparcs.soap.Domain.Helpers.TimetableConstructor
import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Features.Timetable.TimetableViewModelProtocol
import com.sparcs.soap.Shared.ViewModelMocks.OTL.MockTimetableViewModel
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB

@Composable
fun TimetableGrid(
    viewModel: TimetableViewModelProtocol,
    onLectureSelected: (Lecture) -> Unit = {},
    showDeleteDialog: (Lecture) -> Unit,
) {
    val timetable = viewModel.selectedTimetable.collectAsState().value
    val visibleDays = timetable?.visibleDays ?: DayType.weekdays()
    val candidateLecture by viewModel.candidateLecture.collectAsState()

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val height = maxHeight

        DaysColumnHeader(visibleDays = visibleDays)

        TimesRowHeader(
            minMinutes = timetable?.minMinutes ?: TimetableDefaults.defaultMinMinutes,
            maxMinutes = timetable?.maxMinutes ?: TimetableDefaults.defaultMaxMinutes
        )
        Row(
            modifier = Modifier
                .padding(start = TimetableConstructor.hoursWidth + 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            visibleDays.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    GridHorizontalLines(
                        minMinutes = timetable?.minMinutes ?: TimetableDefaults.defaultMinMinutes,
                        maxMinutes = timetable?.maxMinutes ?: TimetableDefaults.defaultMaxMinutes
                    )

                    timetable?.getLectures(day, candidateLecture)?.forEach { item ->
                        val density = LocalDensity.current
                        val containerHeightPx = with(density) { height.toPx() }
                        val daysHeightPx = with(density) { TimetableConstructor.daysHeight.toPx() }

                        val cellHeight = with(density) {
                            TimetableConstructor.getCellHeightPx(
                                item,
                                containerHeightPx,
                                timetable.duration,
                                daysHeightPx + 24
                            ).toDp()
                        }
                        val cellOffsetY = with(density) {
                            TimetableConstructor.getCellOffsetPx(
                                item,
                                containerHeightPx,
                                timetable.minMinutes,
                                timetable.duration,
                                daysHeightPx + 24
                            ).toDp()
                        }//+24하면 딱 맞는 이유가 뭐지...

                        val isCandidate =
                            item.lecture.id == candidateLecture?.id
                        val animatedAlpha by animateFloatAsState(
                            targetValue = if (viewModel.isLoading.value) 0.5f else 1f,
                            label = "LectureAlpha"
                        )

                        TimetableGridCell(
                            lecture = item.lecture,
                            isCandidate = isCandidate,
                            modifier = Modifier
                                .offset(y = cellOffsetY)
                                .height(cellHeight)
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        onLectureSelected(item.lecture)
                                    },
                                    onLongClick = {
                                        showDeleteDialog(item.lecture)
                                    }
                                )
                                .graphicsLayer { alpha = animatedAlpha }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DaysColumnHeader(visibleDays: List<DayType>) {
    Row(
        modifier = Modifier
            .padding(start = TimetableConstructor.hoursWidth + 8.dp)
            .height(TimetableConstructor.daysHeight)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        visibleDays.forEach { day ->
            Text(
                text = day.stringValue,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimesRowHeader(minMinutes: Int, maxMinutes: Int) {
    val minHour = minMinutes / 60
    val maxHour = maxMinutes / 60

    BoxWithConstraints(
        modifier = Modifier
            .padding(top = TimetableConstructor.daysHeight)
            .width(TimetableConstructor.hoursWidth)
            .fillMaxHeight()
    ) {
        val totalHours = maxHour - minHour
        val spacing = (maxHeight - 15.dp) / totalHours

        (minHour until maxHour).forEachIndexed { index, hour ->
            Text(
                text = hour.toString(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(TimetableConstructor.hoursWidth)
                    .offset(y = spacing * index)
                    .padding(top = 6.dp),

                )
        }
    }
}

@Composable
private fun GridHorizontalLines(minMinutes: Int, maxMinutes: Int) {
    val lineColor = MaterialTheme.colorScheme.grayBB
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = TimetableConstructor.daysHeight + 14.dp)
    ) {
        val duration = maxMinutes - minMinutes
        val spacing = size.height / duration.toFloat() * 60f

        val hours = (minMinutes / 60) until (maxMinutes / 60)
        hours.forEachIndexed { i, _ ->
            val y = i * spacing
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            drawLine(
                color = lineColor,
                start = Offset(0f, y + spacing / 2),
                end = Offset(size.width, y + spacing / 2),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )
        }
    }
}


object TimetableDefaults {
    const val defaultMinMinutes = 540   // 8:00 AM
    const val defaultMaxMinutes = 1080  // 6:00 PM
}


@Preview
@Composable
private fun Preview() {
    Theme {
        TimetableGrid(viewModel = MockTimetableViewModel(), onLectureSelected = {}, showDeleteDialog = {})
    }
}