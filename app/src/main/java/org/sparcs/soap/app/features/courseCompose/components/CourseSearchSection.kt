package org.sparcs.soap.app.features.courseCompose.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.views.contentViews.CategoryFilterContent
import org.sparcs.soap.app.shared.views.contentViews.SearchCustomBar
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSearchSection(
    modifier: Modifier = Modifier,
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol,
    lectureSearchViewModel: LectureSearchViewModelProtocol,
    onSearchFocusChange: (Boolean) -> Unit = {},
) {
    val state by lectureSearchViewModel.state.collectAsState()
    val searchText by lectureSearchViewModel.searchText.collectAsState()
    val courses by lectureSearchViewModel.courses.collectAsState()
    val courseFilterState by lectureSearchViewModel.courseFilterState.collectAsState()
    
    val candidateLecture by timetableViewModel.candidateLecture.collectAsState()
    val overlappingLectures by timetableViewModel.overlappingLectures.collectAsState()
    val selectedSemester by timetableViewModel.selectedSemester.collectAsState()

    var activeFilterCategory by remember { mutableStateOf<CourseFilterCategory?>(null) }
    val filterSheetState = rememberModalBottomSheetState()
    var showCannotAddLectureAlert by remember { mutableStateOf(false) }
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }
    
    val focusManager = LocalFocusManager.current

    LaunchedEffect(selectedSemester) {
        selectedSemester?.let { semester ->
            lectureSearchViewModel.bind(semester)
        }
    }

    LaunchedEffect(searchText, courseFilterState) {
        timetableViewModel.setCandidateLecture(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .onFocusChanged { onSearchFocusChange(it.isFocused) }
            ) {
                SearchCustomBar(
                    value = searchText,
                    onValueChange = { value ->
                        lectureSearchViewModel.onSearchTextChange(value)
                    },
                    onValueClear = {
                        lectureSearchViewModel.onSearchTextChange("")
                    },
                    placeHolder = stringResource(R.string.search_by_course),
                    containerColor = MaterialTheme.colorScheme.background
                )
            }

            Spacer(modifier = Modifier.height(0.dp))

            CourseFilterRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                courseFilterState = courseFilterState,
                onCategoryClick = { activeFilterCategory = it },
                onResetFilters = { lectureSearchViewModel.onFilterChange(CourseFilterState()) },
                showLeadingDivider = false
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                searchText.isEmpty() && courseFilterState.isEmpty() -> {
                    UnavailableView(
                        icon = Icons.Rounded.Search,
                        title = stringResource(R.string.search),
                        description = stringResource(R.string.search_by_course)
                    )
                }

                state is LectureSearchViewModel.ViewState.Loading -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(10) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassBorder(shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp)
                            ) {
                                SkeletonLectureRow()
                            }
                        }
                    }
                }

                courses.isEmpty() -> {
                    UnavailableView(
                        icon = Icons.Rounded.Search,
                        title = stringResource(R.string.no_results_for, searchText),
                        description = stringResource(R.string.check_the_spelling)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        courses.forEach { course ->
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .glassBorder(shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp)
                                ) {
                                    Column {
                                        CourseSectionHeader(course, backgroundColor = Color.Transparent)

                                        course.lectures.forEachIndexed { index, lecture ->
                                            val isSelected = candidateLecture?.id == lecture.id
                                            
                                            LectureRow(
                                                lecture = lecture,
                                                isSelected = isSelected,
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    if (isSelected) {
                                                        timetableViewModel.setCandidateLecture(null)
                                                    } else {
                                                        timetableViewModel.setCandidateLecture(lecture)
                                                    }
                                                },
                                                onInfoClick = {
                                                    focusManager.clearFocus()
                                                    timetableViewModel.setCandidateLecture(lecture)
                                                    val json = Uri.encode(Gson().toJson(lecture))
                                                    navController.navigate(Channel.LectureDetail.name + "?lecture_json=$json")
                                                },
                                                onAddClick = {
                                                    val table = timetableViewModel.selectedTimetable.value
                                                    if (table?.hasCollision(lecture) == true) {
                                                        timetableViewModel.setCandidateLecture(lecture)
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
        }
    }

    if (showCannotAddLectureAlert) {
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
                val currentNames = if (overlappingLectures.isEmpty()) {
                    stringResource(R.string.the_existing_lecture)
                } else {
                    overlappingLectures.joinToString(", ") { it.name }
                }
                val newName = pendingLectureToAdd?.name ?: stringResource(R.string.the_new_lecture)
                Text(text = stringResource(id = R.string.lecture_overlap, currentNames, newName))
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
}

@Composable
private fun SkeletonLectureRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 14.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 16.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun MockViewModel(state: LectureSearchViewModel.ViewState) {
    val vm = remember { PreviewLectureSearchViewModel(state) }
    Theme {
        CourseSearchSection(
            navController = rememberNavController(),
            timetableViewModel = PreviewTimetableViewModel(),
            lectureSearchViewModel = vm
        )
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    MockViewModel(LectureSearchViewModel.ViewState.Loading)
}

@Preview
@Composable
private fun LoadedPreview() {
    MockViewModel(LectureSearchViewModel.ViewState.Loaded)
}

@Preview
@Composable
private fun ErrorPreview() {
    MockViewModel(LectureSearchViewModel.ViewState.Error(Exception("Mock Error")))
}
