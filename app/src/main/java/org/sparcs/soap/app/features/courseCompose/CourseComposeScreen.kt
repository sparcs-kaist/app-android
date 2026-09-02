package org.sparcs.soap.app.features.courseCompose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.sparcs.soap.app.features.courseCompose.components.CourseComposeTopBar
import org.sparcs.soap.app.features.courseCompose.components.CourseSearchSection
import org.sparcs.soap.app.features.courseCompose.components.TimetablePreviewSection
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol

@Composable
fun CourseComposeScreen(
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel<LectureSearchViewModel>()
) {
    val candidateLecture by timetableViewModel.candidateLecture.collectAsState()
    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    val timetableName by timetableViewModel.timetableName.collectAsState()

    BackHandler {
        if (candidateLecture != null) {
            timetableViewModel.setCandidateLecture(null)
        } else {
            navController.popBackStack()
        }
    }

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
            // [상단 40%] 시간표 미리보기 영역
            TimetablePreviewSection(
                modifier = Modifier.weight(0.4f),
                viewModel = timetableViewModel,
                candidateLecture = candidateLecture,
                isOverlapping = isOverlapping
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // [하단 60%] 과목 검색 및 리스트 영역
            CourseSearchSection(
                modifier = Modifier.weight(0.6f),
                navController = navController,
                timetableViewModel = timetableViewModel,
                lectureSearchViewModel = lectureSearchViewModel
            )
        }
    }
}
