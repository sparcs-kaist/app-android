package org.sparcs.soap.app.features.courseCompose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.features.courseCompose.components.CourseComposeTopBar
import org.sparcs.soap.app.features.courseCompose.components.CourseSearchSection
import org.sparcs.soap.app.features.courseCompose.components.TimetablePreviewSection
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun CourseComposeView(
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel<LectureSearchViewModel>(),
) {
    val timetableName by timetableViewModel.timetableName.collectAsState()

    BackHandler {
        timetableViewModel.setCandidateLecture(null)
        navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CourseComposeTopBar(
                    title = timetableName,
                    onClose = {
                        timetableViewModel.setCandidateLecture(null)
                        navController.popBackStack()
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TimetablePreviewSection(
                    modifier = Modifier.weight(0.42f),
                    viewModel = timetableViewModel,
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                CourseSearchSection(
                    modifier = Modifier.weight(0.58f),
                    navController = navController,
                    timetableViewModel = timetableViewModel,
                    lectureSearchViewModel = lectureSearchViewModel
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        CourseComposeView(
            navController = rememberNavController(),
            timetableViewModel = PreviewTimetableViewModel(),
            lectureSearchViewModel = PreviewLectureSearchViewModel(LectureSearchViewModel.ViewState.Loaded)
        )
    }
}