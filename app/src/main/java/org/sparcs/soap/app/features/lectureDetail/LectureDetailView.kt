package org.sparcs.soap.app.features.lectureDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.lectureDetail.components.LectureDetailNavigationBar
import org.sparcs.soap.app.features.lectureDetail.components.LectureInformation
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviews
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
    val isInCurrentTimetable = remember(selectedTimetable, lecture) {
        selectedTimetable?.lectures?.any { it.id == lecture.id } ?: false
    }

    val isEditable by timetableViewModel.isEditable.collectAsState()
    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    Scaffold(
        topBar = {
            LectureDetailNavigationBar(
                navController = navController,
                text = lecture.name + lecture.subtitle,
                onAdd = {
                    if (isOverlapping) {
                        showCannotAddLectureAlert = true
                        pendingLectureToAdd = lecture
                    } else {
                        timetableViewModel.addLecture(lecture)
                    }
                },
                onDelete = {
                    timetableViewModel.deleteLecture(lecture)
                },
                isCurrentTimetable = isInCurrentTimetable,
                isEnabled = isEditable
            )
        },
        modifier = Modifier.analyticsScreen("Lecture Detail")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            if (state is LectureDetailViewModel.ViewState.Error) {
                val error = (state as LectureDetailViewModel.ViewState.Error).error
                ErrorView(
                    defaultMessageResId = R.string.failed_to_load_course,
                    error = error,
                    onRetry = { viewModel.fetchReviews(lecture) }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (state) {
                        LectureDetailViewModel.ViewState.Loading -> {
                            LectureSummarySkeleton()
                        }

                        is LectureDetailViewModel.ViewState.Loaded -> {
                            // Lecture Summary
                            LectureSummary(lecture)


                            Spacer(modifier = Modifier.height(24.dp))
                            // Lecture Information
                            LectureInformation(lecture)

                            Spacer(modifier = Modifier.height(32.dp))

                            // Lecture Reviews
                            LectureReviews(
                                lecture = lecture,
                                viewModel = viewModel,
                                navController = navController,
                                canWriteReview = canWriteReview,
                            )

                            Spacer(modifier = Modifier.height(40.dp))
                        }

                        else -> {}
                    }
                }
            }
        }
        if (showCannotAddLectureAlert) {
            val overlappingLecture by timetableViewModel.overlappingLecture.collectAsState()

            AlertDialog(
                onDismissRequest = {
                    showCannotAddLectureAlert = false
                    pendingLectureToAdd = null
                },
                confirmButton = {
                    TextButton(onClick = {
                        showCannotAddLectureAlert = false

                        pendingLectureToAdd?.let { lecture ->
                            timetableViewModel.addLecture(lecture)
                            pendingLectureToAdd = null
                        }
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCannotAddLectureAlert = false
                        pendingLectureToAdd = null
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.add_overlapping_lecture)) },
                text = {
                    val currentName =
                        overlappingLecture?.name ?: stringResource(R.string.the_existing_lecture)
                    val newName = pendingLectureToAdd?.name ?: stringResource(R.string.the_new_lecture)
                    Text(
                        text = stringResource(
                            id = R.string.lecture_overlap,
                            currentName,
                            newName
                        )
                    )
                }
            )
        }
    }
    GlobalAlertDialog(
        state = viewModel.alertState,
        isPresented = viewModel.isAlertPresented,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: LectureDetailViewModel.ViewState) {
    LectureDetailView(
        viewModel = PreviewLectureDetailViewModel(initialState = state),
        navController = rememberNavController(),
        timetableViewModel = PreviewTimetableViewModel()
    )
}

@Composable
@Preview
private fun LoadingPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loaded) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Error(Exception())) }
}
