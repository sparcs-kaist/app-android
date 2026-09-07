package org.sparcs.soap.app.features.lectureSearch

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.lectureSearch.components.CourseSearchSheetContent
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchViewNavigationBar
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun LectureSearchView(
    navController: NavController,
    timetableName: String,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel<LectureSearchViewModel>(),
    onFoldSheet: () -> Unit = {},
    onExpandSheet: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            LectureSearchViewNavigationBar(
                title = stringResource(R.string.add_to_timetable, timetableName)
            )
        },
        modifier = Modifier.analyticsScreen("Lecture Search")
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            CourseSearchSheetContent(
                navController = navController,
                timetableViewModel = timetableViewModel,
                lectureSearchViewModel = lectureSearchViewModel,
                onFoldSheet = onFoldSheet,
                onExpandSheet = onExpandSheet
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LectureRow(
    lecture: Lecture,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onInfoClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = lecture.section + lecture.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = true
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = lecture.professors.firstOrNull()?.name
                    ?: stringResource(R.string.unknown),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (isSelected) {
            Spacer(Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "info",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.combinedClickable(onClick = onInfoClick)
            )

            Spacer(Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "add lecture",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.combinedClickable(onClick = onAddClick)
            )
        }
    }
}

@Composable
fun CourseSectionHeader(course: CourseLecture) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = course.code,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = course.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}


/* ____________________________________________________________________*/

@Composable
private fun MockView(state: LectureSearchViewModel.ViewState) {
    LectureSearchView(
        navController = rememberNavController(),
        timetableName = "My Table",
        timetableViewModel = PreviewTimetableViewModel(),
        lectureSearchViewModel = PreviewLectureSearchViewModel(initialState = state)
    ) {}
}

@Composable
@Preview(showBackground = true)
private fun LoadedPreview() {
    Theme { MockView(LectureSearchViewModel.ViewState.Loaded) }
}

@Preview
@Composable
private fun LectureRowPreview() {
    Theme {
        LectureRow(
            lecture = Lecture.mock(),
            onClick = {},
            onInfoClick = {},
            onAddClick = {}
        )
    }
}
