package org.sparcs.soap.App.Features.LectureSearch

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.CourseLecture
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Features.LectureSearch.Components.LectureSearchViewNavigationBar
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.Shared.Views.ContentViews.SearchCustomBar
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.BuddyPreviewSupport.OTL.PreviewLectureSearchViewModel
import org.sparcs.soap.BuddyPreviewSupport.OTL.PreviewTimetableViewModel
import org.sparcs.soap.R

@Composable
fun LectureSearchView(
    navController: NavController,
    timetableName: String,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel(),
    onFold: () -> Unit,
) {
    val state by lectureSearchViewModel.state.collectAsState()
    val searchText by lectureSearchViewModel.searchText.collectAsState()
    val courses by lectureSearchViewModel.courses.collectAsState()

    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    val selectedSemester by timetableViewModel.selectedSemester.collectAsState()

    LaunchedEffect(selectedSemester) {
        selectedSemester?.let { semester ->
            lectureSearchViewModel.bind(semester)
        }
    }

    Scaffold(
        topBar = {
            LectureSearchViewNavigationBar(
                title = stringResource(R.string.add_to_timetable, timetableName)
            )
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
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    searchText.isEmpty() -> {
                        UnavailableView(
                            icon = Icons.Rounded.Search,
                            title = stringResource(R.string.search),
                            description = stringResource(R.string.search_by_course)
                        )
                    }

                    state is LectureSearchViewModel.ViewState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    courses.isEmpty() -> {
                        UnavailableView(
                            icon = Icons.Rounded.Search,
                            title = stringResource(R.string.no_results_for, searchText),
                            description = stringResource(R.string.check_the_spelling)
                        )
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            courses.forEach { course ->
                                item { CourseSectionHeader(course) }

                                items(course.lectures.size) { index ->
                                    val lecture = course.lectures[index]
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
                                        onInfoClick = {
                                            timetableViewModel.setCandidateLecture(lecture)
                                            val json = Uri.encode(Gson().toJson(lecture))
                                            navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                            onFold()
                                        },

                                        onAddClick = {
                                            if (isOverlapping) {
                                                pendingLectureToAdd = lecture
                                                showCannotAddLectureAlert = true
                                            } else {
                                                timetableViewModel.addLecture(lecture)
                                            }
                                        }
                                    )
                                    if (index < course.lectures.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
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

        DisposableEffect(Unit) {
            onDispose {
                timetableViewModel.setCandidateLecture(null)
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

@Composable
private fun CourseSectionHeader(course: CourseLecture) {
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
        lectureSearchViewModel = PreviewLectureSearchViewModel(initialState = state),
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
