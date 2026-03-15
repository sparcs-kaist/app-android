package org.sparcs.soap.App.Features.Timetable.Components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Helpers.TimetableConstructor
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.soap.App.Shared.ViewModelMocks.OTL.MockTimetableViewModel
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB

@Composable
fun TimetableGrid(
    viewModel: TimetableViewModelProtocol,
    onLectureSelected: (Lecture) -> Unit = {},
    showDeleteDialog: (Lecture) -> Unit,
) {
    val timetable = viewModel.timetableUseCase?.selectedTimetableObject?.collectAsState()?.value
    val visibleDays = timetable?.visibleDays ?: DayType.weekdays()

    val candidateLecture by viewModel.candidateLecture.collectAsState()
    val times = buildList {
        timetable?.lectures?.forEach { addAll(it.classTimes) }
        candidateLecture?.let { addAll(it.classTimes) }
    }

    val minMinutes = times.minOfOrNull { it.begin }?.let { (it / 60) * 60 } ?: timetable?.minMinutes ?: TimetableDefaults.defaultMinMinutes
    val maxMinutes = times.maxOfOrNull { it.end }?.let { ((it / 60) + 1) * 60 } ?: timetable?.gappedMaxMinutes ?: TimetableDefaults.defaultMaxMinutes

    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val height = maxHeight

        DaysColumnHeader(visibleDays = visibleDays)

        TimesRowHeader(
            minMinutes = minMinutes,
            maxMinutes = maxMinutes
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
                        .padding(start = 2.dp, end = 2.dp)
                ) {
                    GridHorizontalLines(
                        minMinutes = minMinutes,
                        maxMinutes = maxMinutes
                    )

                    timetable?.getLectures(day, candidateLecture)?.forEach { item ->
                        val density = LocalDensity.current
                        val containerHeightPx = with(density) { height.toPx() }
                        val daysHeightPx = with(density) { TimetableConstructor.daysHeight.toPx() }

                        val cellHeight = with(density) {
                            TimetableConstructor.getCellHeightPx(
                                item,
                                containerHeightPx,
                                maxMinutes - minMinutes,
                                daysHeightPx + 24
                            ).toDp()
                        }
                        val cellOffsetY = with(density) {
                            TimetableConstructor.getCellOffsetPx(
                                item,
                                containerHeightPx,
                                minMinutes,
                                maxMinutes - minMinutes,
                                daysHeightPx + 24
                            ).toDp()
                        }//+24하면 딱 맞는 이유가 뭐지...

                        val animatedCellHeight by animateDpAsState(
                            targetValue = cellHeight,
                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                            label = "HeightAnimation"
                        )

                        val animatedCellOffsetY by animateDpAsState(
                            targetValue = cellOffsetY,
                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                            label = "OffsetAnimation"
                        )

                        val isCandidate =
                            item.lecture.id == candidateLecture?.id
                        val animatedAlpha by animateFloatAsState(
                            targetValue = if (viewModel.isLoading.collectAsState().value) 0.5f else 1f,
                            label = "LectureAlpha"
                        )

                        TimetableGridCell(
                            lecture = item.lecture,
                            isCandidate = isCandidate,
                            cellHeight = animatedCellHeight,
                            modifier = Modifier
                                .offset(y = animatedCellOffsetY)
                                .height(animatedCellHeight)
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                        onLectureSelected(item.lecture)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                text = stringResource(day.stringValue),
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
                color = MaterialTheme.colorScheme.onSurface,
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
    val animatedMinMinutes by animateIntAsState(
        targetValue = minMinutes,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "MinMinutesAnim"
    )
    val animatedMaxMinutes by animateIntAsState(
        targetValue = maxMinutes,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "MaxMinutesAnim"
    )
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = TimetableConstructor.daysHeight + 14.dp)
    ) {
        val duration = animatedMaxMinutes - animatedMinMinutes
        if (duration <= 0) return@Canvas

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