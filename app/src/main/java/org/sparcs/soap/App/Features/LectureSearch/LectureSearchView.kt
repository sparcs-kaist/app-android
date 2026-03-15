package org.sparcs.soap.App.Features.LectureSearch

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Features.LectureSearch.Components.LectureSearchViewNavigationBar
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.ViewModelMocks.OTL.MockLectureSearchViewModel
import org.sparcs.soap.App.Shared.ViewModelMocks.OTL.MockTimetableViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.SearchCustomBar
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R

@Composable
fun LectureSearchView(
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel(),
    onFold: () -> Unit,
) {
    val searchText by lectureSearchViewModel.searchText.collectAsState()
    val courses by lectureSearchViewModel.lectures.collectAsState()

    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    val selectedTimetableDisplayName by timetableViewModel.selectedTimetableDisplayName.collectAsState()

    Scaffold(
        topBar = {
            if (searchText.isEmpty()) {
                LectureSearchViewNavigationBar(
                    title = stringResource(
                        id = R.string.add_to_timetable,
                        selectedTimetableDisplayName
                    ),
                )
            }
        },
        modifier = Modifier.analyticsScreen("Lecture Search")
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
                value = searchText,
                onValueChange = { value ->
                    lectureSearchViewModel.onSearchTextChange(value)
                },
                onValueClear = {
                    lectureSearchViewModel.onSearchTextChange("")
                },
                placeHolder = stringResource(R.string.search_by_course)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lecture list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (searchText.isEmpty()) {
                    item {
                        UnavailableView(
                            icon = Icons.Rounded.Search,
                            title = stringResource(R.string.search),
                            description = stringResource(R.string.search_by_course)
                        )
                    }
                } else if (courses.isEmpty()) {
                    item {
                        UnavailableView(
                            icon = Icons.Rounded.Search,
                            title = stringResource(R.string.no_results_for, searchText),
                            description = stringResource(R.string.check_the_spelling)
                        )
                    }
                } else {
                    courses.forEach { course ->
                        if (course.lectures.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = course.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = course.code,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.grayBB
                                            )
                                            Text(
                                                text = course.type,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.grayBB
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                                    course.lectures.forEach { lecture ->
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
                            overlappingLecture?.name
                                ?: stringResource(R.string.the_existing_lecture)
                        val newName = pendingLectureToAdd?.name
                            ?: stringResource(R.string.the_new_lecture)
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
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = lecture.classNo + lecture.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = true
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = lecture.professors.firstOrNull()?.name
                    ?: stringResource(R.string.unknown),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "info",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onInfoClick() }
        )

        Spacer(Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Rounded.Add,
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
