package com.sparcs.soap.Features.LectureDetail.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.ui.theme.Theme


@Composable
fun LectureInformation(lecture: Lecture){
    Column {
        Row {
            Text(
                text = stringResource(R.string.information),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.padding(4.dp))
        }

        LectureDetailRow(
            title = stringResource(R.string.code),
            description = lecture.code
        )

        LectureDetailRow(
            title = stringResource(R.string.type),
            description = lecture.typeDetail.localized()
        )

        LectureDetailRow(title = stringResource(R.string.department), description = lecture.department.name.localized())

        LectureDetailRow(
            title = stringResource(R.string.professor),
            description = lecture.professors.joinToString("\n") { it.name.localized() }.ifEmpty { stringResource(R.string.unknown) }
        )

        LectureDetailRow(
            title = stringResource(R.string.classroom),
            description = lecture.classTimes.firstOrNull()?.classroomNameShort?.localized()
                ?: stringResource(R.string.unknown)
        )

        LectureDetailRow(
            title = stringResource(R.string.capacity),
            description = lecture.capacity.toString()
        )

        LectureDetailRow(
            title = stringResource(R.string.exams),
            description = lecture.examTimes.joinToString("\n") { it.description.localized() }.ifEmpty { stringResource(R.string.unknown) }
        )

//        Row(
//            modifier = Modifier
//                .padding(vertical = 8.dp)
//                .clickable {  },
//            verticalAlignment = Alignment.CenterVertically){
//
//            Text(
//                text = stringResource(R.string.view_dictionary),
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Spacer(Modifier.weight(1f))
//
//            Icon(
//                painter = painterResource(R.drawable.rounded_book_2),
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.primary
//            )
//        }
//
//        Row(
//            modifier = Modifier
//                .padding(vertical = 8.dp)
//                .clickable {  },
//            verticalAlignment = Alignment.CenterVertically){
//
//            Text(
//                text = stringResource(R.string.view_syllabus),
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Spacer(Modifier.weight(1f))
//
//            Icon(
//                painter = painterResource(R.drawable.outline_find_in_page),
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.primary
//            )
//        }
    }
}
@Composable
@Preview
private fun Preview(){
    Theme { LectureInformation(lecture = Lecture.mock()) }
}