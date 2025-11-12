package com.example.soap.Features.LectureSearch

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Features.LectureSearch.Components.LectureSearchViewNavigationBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Views.ContentViews.SearchCustomBar
import com.example.soap.Shared.Views.ContentViews.UnavailableView
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.google.gson.Gson

@Composable
fun LectureSearchView(
    navController: NavController,
    timetableViewModel: TimetableViewModel = hiltViewModel(),
    lectureSearchViewModel: LectureSearchViewModel = hiltViewModel(),
    onFold: () -> Unit,
) {
    val searchKeyword = lectureSearchViewModel.searchKeyword
    val lectures by lectureSearchViewModel.lectures.collectAsState()
    val groupedByCourse = lectures.groupBy { it.course }

    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    val selectedTimetableDisplayName by timetableViewModel.selectedTimetableDisplayName.collectAsState()

    val orderedCourses = remember(lectures) {
        val seen = mutableSetOf<Int>()
        val result = mutableListOf<Int>()
        for (lecture in lectures) {
            if (seen.add(lecture.course)) {
                result.add(lecture.course)
            }
        }
        result
    }

    Scaffold(
        topBar = {
            if (searchKeyword.isEmpty()) {
                LectureSearchViewNavigationBar(
                    title = "Add to \"${selectedTimetableDisplayName}\"",
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .imePadding()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // Search bar
            SearchCustomBar(
                value = searchKeyword,
                onValueChange = {
                    lectureSearchViewModel.searchKeyword = it
                    lectureSearchViewModel.searchKeywordFlow.value = it
                },
                onValueClear = {
                    lectureSearchViewModel.searchKeyword = ""
                    lectureSearchViewModel.searchKeywordFlow.value = ""
                },
                placeHolder = stringResource(R.string.search_by_course)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lecture list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (searchKeyword.isEmpty()) {
                    item {
                        UnavailableView(
                            icon = painterResource(R.drawable.search),
                            title = "Search",
                            description = "Search courses, codes, or professors."
                        )
                    }
                } else if (orderedCourses.isEmpty()) {
                    item {
                        UnavailableView(
                            icon = painterResource(R.drawable.search),
                            title = "No Results for \"${searchKeyword}\"",
                            description = "Check the spelling or try a new search"
                        )
                    }
                } else {
                    orderedCourses.forEach { course ->
                        val courseLectures = groupedByCourse[course] ?: emptyList()
                        if (courseLectures.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                                ) {
                                    val firstItem = courseLectures.first()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = firstItem.title.localized(),
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = firstItem.code,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.grayBB
                                            )
                                            Text(
                                                text = firstItem.typeDetail.localized(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.grayBB
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                                    courseLectures.forEach { lecture ->
                                        LectureRow(
                                            lecture = lecture,
                                            onClick = {
                                                val currentCandidate =
                                                    timetableViewModel.candidateLecture.value
                                                if (currentCandidate?.id == lecture.id) {
                                                    timetableViewModel.setCandidateLecture(null)
                                                } else {
                                                    timetableViewModel.setCandidateLecture(lecture)
                                                }
                                                onFold()
                                            },
                                            onAddClick = {
                                                if (isOverlapping) {
                                                    showCannotAddLectureAlert = true
                                                    pendingLectureToAdd = lecture
                                                    timetableViewModel.setCandidateLecture(null)
                                                } else {
                                                    timetableViewModel.addLecture(lecture)
                                                }
                                            },
                                            onInfoClick = {
                                                val json = Uri.encode(Gson().toJson(lecture))
                                                navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                            }
                                        )
                                    }
                                }
                            }
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

            LaunchedEffect(Unit) {
                lectureSearchViewModel.bind()
            }

            DisposableEffect(Unit) {
                onDispose {
                    timetableViewModel.setCandidateLecture(null)
                }
            }
        }
    }
}

@Composable
fun LectureRow(
    lecture: Lecture,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = lecture.section ?: "A",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = lecture.professors.firstOrNull()?.name?.localized() ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.round_info),
            contentDescription = "info",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onInfoClick() }
        )

        Spacer(Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.round_add),
            contentDescription = "add lecture",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onAddClick() }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        LectureSearchView(
            navController = rememberNavController(),
            timetableViewModel = viewModel(),
            lectureSearchViewModel = viewModel(),
            {})
    }
}

@Preview
@Composable
private fun Preview2() {
    Theme {
        LectureRow(
            lecture = Lecture.mock(),
            onClick = {},
            onInfoClick = {},
            onAddClick = {}
        )
    }
}