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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.backgroundColor
import com.example.soap.Domain.Models.OTL.textColor
import com.example.soap.Shared.Extensions.LocalizedText
import com.example.soap.Shared.Mocks.mockList

@Composable
fun TimetableGridCell(
    lecture: Lecture,
    isCandidate: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp)
            .background(if(isCandidate) MaterialTheme.colorScheme.primary else lecture.backgroundColor, shape = RoundedCornerShape(4.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            LocalizedText(
                text = lecture.title,
                color = lecture.textColor,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            LocalizedText(
                text = lecture.classTimes[0].classroomNameShort,
                color = if(isCandidate) MaterialTheme.colorScheme.onPrimary else lecture.textColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
private fun TimetableGridCellPreview() {
    TimetableGridCell(lecture = Lecture.mockList()[0], true)
}

@Preview
@Composable
private fun TimetableGridCellPreview2() {
    TimetableGridCell(lecture = Lecture.mockList()[1], false)
}

@Preview
@Composable
private fun TimetableGridCellPreview3() {
    TimetableGridCell(lecture = Lecture.mockList()[2], false)
}

@Preview
@Composable
private fun TimetableGridCellPreview4() {
    TimetableGridCell(lecture = Lecture.mockList()[3], false)
}