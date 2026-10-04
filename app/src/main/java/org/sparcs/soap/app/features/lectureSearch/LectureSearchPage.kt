package org.sparcs.soap.app.features.lectureSearch

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.features.courseCompose.components.CourseComposeTopBar
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchList
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchTimetablePreview
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun LectureSearchPage(
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol,
    viewModel: LectureSearchViewModelProtocol,
    modifier: Modifier = Modifier,
    onSearchFocusChange: (Boolean) -> Unit = {},
    session: LectureSearchSession = remember { LectureSearchSession(SavedStateHandle()) },
    inspector: (@Composable () -> Unit)? = null,
) {
    if (inspector == null) LifecycleResumeEffect(Unit) {
        timetableViewModel.setCandidateLecture(null)
        onPauseOrDispose { }
    }

    val timetableName by timetableViewModel.timetableName.collectAsState()
    var pickerVisible by remember { mutableStateOf(false) }
    val close = {
        timetableViewModel.setCandidateLecture(null)
        if (inspector != null) navController.popBackStack(Channel.TimeTable.name, false)
        else navController.popBackStack()
        Unit
    }
    BackHandler(enabled = inspector == null, onBack = close)

    BoxWithConstraints(modifier.fillMaxSize()) {
        val isWide = maxWidth >= 700.dp
        Scaffold(
            modifier = if (inspector == null) Modifier.analyticsScreen("Lecture Search") else Modifier,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (inspector == null) CourseComposeTopBar(
                    title = stringResource(R.string.add_to_timetable, timetableName),
                    onClose = close,
                    actions = {
                        LecturePreviewToggle(session)
                    },
                )
            },
        ) { innerPadding ->
            BoxWithConstraints(Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
                val availableHeight = maxHeight
                val showsSearchPane = inspector == null || maxWidth >= 1100.dp
                Row(Modifier.fillMaxSize().animateContentSize()) {
                    if (isWide && session.showPreview && !pickerVisible) {
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .verticalScroll(rememberScrollState()).padding(16.dp)
                        ) {
                            LectureSearchTimetablePreview(
                                viewModel = timetableViewModel,
                                height = (availableHeight - 32.dp).coerceAtLeast(480.dp),
                                maxHeight = (availableHeight - 32.dp).coerceAtLeast(480.dp),
                                onHeightChange = {},
                                resizable = false,
                            )
                        }
                    }
                    if (showsSearchPane) {
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            val previewMaxHeight = (availableHeight * 0.6f).coerceAtLeast(0.dp)
                            val previewHeight = session.previewHeight.dp.coerceIn(minOf(120.dp, previewMaxHeight), previewMaxHeight)
                            LectureSearchList(
                                floatingTimetablePreview = previewHeight < 260.dp,
                                timetablePreview = {
                                    if (!isWide && session.showPreview && !pickerVisible) {
                                        LectureSearchTimetablePreview(
                                            viewModel = timetableViewModel,
                                            height = session.previewHeight.dp,
                                            maxHeight = previewMaxHeight,
                                            onHeightChange = { session.resizePreview(it.value) },
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        )
                                    }
                                },
                                viewModel = viewModel,
                                timetableViewModel = timetableViewModel,
                                modifier = Modifier.weight(1f),
                                listState = session.resultsScroll,
                                onSelectDepartments = { pickerVisible = it },
                                onChooseTime = { navController.navigate(Channel.LectureTimeRange.name) },
                                onSearchFocusChange = onSearchFocusChange,
                                onOpenLecture = { lecture ->
                                    timetableViewModel.setCandidateLecture(
                                        lecture.takeUnless { timetableViewModel.selectedTimetable.value?.contains(it) == true }
                                    )
                                    navController.navigate(
                                        Channel.LectureDetail.name + "?lecture_json=${Uri.encode(Gson().toJson(lecture))}"
                                    ) {
                                        if (inspector != null) popUpTo(Channel.LectureSearch.name)
                                    }
                                },
                                onOpenCourse = { id ->
                                    timetableViewModel.setCandidateLecture(null)
                                    navController.navigate(Channel.CourseView.name + "?courseId=$id") {
                                        if (inspector != null) popUpTo(Channel.LectureSearch.name)
                                    }
                                },
                            )
                        }
                    }
                    if (inspector != null) {
                        Box(Modifier.weight(1f).fillMaxHeight()) { inspector() }
                    }
                }
            }
        }
    }
}

@Composable
fun LecturePreviewToggle(session: LectureSearchSession) {
    val focusManager = LocalFocusManager.current
    IconButton(onClick = {
        focusManager.clearFocus()
        session.showPreview(!session.showPreview)
    }) {
        Icon(
            Icons.Outlined.CalendarMonth,
            stringResource(if (session.showPreview) R.string.hide_timetable_preview else R.string.show_timetable_preview),
            tint = if (session.showPreview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
private fun LectureSearchPagePreview() {
    LectureSearchPageSample(300f)
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun FloatingTimetableSearchPreview() {
    LectureSearchPageSample(180f)
}

@Composable
private fun LectureSearchPageSample(previewHeight: Float) {
    Theme {
        LectureSearchPage(
            navController = rememberNavController(),
            timetableViewModel = PreviewTimetableViewModel(),
            viewModel = PreviewLectureSearchViewModel(LectureSearchViewModel.ViewState.Loaded()),
            session = remember { LectureSearchSession(SavedStateHandle()).apply { showPreview(true); resizePreview(previewHeight) } },
        )
    }
}
