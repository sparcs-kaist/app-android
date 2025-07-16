package com.example.soap.Features.LectureDetail.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.TimeTable.Lecture
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors


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

        LectureDetailRow(title = stringResource(R.string.code), description = lecture.code)

        LectureDetailRow(title = stringResource(R.string.type), description = lecture.typeDetail.localized())

        LectureDetailRow(title = stringResource(R.string.department), description = lecture.department.localized())

        LectureDetailRow(
            title = stringResource(R.string.professor),
            description = if (lecture.professors.isEmpty()) {
                stringResource(R.string.unknown)
            } else {
                lecture.professors.joinToString("\n") { it.name.localized() }
            }
        )

        LectureDetailRow(
            title = stringResource(R.string.classroom),
            description = lecture.classTimes.firstOrNull()?.classroomNameShort?.localized() ?: stringResource(R.string.unknown)
        )

        LectureDetailRow(title = stringResource(R.string.capacity), description = lecture.capacity.toString())

        LectureDetailRow(
            title = stringResource(R.string.exams),
            description = if(lecture.code.isEmpty()){
                stringResource(R.string.unknown)
            } else {
                lecture.examTimes.joinToString("\n") { it.str.localized() }
            }
        )

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable {  },
            verticalAlignment = Alignment.CenterVertically){

            Text(
                text = stringResource(R.string.view_dictionary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.soapColors.primary
            )

            Spacer(Modifier.weight(1f))

            Icon(
                painter = painterResource(R.drawable.rounded_book_2),
                contentDescription = null,
                tint = MaterialTheme.soapColors.primary
            )
        }

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable {  },
            verticalAlignment = Alignment.CenterVertically){

            Text(
                text = stringResource(R.string.view_syllabus),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.soapColors.primary
            )

            Spacer(Modifier.weight(1f))

            Icon(
                painter = painterResource(R.drawable.outline_find_in_page),
                contentDescription = null,
                tint = MaterialTheme.soapColors.primary
            )
        }
    }
}

@Composable
fun LectureDetailRow(
    title: String,
    description: String
){
    Column{
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { LectureInformation(lecture = Lecture.mock()) }
}