package org.sparcs.soap.App.Features.Timetable.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.backgroundColor
import org.sparcs.soap.App.Domain.Models.OTL.textColor
import org.sparcs.soap.App.Shared.Mocks.mockList

@Composable
fun TimetableGridCell(
    lecture: Lecture,
    isCandidate: Boolean,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(cellHeight)
            .background(
                if (isCandidate) MaterialTheme.colorScheme.primary else lecture.backgroundColor,
                RoundedCornerShape(4.dp)
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            val titleMaxHeight = maxHeight * 0.6f
            val subMaxHeight = maxHeight * 0.3f

            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = lecture.title.localized(),
                    color = lecture.textColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(max = titleMaxHeight)
                )

                Text(
                    text = lecture.classTimes[0].classroomNameShort.localized(),
                    color = if (isCandidate) MaterialTheme.colorScheme.onPrimary else lecture.textColor,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.heightIn(max = subMaxHeight)
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimetableGridCellPreview2() {
    TimetableGridCell(lecture = Lecture.mockList()[1], false, 100.dp)
}

@Preview
@Composable
private fun TimetableGridCellPreview3() {
    TimetableGridCell(lecture = Lecture.mockList()[2], false, 100.dp)
}

@Preview
@Composable
private fun TimetableGridCellPreview4() {
    TimetableGridCell(lecture = Lecture.mockList()[3], false, 100.dp)
}