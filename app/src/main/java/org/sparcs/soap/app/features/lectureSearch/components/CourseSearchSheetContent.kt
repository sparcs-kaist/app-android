package org.sparcs.soap.app.features.lectureSearch.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterProvider
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.lectureSearch.CourseSectionHeader
import org.sparcs.soap.app.features.lectureSearch.LectureRow
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.search.components.CourseFilterRow
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.views.contentViews.CategoryFilterContent
import org.sparcs.soap.app.shared.views.contentViews.SearchCustomBar
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSearchSheetContent(
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel<LectureSearchViewModel>(),
    onFoldSheet: () -> Unit = {},
    onExpandSheet: () -> Unit = {},
) {
    val state by lectureSearchViewModel.state.collectAsState()
    val searchText by lectureSearchViewModel.searchText.collectAsState()
    val courses by lectureSearchViewModel.courses.collectAsState()
    val courseFilterState by lectureSearchViewModel.courseFilterState.collectAsState()

    var activeFilterCategory by remember { mutableStateOf<CourseFilterCategory?>(null) }
    val filterSheetState = rememberModalBottomSheetState()

    val isOverlapping by timetableViewModel.isCandidateOverlapping.collectAsState()
    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    val selectedSemester by timetableViewModel.selectedSemester.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(selectedSemester) {
        selectedSemester?.let { semester ->
            lectureSearchViewModel.bind(semester)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .imePadding()
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

            CourseFilterRow(
                courseFilterState = courseFilterState,
                onCategoryClick = { 
                    activeFilterCategory = it 
                },
                onResetFilters = { lectureSearchViewModel.onFilterChange(CourseFilterState()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                showLeadingDivider = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lecture list
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    searchText.isEmpty() && courseFilterState.isEmpty() -> {
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
                                            focusManager.clearFocus() // 키보드 내리기
                                            val currentCandidate =
                                                timetableViewModel.candidateLecture.value
                                            if (currentCandidate?.id == lecture.id) {
                                                timetableViewModel.setCandidateLecture(null)
                                            } else {
                                                timetableViewModel.setCandidateLecture(lecture)
                                            }
                                            onFoldSheet()
                                        },
                                        onInfoClick = {
                                            focusManager.clearFocus() // 키보드 내리기
                                            timetableViewModel.setCandidateLecture(lecture)
                                            val json = Uri.encode(Gson().toJson(lecture))
                                            navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                            onFoldSheet()
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
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    activeFilterCategory?.let { category ->
        ModalBottomSheet(
            onDismissRequest = { activeFilterCategory = null },
            sheetState = filterSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CategoryFilterContent(
                category = category,
                selectedFilters = courseFilterState,
                onFilterChange = lectureSearchViewModel::onFilterChange,
                options = CourseFilterProvider.getOptions(category)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            timetableViewModel.setCandidateLecture(null)
        }
    }
}
