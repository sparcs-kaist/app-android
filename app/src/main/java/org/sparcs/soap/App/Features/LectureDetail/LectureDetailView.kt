package org.sparcs.soap.App.Features.LectureDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLReviewRepositoryProtocol
import org.sparcs.soap.App.Features.LectureDetail.Components.LectureDetailNavigationBar
import org.sparcs.soap.App.Features.LectureDetail.Components.LectureInformation
import org.sparcs.soap.App.Features.LectureDetail.Components.LectureReviews
import org.sparcs.soap.App.Features.LectureDetail.Components.LectureSummary
import org.sparcs.soap.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.ViewModelMocks.OTL.MockLectureDetailViewModel
import org.sparcs.soap.App.Shared.ViewModelMocks.OTL.MockTimetableViewModel
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun LectureDetailView(
    lectureDetailViewModel: LectureDetailViewModelProtocol = hiltViewModel(),
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val lecture = lectureDetailViewModel.lecture.collectAsState().value

    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    LocalInspectionMode.current
    val repo: OTLReviewRepositoryProtocol = hiltViewModel<LectureDetailViewModel>().otlReviewRepository

    LaunchedEffect(lecture.id) {
        lectureDetailViewModel.fetchReviews()
    }

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
                onDelete = {
                    timetableViewModel.deleteLecture(lecture)
                },
                isCurrentTimetable = lectureDetailViewModel.isInCurrentTimetable,
                isEnabled = timetableViewModel.isEditable.collectAsState().value
            )
        },
        modifier = Modifier.analyticsScreen("Lecture Detail")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Lecture Summary
            item { LectureSummary(lecture) }

            // Lecture Information
            item { LectureInformation(lecture) }

            // Lecture Reviews
            item {
                LectureReviews(
                    lecture = lecture,
                    viewModel = lectureDetailViewModel,
                    repo = repo,
                    navController = navController
                )
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
}

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: LectureDetailViewModel.ViewState) {
    LectureDetailView(
        lectureDetailViewModel = MockLectureDetailViewModel(initialState = state),
        navController = rememberNavController(),
        timetableViewModel = MockTimetableViewModel()
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
    Theme { MockView(LectureDetailViewModel.ViewState.Error("Error Message")) }
}