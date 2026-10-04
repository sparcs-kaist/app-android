package org.sparcs.soap.app.features.lectureDetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme


@Composable
fun LectureInformation(lecture: Lecture, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.information),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { navController.navigate(Channel.CourseView.name + "?courseId=${lecture.courseID}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.view_course))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                    )
                }

            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = RoundedCornerShape(16.dp))
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
                LectureDetailRow(
                    title = stringResource(R.string.department),
                    description = lecture.department.name
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(
                    title = stringResource(R.string.professor),
                    description = lecture.professors.joinToString("\n") { it.name }
                        .ifEmpty { stringResource(R.string.unknown) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                LectureDetailRow(
                    title = stringResource(R.string.classroom),
                    description = lecture.classes.firstOrNull()
                        ?.let { "${it.buildingName} ${it.roomName}" }
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
private fun Preview() {
    Theme { LectureInformation(lecture = Lecture.mock(), navController = rememberNavController()) }
}
