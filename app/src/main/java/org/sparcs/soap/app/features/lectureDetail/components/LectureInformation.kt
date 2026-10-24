package org.sparcs.soap.app.features.lectureDetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.subtitleTopic
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme


@Composable
fun LectureInformation(lecture: Lecture, navController: NavController) {
    val unknown = stringResource(R.string.unknown)
    Column(modifier = Modifier.fillMaxWidth()) {
        DetailSectionHeader(stringResource(R.string.information)) {
            TextButton(
                onClick = { navController.navigate(Channel.CourseView.name + "?courseId=${lecture.courseID}") },
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
            ) {
                Text(stringResource(R.string.view_course))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }

        DetailInfoCard(
            rows = listOfNotNull(
                stringResource(R.string.code) to lecture.code,
                stringResource(R.string.type) to stringResource(lecture.type.displayName),
                stringResource(R.string.department) to lecture.department.name,
                lecture.subtitle.subtitleTopic?.let { stringResource(R.string.subtitle) to it },
                stringResource(R.string.professor) to lecture.professors.joinToString("\n") { it.name }
                    .ifEmpty { unknown },
                stringResource(R.string.classroom) to (lecture.classes.firstOrNull()
                    ?.let { "${it.buildingName} ${it.roomName}" } ?: unknown),
                stringResource(R.string.capacity) to lecture.capacity.toString(),
                stringResource(R.string.exams) to (lecture.exams.joinToString("\n") { it.description }
                    .takeIf { it.isNotBlank() } ?: unknown),
            )
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { LectureInformation(lecture = Lecture.mock(), navController = rememberNavController()) }
}
