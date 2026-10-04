package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterProvider
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.lectureSearch.CourseSectionHeader
import org.sparcs.soap.app.features.lectureSearch.LectureRow
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.CategoryFilterContent
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureSearchList(
    viewModel: LectureSearchViewModelProtocol,
    timetableViewModel: TimetableViewModelProtocol,
    onOpenLecture: (Lecture) -> Unit,
    onOpenCourse: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onSearchFocusChange: (Boolean) -> Unit = {},
    onSelectDepartments: (Boolean) -> Unit = {},
    onChooseTime: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    timetablePreview: (@Composable () -> Unit)? = null,
    floatingTimetablePreview: Boolean = false,
) {
    val state by viewModel.state.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val filter by viewModel.courseFilterState.collectAsState()
    val time by viewModel.time.collectAsState()
    val wishlistState by viewModel.wishlistState.collectAsState()
    val wishlistedLectureIDs by viewModel.wishlistedLectureIDs.collectAsState()
    val pagination by viewModel.pagination.collectAsState()
    val semester by timetableViewModel.selectedSemester.collectAsState()
    val timetable by timetableViewModel.selectedTimetable.collectAsState()
    var expandedLectureID by rememberSaveable { mutableStateOf<Int?>(null) }

    var showTimeFilter by remember { mutableStateOf(false) }
    var showTimeRangePicker by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf<CourseFilterCategory?>(null) }
    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }
    val focusManager = LocalFocusManager.current

    val showsWishlist = searchText.isBlank() && filter.isEmpty() && time.isEmpty
    val displayedState = if (showsWishlist) wishlistState else state
    val displayedCourses = displayedState.courses

    LaunchedEffect(semester) { semester?.let(viewModel::bind) }
    LaunchedEffect(category) { onSelectDepartments(category == CourseFilterCategory.Department) }

    LectureSearchResultsLayout(
        modifier = modifier
            .fillMaxSize()
            .background(LectureSearchChrome.background)
            .imePadding(),
        floatingPreview = floatingTimetablePreview,
        preview = timetablePreview,
        header = {
            LectureSearchField(
                searchText = searchText,
                filter = filter,
                time = time,
                onSearchTextChange = viewModel::onSearchTextChange,
                onCategoryClick = { category = it },
                onTimeClick = { showTimeFilter = true },
                onReset = {
                    viewModel.onFilterChange(CourseFilterState())
                    viewModel.onTimeChange(LectureTimeFilter())
                },
                onFocusChange = onSearchFocusChange,
            )
        },
        results = {
            Column(Modifier.fillMaxSize()) {
                if (showsWishlist) {
                    WishlistHeader()
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LectureSearchContent(
                        listState = listState,
                        showsWishlist = showsWishlist,
                        state = displayedState,
                        displayedCourses = displayedCourses,
                        expandedLectureID = expandedLectureID,
                        timetable = timetable,
                        wishlistedLectureIDs = wishlistedLectureIDs,
                        pagination = pagination,
                        totalCourses = state.courses.sumOf { it.lectures.size },
                        onRetryLectures = { semester?.let(viewModel::fetchLectures) },
                        onRetryWishlist = { semester?.let(viewModel::fetchWishlist) },
                        onOpenCourse = onOpenCourse,
                        onToggleWishlist = viewModel::toggleWishlist,
                        onLectureClick = { lecture ->
                            focusManager.clearFocus()
                            expandedLectureID = lecture.id.takeUnless { expandedLectureID == it }
                            timetableViewModel.setCandidateLecture(
                                lecture.takeIf {
                                    expandedLectureID == it.id && timetable?.contains(
                                        it
                                    ) != true
                                }
                            )
                        },
                        onInfoClick = { lecture ->
                            focusManager.clearFocus()
                            onOpenLecture(lecture)
                        },
                        onAddClick = { lecture ->
                            if (timetable?.hasCollision(lecture) == true) {
                                pendingLectureToAdd = lecture
                            } else {
                                timetableViewModel.addLecture(lecture)
                            }
                        },
                        onLoadNextPage = viewModel::loadNextPage
                    )
                }
            }
        },
    )

    if (showTimeFilter) {
        TimeFilterBottomSheet(
            time = time,
            onTimeChange = viewModel::onTimeChange,
            onDismiss = { showTimeFilter = false },
            onChooseOnTimetable = {
                showTimeFilter = false
                if (onChooseTime != null) onChooseTime() else showTimeRangePicker = true
            },
        )
    }

    if (showTimeRangePicker) {
        Dialog(
            onDismissRequest = { showTimeRangePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            LectureTimeRangeContent(timetable, time, viewModel::onTimeChange) {
                showTimeRangePicker = false
            }
        }
    }

    pendingLectureToAdd?.let { lecture ->
        LectureCollisionDialog(
            lecture = lecture,
            conflicts = timetable?.conflicts(lecture).orEmpty(),
            onReplace = {
                timetableViewModel.addLecture(lecture)
                pendingLectureToAdd = null
            },
            onDismiss = { pendingLectureToAdd = null }
        )
    }

    category?.let { selected ->
        CategoryFilterBottomSheet(
            selected = selected,
            filter = filter,
            onFilterChange = viewModel::onFilterChange,
            onDismiss = { category = null }
        )
    }
}

@Composable
private fun WishlistHeader() {
    Text(
        text = stringResource(R.string.wishlist),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun LectureSearchContent(
    listState: LazyListState,
    showsWishlist: Boolean,
    state: LectureSearchViewModel.ViewState,
    displayedCourses: List<CourseLecture>,
    expandedLectureID: Int?,
    timetable: Timetable?,
    wishlistedLectureIDs: Set<Int>,
    pagination: LectureSearchViewModel.PaginationState,
    totalCourses: Int,
    onRetryLectures: () -> Unit,
    onRetryWishlist: () -> Unit,
    onOpenCourse: (Int) -> Unit,
    onToggleWishlist: (Lecture) -> Unit,
    onLectureClick: (Lecture) -> Unit,
    onInfoClick: (Lecture) -> Unit,
    onAddClick: (Lecture) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    when {
        state is LectureSearchViewModel.ViewState.Loading -> LectureSearchSkeleton()

        state is LectureSearchViewModel.ViewState.Error -> ErrorView(
            defaultMessageResId = if (showsWishlist) R.string.wishlist_load_failed else R.string.failed_to_load_course,
            error = state.error,
            onRetry = if (showsWishlist) onRetryWishlist else onRetryLectures,
        )

        displayedCourses.isEmpty() -> {
            LectureSearchEmptyState(showsWishlist = showsWishlist)
        }

        else -> {
            CourseLazyList(
                listState = listState,
                showsWishlist = showsWishlist,
                displayedCourses = displayedCourses,
                expandedLectureID = expandedLectureID,
                timetable = timetable,
                wishlistedLectureIDs = wishlistedLectureIDs,
                pagination = pagination,
                totalCourses = totalCourses,
                onOpenCourse = onOpenCourse,
                onToggleWishlist = onToggleWishlist,
                onLectureClick = onLectureClick,
                onInfoClick = onInfoClick,
                onAddClick = onAddClick,
                onLoadNextPage = onLoadNextPage
            )
        }
    }
}

@Composable
private fun CourseLazyList(
    listState: LazyListState,
    showsWishlist: Boolean,
    displayedCourses: List<CourseLecture>,
    expandedLectureID: Int?,
    timetable: Timetable?,
    wishlistedLectureIDs: Set<Int>,
    pagination: LectureSearchViewModel.PaginationState,
    totalCourses: Int,
    onOpenCourse: (Int) -> Unit,
    onToggleWishlist: (Lecture) -> Unit,
    onLectureClick: (Lecture) -> Unit,
    onInfoClick: (Lecture) -> Unit,
    onAddClick: (Lecture) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = if (showsWishlist) 0.dp else 12.dp, bottom = 12.dp)
    ) {
        displayedCourses.forEach { course ->
            val showSection = course.lectures.size > 1

            item(key = "course-${course.id}") {
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    color = LectureSearchChrome.card
                ) {
                    CourseSectionHeader(course, onOpenCourse = { onOpenCourse(course.id) })
                }
            }

            items(course.lectures, key = { "${course.id}-${it.id}" }) { lecture ->
                val isLast = lecture.id == course.lectures.last().id
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(
                        bottomStart = if (isLast) 26.dp else 0.dp,
                        bottomEnd = if (isLast) 26.dp else 0.dp
                    ),
                    color = LectureSearchChrome.card,
                ) {
                    Column {
                        LectureRow(
                            lecture = lecture,
                            isSelected = expandedLectureID == lecture.id,
                            showSection = showSection,
                            conflicts = timetable?.conflicts(lecture).orEmpty(),
                            isAdded = timetable?.contains(lecture) == true,
                            isWishlisted = lecture.id in wishlistedLectureIDs,
                            onToggleWishlist = { onToggleWishlist(lecture) },
                            onClick = { onLectureClick(lecture) },
                            onInfoClick = { onInfoClick(lecture) },
                            onAddClick = { onAddClick(lecture) },
                        )
                        if (!isLast) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = LectureSearchChrome.separator
                            )
                        }
                    }
                }
                if (isLast) Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (!showsWishlist) {
            item {
                LectureSearchPagination(pagination, totalCourses, onLoadNextPage)
            }
        }
    }
}

@Composable
private fun LectureSearchEmptyState(showsWishlist: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        if (showsWishlist) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = LectureSearchChrome.secondary.copy(alpha = 0.4f)
            )
        }
        Text(
            text = stringResource(if (showsWishlist) R.string.wishlist_empty else R.string.lecture_search_empty),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = LectureSearchChrome.secondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFilterBottomSheet(
    time: LectureTimeFilter,
    onTimeChange: (LectureTimeFilter) -> Unit,
    onDismiss: () -> Unit,
    onChooseOnTimetable: () -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier.analyticsScreen("Lecture Time Filter"),
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.lecture_time_filter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onTimeChange(LectureTimeFilter()) }) {
                    Text(stringResource(R.string.reset))
                }
            }
            Column(Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())) {
                LectureTimeFilterRow(time, onTimeChange)
                TextButton(onClick = onChooseOnTimetable) {
                    Text(stringResource(R.string.choose_time_on_timetable))
                }
            }
        }
    }
}

@Composable
internal fun LectureCollisionDialog(
    lecture: Lecture,
    conflicts: List<String>,
    onReplace: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lecture_collision_title)) },
        text = {
            Text(
                stringResource(
                    R.string.lecture_collision_message,
                    conflicts.joinToString(", "),
                    lecture.name
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onReplace) {
                Text(stringResource(R.string.replace_overlapping_lecture))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        containerColor = MaterialTheme.colorScheme.background,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterBottomSheet(
    selected: CourseFilterCategory,
    filter: CourseFilterState,
    onFilterChange: (CourseFilterState) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier.analyticsScreen("Lecture Filter ${selected.name}"),
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        CategoryFilterContent(
            category = selected,
            selectedFilters = filter,
            onFilterChange = onFilterChange,
            options = CourseFilterProvider.getOptions(selected)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureSearchListPreview() {
    Theme {
        LectureSearchList(
            viewModel = PreviewLectureSearchViewModel(LectureSearchViewModel.ViewState.Loaded()),
            timetableViewModel = PreviewTimetableViewModel(),
            onOpenLecture = {},
            onOpenCourse = {}
        )
    }
}
