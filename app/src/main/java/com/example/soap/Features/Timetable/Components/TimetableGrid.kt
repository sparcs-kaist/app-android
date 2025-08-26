package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Enums.DayType
import com.example.soap.Domain.Helpers.TimetableConstructor
import com.example.soap.Domain.Models.TimeTable.Lecture
import com.example.soap.Domain.Models.TimeTable.Timetable
import com.example.soap.Domain.Models.TimeTable.duration
import com.example.soap.Domain.Models.TimeTable.getLectures
import com.example.soap.Domain.Models.TimeTable.maxMinutes
import com.example.soap.Domain.Models.TimeTable.minMinutes
import com.example.soap.Domain.Models.TimeTable.visibleDays
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB

@Composable
fun TimetableGrid(
    viewModel: TimetableViewModel,
    selectedLecture: ((Lecture) -> Unit)? = null
) {
    val minMinutes = viewModel.selectedTimetable?.minMinutes ?: 540 //9:00AM
    val maxMinutes = viewModel.selectedTimetable?.maxMinutes ?: 1080 //6:00PM
    val visibleDays = viewModel.selectedTimetable?.visibleDays
        ?: listOf(DayType.MON, DayType.TUE, DayType.WED, DayType.THU, DayType.FRI)
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    BoxWithConstraints {
        val heightPx = with(density) { (maxHeight * 0.2f).toPx() }
        val widthPx = with(density) { maxWidth.toPx() }
        val size = Size(widthPx, heightPx)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            modifier = Modifier.height(448.dp)
        ) {
            Column(Modifier.verticalScroll(scrollState)){

                Row {
                    TimesRowHeader(minMinutes, maxMinutes)

                    Column(modifier = Modifier.weight(1f)) {

                        DaysColumnHeader(visibleDays)

                        Row {
                            visibleDays.forEach { day ->
                                Box(modifier = Modifier.weight(1f)) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    GridHorizontalLines(
                                        minMinutes = minMinutes,
                                        maxMinutes = maxMinutes,
                                        timetableViewModel = viewModel
                                    )

                                    viewModel.selectedTimetable?.getLectures(day)?.forEach { item ->
                                        val cellHeight = TimetableConstructor.getCellHeight(
                                            item,
                                            size,
                                            viewModel.selectedTimetable!!.duration
                                        )
                                        val offsetPx = TimetableConstructor.getCellOffset(
                                            item,
                                            size,
                                            viewModel.selectedTimetable!!.minMinutes,
                                            viewModel.selectedTimetable!!.duration
                                        )

                                        val heightDp = cellHeight.dp
                                        val offsetDp = offsetPx.dp

                                        TimetableGridCell(
                                            lecture = item.lecture,
                                            modifier = Modifier
                                                .offset(y = offsetDp)
                                                .height(heightDp)
                                                .fillMaxWidth()
                                                .clickable { selectedLecture?.invoke(item.lecture) }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(TimetableConstructor.hoursWidth / 2))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GridHorizontalLines(
    timetableViewModel: TimetableViewModel,
    minMinutes: Int,
    maxMinutes: Int
) {
    val minHour = (timetableViewModel.selectedTimetable?.minMinutes ?: minMinutes) / 60
    val maxHour = (timetableViewModel.selectedTimetable?.maxMinutes ?: maxMinutes) / 60

    val totalLines = maxHour - minHour + 1

    val lineColor = MaterialTheme.colorScheme.grayBB
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))


    val lineHeight = TimetableConstructor.daysHeight
    val lineMargin = 2.dp

    Column{

    repeat(totalLines) { index ->
            //실선
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(lineHeight)) {
                drawLine(
                    color = lineColor,
                    start = Offset(lineMargin.toPx(), 0f),
                    end = Offset(size.width - lineMargin.toPx(), 0f),
                    strokeWidth = 1f
                )
            }

            //점선
            if (index < totalLines - 1) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineHeight)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(lineMargin.toPx(), 0f),
                        end = Offset(size.width - lineMargin.toPx(), 0f),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                }
            }
        }
    }
}


@Composable
fun DaysColumnHeader(days: List<DayType>) {
    Row(modifier = Modifier
        .fillMaxWidth()) {
        days.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(TimetableConstructor.daysHeight)
            ) {
                Text(
                    text = day.stringValue,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(Modifier.width(TimetableConstructor.hoursWidth/2))

    }
}
@Composable
fun TimesRowHeader(minMinutes: Int, maxMinutes: Int) {
    val minHour = minMinutes / 60
    val maxHour = maxMinutes / 60

    //2h
    val fixedCellHeight = (TimetableConstructor.daysHeight*2)


    Column(
        modifier = Modifier.width(TimetableConstructor.hoursWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        repeat(maxHour - minHour +1) { index ->
            Box(
                modifier = Modifier
                    .height(fixedCellHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (minHour + index).toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    val mockTimetable = Timetable.mockList()
    val mockViewModel = remember {
        TimetableViewModel().apply {
            selectedTimetable = mockTimetable[1]
        }
    }

    Theme {
        TimetableGrid(viewModel = mockViewModel, selectedLecture = {})
    }
}