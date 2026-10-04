package org.sparcs.soap.app.features.lectureDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.lectureDetail.components.LectureDetailNavigationBar
import org.sparcs.soap.app.features.lectureDetail.components.LectureInformation
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviews
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviewsSkeleton
import org.sparcs.soap.app.features.lectureDetail.components.LectureSummary
import org.sparcs.soap.app.features.lectureSearch.components.LectureCollisionDialog
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchChrome
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchDestination
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchResultsLayout
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureDetailViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun LectureDetailView(
    viewModel: LectureDetailViewModelProtocol = hiltViewModel<LectureDetailViewModel>(),
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    navController: NavController,
) {
    LectureSearchDestination(navController) { search, topContent, actions, floatingPreview ->
        val lecture by viewModel.lecture.collectAsState()
        val wishlist = search?.wishlistedLectureIDs?.collectAsState()?.value.orEmpty()
        LectureDetailContent(
            viewModel = viewModel,
            timetableViewModel = timetableViewModel,
            navController = navController,
            isSearchContext = search != null,
            isWishlisted = lecture.id in wishlist,
            onToggleWishlist = search?.let { { it.toggleWishlist(lecture) } },
            floatingTopContent = floatingPreview,
            topContent = topContent,
            navigationActions = actions,
        )
    }
}

@Composable
fun LectureDetailContent(
    viewModel: LectureDetailViewModelProtocol = hiltViewModel<LectureDetailViewModel>(),
    timetableViewModel: TimetableViewModelProtocol = hiltViewModel<TimetableViewModel>(),
    navController: NavController,
    isWishlisted: Boolean = false,
    onToggleWishlist: (() -> Unit)? = null,
    isSearchContext: Boolean = false,
    navigationActions: @Composable RowScope.() -> Unit = {},
    topContent: @Composable () -> Unit = {},
    floatingTopContent: Boolean = false,
) {
    val state by viewModel.state.collectAsState()
    val lecture by viewModel.lecture.collectAsState()
    val canWriteReview by viewModel.canWriteReview.collectAsState()

    val selectedTimetable by timetableViewModel.selectedTimetable.collectAsState()
    val isContained = selectedTimetable?.lectures?.any { it.id == lecture.id } ?: false
    val isEditable by timetableViewModel.isEditable.collectAsState()

    var pendingLectureToAdd by remember { mutableStateOf<Lecture?>(null) }

    Scaffold(
        containerColor = if (isSearchContext) LectureSearchChrome.background else MaterialTheme.colorScheme.surface,
        topBar = {
            LectureDetailNavigationBar(
                navController = navController,
                text = lecture.name,
                onAdd = {
                    val table = timetableViewModel.selectedTimetable.value
                    if (table?.hasCollision(lecture) == true) {
                        timetableViewModel.setCandidateLecture(lecture)
                        pendingLectureToAdd = lecture
                    } else {
                        timetableViewModel.addLecture(lecture)
                        navController.popBackStack()
                    }
                },
                onDelete = {
                    timetableViewModel.deleteLecture(lecture)
                    navController.popBackStack()
                },
                isCurrentTimetable = isContained,
                isWishlisted = isWishlisted,
                onToggleWishlist = onToggleWishlist,
                isSearchContext = isSearchContext,
                navigationActions = navigationActions,
                isEnabled = isEditable
            )
        },
        modifier = Modifier.analyticsScreen("Lecture Detail")
    ) { paddingValues ->
        LectureSearchResultsLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            floatingPreview = floatingTopContent,
            header = {},
            preview = topContent,
            results = {
                BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    val isLandscape = maxWidth >= 840.dp
                    if (isLandscape) {
                        LectureLandscapeLayout(
                            state,
                            lecture,
                            viewModel,
                            navController,
                            canWriteReview,
                            selectedTimetable?.conflicts(lecture).orEmpty(),
                            isContained,
                        )
                    } else {
                        LecturePortraitLayout(
                            state,
                            lecture,
                            viewModel,
                            navController,
                            canWriteReview,
                            selectedTimetable?.conflicts(lecture).orEmpty(),
                            isContained,
                        )
                    }
                }
            },
        )
    }


    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )

    pendingLectureToAdd?.let { pending ->
        LectureCollisionDialog(
            lecture = pending,
            conflicts = selectedTimetable?.conflicts(pending).orEmpty(),
            onReplace = {
                timetableViewModel.addLecture(pending)
                pendingLectureToAdd = null
                navController.popBackStack()
            },
            onDismiss = { pendingLectureToAdd = null },
        )
    }
}

@Composable
private fun LectureLandscapeLayout(
    state: LectureDetailViewModel.ViewState,
    lecture: Lecture,
    viewModel: LectureDetailViewModelProtocol,
    navController: NavController,
    canWriteReview: Boolean,
    conflicts: List<String>,
    isContained: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LectureSummaryAndInfoSection(lecture, navController, conflicts, isContained)
        }

        Column(
            modifier = Modifier
                .weight(1.2f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LectureReviewSection(state, lecture, viewModel, navController, canWriteReview)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LecturePortraitLayout(
    state: LectureDetailViewModel.ViewState,
    lecture: Lecture,
    viewModel: LectureDetailViewModelProtocol,
    navController: NavController,
    canWriteReview: Boolean,
    conflicts: List<String>,
    isContained: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LectureSummaryAndInfoSection(lecture, navController, conflicts, isContained)
        Spacer(modifier = Modifier.height(32.dp))
        LectureReviewSection(state, lecture, viewModel, navController, canWriteReview)
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun LectureSummaryAndInfoSection(lecture: Lecture, navController: NavController, conflicts: List<String>, isContained: Boolean) {
    LectureSummary(lecture)
    Spacer(modifier = Modifier.height(24.dp))
    if (isContained || conflicts.isNotEmpty()) {
        val statusColor = if (isContained) MaterialTheme.colorScheme.primary else Color(0xFFFF8800)
        Surface(
            shape = RoundedCornerShape(50),
            color = statusColor.copy(alpha = 0.12f),
            contentColor = statusColor,
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (isContained) Icons.Rounded.CheckCircle else Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    if (isContained) stringResource(R.string.lecture_in_timetable)
                    else stringResource(R.string.lecture_conflicts, conflicts.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
    LectureInformation(lecture, navController)
}

@Composable
private fun LectureReviewSection(
    state: LectureDetailViewModel.ViewState,
    lecture: Lecture,
    viewModel: LectureDetailViewModelProtocol,
    navController: NavController,
    canWriteReview: Boolean,
) {
    if (state is LectureDetailViewModel.ViewState.Loading) {
        LectureReviewsSkeleton()
    } else {
        LectureReviews(
            lecture = lecture,
            viewModel = viewModel,
            navController = navController,
            canWriteReview = canWriteReview,
        )
    }
}

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: LectureDetailViewModel.ViewState) {
    val mockViewModel = remember { PreviewLectureDetailViewModel(initialState = state) }
    val mockTimetableViewModel = remember { PreviewTimetableViewModel() }
    LectureDetailView(
        viewModel = mockViewModel,
        timetableViewModel = mockTimetableViewModel,
        navController = rememberNavController(),
    )
}

@Composable
@Preview(showBackground = true)
private fun LoadingPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loaded) }
}

@Composable
@Preview(widthDp = 840, heightDp = 480)
private fun LoadedLandscapePreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loaded) }
}

@Composable
@Preview(showBackground = true)
private fun ErrorPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Error(Exception())) }
}
