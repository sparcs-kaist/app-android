package org.sparcs.soap.app.features.courseCompose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.features.courseCompose.components.CourseComposeTopBar
import org.sparcs.soap.app.features.courseCompose.components.CourseSearchSection
import org.sparcs.soap.app.features.courseCompose.components.TimetablePreviewSection
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModelProtocol
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableSummary
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureSearchViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseComposeView(
    navController: NavController,
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    lectureSearchViewModel: LectureSearchViewModelProtocol = hiltViewModel<LectureSearchViewModel>(),
) {
    val timetableName by timetableViewModel.timetableName.collectAsState()

    var isFitToScreen by rememberSaveable { mutableStateOf(true) }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    val isKeyboardVisible = WindowInsets.isImeVisible
    val focusManager = LocalFocusManager.current

    val searchHeightFraction by animateFloatAsState(
        targetValue = when {
            isSearching && isKeyboardVisible -> 1.0f
            isSearching -> 0.6f
            else -> 0.5f
        },
        label = "searchHeightFraction"
    )

    BackHandler {
        if (isSearching) {
            isSearching = false
            focusManager.clearFocus()
        } else {
            timetableViewModel.setCandidateLecture(null)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CourseComposeTopBar(
                title = timetableName,
                onClose = {
                    timetableViewModel.setCandidateLecture(null)
                    navController.popBackStack()
                },
                onAddCustom = {
                    // Navigate to custom block addition
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                TimetablePreviewSection(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = timetableViewModel,
                    isScrollable = !isFitToScreen
                )

                if (searchHeightFraction < 1.0f) {
                    Surface(
                        onClick = { isFitToScreen = !isFitToScreen },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 12.dp)
                            .size(42.dp)
                            .shadow(4.dp, MaterialTheme.shapes.medium),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFitToScreen) Icons.Rounded.Fullscreen else Icons.Rounded.FullscreenExit,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(searchHeightFraction / (if (searchHeightFraction >= 1f) 1f else (1f))), // Using height fraction effectively
                shape = RoundedCornerShape(
                    topStart = if (searchHeightFraction > 0.99f) 0.dp else 32.dp,
                    topEnd = if (searchHeightFraction > 0.99f) 0.dp else 32.dp
                ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 20.dp
            ) {
                Column {
                    TimetableSummary(viewModel = timetableViewModel, compact = true)
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    CourseSearchSection(
                        navController = navController,
                        timetableViewModel = timetableViewModel,
                        lectureSearchViewModel = lectureSearchViewModel,
                        onSearchFocusChange = { focused ->
                            if (focused) isSearching = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun CourseComposeViewPreview() {
    Theme {
        CourseComposeView(
            navController = rememberNavController(),
            timetableViewModel = PreviewTimetableViewModel(),
            lectureSearchViewModel = PreviewLectureSearchViewModel(LectureSearchViewModel.ViewState.Loaded)
        )
    }
}
