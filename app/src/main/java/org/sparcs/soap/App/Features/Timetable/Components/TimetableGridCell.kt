package org.sparcs.soap.App.Features.Timetable.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import org.sparcs.soap.App.Domain.Models.OTL.LectureItem
import org.sparcs.soap.App.Domain.Models.OTL.backgroundColor
import org.sparcs.soap.App.Domain.Models.OTL.textColor
import org.sparcs.soap.App.Shared.Mocks.OTL.mockList

@Composable
fun TimetableGridCell(
    lectureItem: LectureItem,
    isCandidate: Boolean,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(cellHeight)
            .background(
                if (isCandidate) MaterialTheme.colorScheme.primary else lectureItem.lecture.backgroundColor,
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
                    text = lectureItem.lecture.name + lectureItem.lecture.subtitle,
                    color = lectureItem.lecture.textColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(max = titleMaxHeight)
                )

                Text(
                    text = "(" + lectureItem.lectureClass.buildingCode + ") " + lectureItem.lectureClass.roomName,
                    color = if (isCandidate) MaterialTheme.colorScheme.onPrimary else lectureItem.lecture.textColor,
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
    TimetableGridCell(lectureItem = LectureItem.mockList()[1], false, 100.dp)
}

@Preview
@Composable
private fun TimetableGridCellPreview3() {
    TimetableGridCell(lectureItem = LectureItem.mockList()[2], false, 100.dp)
}

@Preview
@Composable
private fun TimetableGridCellPreview4() {
    TimetableGridCell(lectureItem = LectureItem.mockList()[3], false, 100.dp)
}