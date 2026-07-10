package org.sparcs.soap.app.features.lectureDetail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.lectureDetail.components.LectureDetailNavigationBar
import org.sparcs.soap.app.features.lectureDetail.components.LectureInformation
import org.sparcs.soap.app.features.lectureDetail.components.LectureInformationSkeleton
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviews
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviewsSkeleton
import org.sparcs.soap.app.features.lectureDetail.components.LectureSummary
import org.sparcs.soap.app.features.lectureDetail.components.LectureSummarySkeleton
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureDetailViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun LectureDetailView(
    viewModel: LectureDetailViewModelProtocol = hiltViewModel<LectureDetailViewModel>(),
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val lecture by viewModel.lecture.collectAsState()
    val canWriteReview by viewModel.canWriteReview.collectAsState()

    val selectedTimetable by timetableViewModel.selectedTimetable.collectAsState()
    val isContained = selectedTimetable?.lectures?.any { it.id == lecture.id } ?: false
    val isEditable by timetableViewModel.isEditable.collectAsState()

    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    Scaffold(
        topBar = {
            LectureDetailNavigationBar(
                navController = navController,
                text = lecture.name,
                onAdd = {
                    if (isOverlapping) {
                        showCannotAddLectureAlert = true
                        pendingLectureToAdd = lecture
                    } else {
                        timetableViewModel.addLecture(lecture)
                    }
                },
                onDelete = { timetableViewModel.deleteLecture(lecture) },
                isCurrentTimetable = isContained,
                isEnabled = isEditable
            )
        },
        modifier = Modifier.analyticsScreen("Lecture Detail")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state is LectureDetailViewModel.ViewState.Error) {
                val error = (state as LectureDetailViewModel.ViewState.Error).error
                ErrorView(
                    defaultMessageResId = R.string.failed_to_load_course,
                    error = error,
                    onRetry = { viewModel.fetchReviews(lecture) }
                )
            } else {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                if (isLandscape) {
                    LectureLandscapeLayout(state, lecture, viewModel, navController, canWriteReview)
                } else {
                    LecturePortraitLayout(state, lecture, viewModel, navController, canWriteReview)
                }
            }
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

@Composable
private fun LectureLandscapeLayout(
    state: LectureDetailViewModel.ViewState,
    lecture: Lecture,
    viewModel: LectureDetailViewModelProtocol,
    navController: NavController,
    canWriteReview: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LectureSummaryAndInfoSection(state, lecture)
        }

        Column(
            modifier = Modifier
                .weight(1.2f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LectureReviewSection(state, lecture, viewModel, navController, canWriteReview)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LecturePortraitLayout(
    state: LectureDetailViewModel.ViewState,
    lecture: Lecture,
    viewModel: LectureDetailViewModelProtocol,
    navController: NavController,
    canWriteReview: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LectureSummaryAndInfoSection(state, lecture)
        Spacer(modifier = Modifier.height(32.dp))
        LectureReviewSection(state, lecture, viewModel, navController, canWriteReview)
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun LectureSummaryAndInfoSection(state: LectureDetailViewModel.ViewState, lecture: Lecture) {
    if (state is LectureDetailViewModel.ViewState.Loading) {
        LectureSummarySkeleton()
        Spacer(modifier = Modifier.height(24.dp))
        LectureInformationSkeleton()
    } else {
        LectureSummary(lecture)
        Spacer(modifier = Modifier.height(24.dp))
        LectureInformation(lecture)
    }
}

@Composable
private fun LectureReviewSection(
    state: LectureDetailViewModel.ViewState,
    lecture: Lecture,
    viewModel: LectureDetailViewModelProtocol,
    navController: NavController,
    canWriteReview: Boolean
) {
    if (state is LectureDetailViewModel.ViewState.Loading) {
        LectureReviewsSkeleton()
    } else {
        LectureReviews(
            lecture = lecture,
            viewModel = viewModel,
            navController = navController,
            canWriteReview = canWriteReview,
        )
    }
}

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: LectureDetailViewModel.ViewState) {
    val mockViewModel = remember { PreviewLectureDetailViewModel(initialState = state) }
    val mockTimetableViewModel = remember { PreviewTimetableViewModel() }
    LectureDetailView(
        viewModel = mockViewModel,
        timetableViewModel = mockTimetableViewModel,
        navController = rememberNavController(),
    )
}

@Composable
@Preview(showBackground = true)
private fun LoadingPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loaded) }
}

@Composable
@Preview(widthDp = 840, heightDp = 480)
private fun LoadedLandscapePreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loaded) }
}

@Composable
@Preview(showBackground = true)
private fun ErrorPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Error(Exception())) }
}
