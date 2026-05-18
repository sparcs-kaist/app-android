package org.sparcs.soap.App.Features.LectureDetail.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R


@Composable
fun LectureInformation(lecture: Lecture){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.information),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                LectureDetailRow(
                    title = stringResource(R.string.code),
                    description = lecture.code
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(
                    title = stringResource(R.string.type),
                    description = stringResource(lecture.type.displayName)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(title = stringResource(R.string.department), description = lecture.department.name)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(
                    title = stringResource(R.string.professor),
                    description = lecture.professors.joinToString("\n") { it.name }.ifEmpty { stringResource(R.string.unknown) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(
                    title = stringResource(R.string.classroom),
                    description = lecture.classes.firstOrNull()?.let { "${it.buildingName} ${it.roomName}" }
                        ?: stringResource(R.string.unknown)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(
                    title = stringResource(R.string.capacity),
                    description = lecture.capacity.toString()
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(
                    title = stringResource(R.string.exams),
                    description = lecture.exams.joinToString("\n") { it.description }
                        .takeIf { it.isNotBlank() } ?: stringResource(R.string.unknown)
                )
            }
        }
    }
}
@Composable
@Preview
private fun Preview(){
    Theme { LectureInformation(lecture = Lecture.mock()) }
}
