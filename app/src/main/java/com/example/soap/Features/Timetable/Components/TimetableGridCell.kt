package com.example.soap.Features.Timetable.Components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Models.TimeTable.Lecture
import com.example.soap.Utilities.Extensions.LocalizedText
import com.example.soap.Utilities.Mocks.mockList

@Composable
fun TimetableGridCell(
    lecture: Lecture,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(lecture.backgroundColor, shape = RoundedCornerShape(4.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            LocalizedText(
                text = lecture.title,
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            LocalizedText(
                text = lecture.classTimes[0].classroomNameShort,
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
private fun TimetableGridCellPreview() {
    TimetableGridCell(lecture = Lecture.mockList()[0])
}

@Preview
@Composable
private fun TimetableGridCellPreview2() {
    TimetableGridCell(lecture = Lecture.mockList()[1])
}

@Preview
@Composable
private fun TimetableGridCellPreview3() {
    TimetableGridCell(lecture = Lecture.mockList()[2])
}

@Preview
@Composable
private fun TimetableGridCellPreview4() {
    TimetableGridCell(lecture = Lecture.mockList()[3])
}
