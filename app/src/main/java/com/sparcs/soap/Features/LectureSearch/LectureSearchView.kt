package com.sparcs.soap.Features.LectureSearch

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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Features.LectureSearch.Components.LectureSearchViewNavigationBar
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.Features.Timetable.TimetableViewModel
import com.sparcs.soap.Features.Timetable.TimetableViewModelProtocol
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.Shared.ViewModelMocks.OTL.MockLectureSearchViewModel
import com.sparcs.soap.Shared.ViewModelMocks.OTL.MockTimetableViewModel
import com.sparcs.soap.Shared.Views.ContentViews.SearchCustomBar
import com.sparcs.soap.Shared.Views.ContentViews.UnavailableView
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB

@Composable
fun LectureSearchView(
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel(),
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
                    title = stringResource(
                        id = R.string.add_to_timetable,
                        selectedTimetableDisplayName
                    ),
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
                            title = stringResource(R.string.search),
                            description = stringResource(R.string.search_by_course)
                        )
                    }
                } else if (orderedCourses.isEmpty()) {
                    item {
                        UnavailableView(
                            icon = painterResource(R.drawable.search),
                            title = stringResource(R.string.no_results_for, searchKeyword),
                            description = stringResource(R.string.check_the_spelling)
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
                                                    try {
                                                        timetableViewModel.addLecture(lecture)
                                                    }catch (e:Exception){
                                                        timetableViewModel.handleException(e, TimetableViewModel.ErrorType.AddLecture)
                                                    }
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
                            overlappingLecture?.title?.localized() ?: stringResource(R.string.the_existing_lecture)
                        val newName = pendingLectureToAdd?.title?.localized() ?: stringResource(R.string.the_new_lecture)
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
            text = lecture.professors.firstOrNull()?.name?.localized() ?: stringResource(R.string.unknown),
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

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: LectureSearchViewModel.ViewState) {
    LectureSearchView(
        navController = rememberNavController(),
        timetableViewModel = MockTimetableViewModel(),
        lectureSearchViewModel = MockLectureSearchViewModel(initialState = state),
        {}
    )
}

@Composable
@Preview
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