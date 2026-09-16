package org.sparcs.soap.app.features.timetable.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableConstructor
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun TimetableGrid(
    viewModel: TimetableViewModelProtocol,
    onLectureSelected: (Lecture) -> Unit = {},
    showDeleteDialog: (Lecture) -> Unit,
    onEditActivity: (TimetableActivity) -> Unit = {},
) {
    val timetable by viewModel.selectedTimetable.collectAsState()
    val isEditable by viewModel.isEditable.collectAsState()

    val visibleDays = timetable?.visibleDays ?: DayType.weekdays()

    val candidateLecture by viewModel.candidateLecture.collectAsState()
    val times = buildList {
        timetable?.lectures?.forEach { addAll(it.classes) }
        candidateLecture?.let { addAll(it.classes) }
    }

    val minMinutes =
        ((times.map { it.begin } + timetable?.activities.orEmpty().map { it.begin }).minOrNull()
            ?: TimetableDefaults.DEFAULT_MIN_MINUTES) / 60 * 60
    val maxMinutes =
        ((times.map { it.end } + timetable?.activities.orEmpty().map { it.end }).maxOrNull()
            ?.let { (it / 60 + 1) * 60 } ?: TimetableDefaults.DEFAULT_MAX_MINUTES).coerceAtLeast(
            minMinutes + 60
        )

    val haptic = LocalHapticFeedback.current
    var selectedActivity by remember { mutableStateOf<TimetableActivity?>(null) }
    var activityActions by remember { mutableStateOf(false) }


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

                    timetable?.activities?.filter { it.day == day.value }?.forEach { activity ->
                        val top = TimetableConstructor.daysHeight + 14.dp
                        val usable = (height - top).coerceAtLeast(0.dp)
                        val activityHeight =
                            (usable * ((activity.end - activity.begin).toFloat() / (maxMinutes - minMinutes)) - 4.dp).coerceAtLeast(
                                1.dp
                            )
                        Column(
                            Modifier
                                .offset(y = top + usable * ((activity.begin - minMinutes).toFloat() / (maxMinutes - minMinutes)))
                                .height(activityHeight)
                                .fillMaxWidth()
                                .background(
                                    LocalTimetableTheme.current.colorFor(activity.id),
                                    RoundedCornerShape(4.dp)
                                )
                                .combinedClickable(onClick = {
                                    selectedActivity = activity; activityActions = false
                                }, onLongClick = if (isEditable) { {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedActivity = activity; activityActions = true
                                } } else null)
                                .padding(5.dp)
                        ) {
                            Text(
                                activity.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalTimetableTheme.current.textColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (activity.location.isNotBlank()) Text(
                                activity.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalTimetableTheme.current.textColor.copy(alpha = .8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    timetable?.getLectures(day, candidateLecture)?.forEach { item ->
                        val top = TimetableConstructor.daysHeight + 14.dp
                        val usable = (height - top).coerceAtLeast(0.dp)
                        val cellHeight =
                            (usable * (item.lectureClass.duration.toFloat() / (maxMinutes - minMinutes)) - 4.dp).coerceAtLeast(
                                1.dp
                            )
                        val cellOffsetY =
                            top + usable * ((item.lectureClass.begin - minMinutes).toFloat() / (maxMinutes - minMinutes))

                        val animatedCellHeight by animateDpAsState(
                            targetValue = cellHeight,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            ),
                            label = "HeightAnimation"
                        )

                        val animatedCellOffsetY by animateDpAsState(
                            targetValue = cellOffsetY,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            ),
                            label = "OffsetAnimation"
                        )

                        val isCandidate =
                            item.lecture.id == candidateLecture?.id
                        val isConflict =
                            isCandidate && viewModel.isCandidateOverlapping.collectAsState().value
                        val animatedAlpha by animateFloatAsState(
                            targetValue = if (viewModel.isLoading.collectAsState().value) 0.5f else 1f,
                            label = "LectureAlpha"
                        )

                        TimetableGridCell(
                            lectureItem = item,
                            isCandidate = isCandidate,
                            isConflict = isConflict,
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
                                    onLongClick = if (isEditable) { {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDeleteDialog(item.lecture)
                                    } } else null
                                )
                                .graphicsLayer { alpha = animatedAlpha }
                        )
                    }
                }
            }
        }
    }
    ActivityDetailsDialog(
        selectedActivity,
        activityActions,
        viewModel,
        onEditActivity
    ) { selectedActivity = null }

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
                color = LocalTimetableTheme.current.gridLabelColor
                    ?: MaterialTheme.colorScheme.onSurface,
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
                color = LocalTimetableTheme.current.gridLabelColor
                    ?: MaterialTheme.colorScheme.onSurface,
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
    val lineColor = LocalTimetableTheme.current.separatorColor ?: MaterialTheme.colorScheme.grayBB
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
    const val DEFAULT_MIN_MINUTES = 540   // 8:00 AM
    const val DEFAULT_MAX_MINUTES = 1080  // 6:00 PM
}


@Preview
@Composable
private fun Preview() {
    Theme {
        TimetableGrid(
            viewModel = PreviewTimetableViewModel(),
            onLectureSelected = {},
            showDeleteDialog = {})
    }
}