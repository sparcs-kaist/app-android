package com.example.soap.Features.LectureDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import com.example.soap.Features.LectureDetail.Components.LectureDetailNavigationBar
import com.example.soap.Features.LectureDetail.Components.LectureInformation
import com.example.soap.Features.LectureDetail.Components.LectureReviews
import com.example.soap.Features.LectureDetail.Components.LectureSummary
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.ui.theme.Theme

@Composable
fun LectureDetailView(
    lectureDetailViewModel: LectureDetailViewModel = hiltViewModel(),
    timetableViewModel: TimetableViewModel = hiltViewModel(),
    navController: NavController,
) {
    val lecture = lectureDetailViewModel.lecture.collectAsState().value
    var canWriteReview by remember { mutableStateOf(false) }

    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    val repo: OTLCourseRepositoryProtocol =
        hiltViewModel<LectureDetailViewModel>().otlCourseRepository
    val userUseCase: UserUseCaseProtocol = hiltViewModel<LectureDetailViewModel>().userUseCase

    LaunchedEffect(lecture.id) {
        lectureDetailViewModel.fetchReviews(lecture.id)
        val otl = userUseCase.otlUser
        canWriteReview = otl?.reviewWritableLectures?.any { it.id == lecture.id } ?: false
    }

    Scaffold(
        topBar = {
            LectureDetailNavigationBar(
                navController = navController,
                text = lecture.title.localized(),
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
        }
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
                    navController = navController,
                    canWriteReview = canWriteReview
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
                            timetableViewModel.removeOverlappingLectures(lecture)
                            timetableViewModel.addLecture(lecture)
                            pendingLectureToAdd = null
                        }
                    }) {
                        Text("Okay")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCannotAddLectureAlert = false
                        pendingLectureToAdd = null
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Add Overlapping Lecture") },
                text = {
                    val currentName =
                        overlappingLecture?.title?.localized() ?: "the existing lecture"
                    val newName = pendingLectureToAdd?.title?.localized() ?: "the new lecture"
                    Text(
                        text = "The lecture \"$currentName\" overlaps with your current timetable.\n" +
                                "If you add \"$newName\", the existing lecture will be removed.\n" +
                                "Would you like to add it to your timetable?"
                    )
                }
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { LectureDetailView(navController = rememberNavController()) }
}