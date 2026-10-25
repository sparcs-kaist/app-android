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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchDestination
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchResultsLayout
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipView
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
    val timetableName by timetableViewModel.timetableName.collectAsState()
    val tipStore = LocalTipStore.current
    val addFromDetailTip: @Composable () -> Unit = {
        TipView(
            BuddyTip.AddFromDetail,
            enabled = isSearchContext && isEditable && !isContained,
            message = stringResource(R.string.tip_add_from_detail_message, timetableName),
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LectureDetailNavigationBar(
                navController = navController,
                text = lecture.name,
                onAdd = {
                    tipStore?.invalidate(BuddyTip.AddFromDetail)
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
            results = { topInset ->
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
                            topInset,
                            addFromDetailTip,
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
                            topInset,
                            addFromDetailTip,
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
    topInset: Dp,
    header: @Composable () -> Unit,
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
                .padding(top = topInset)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            header()
            LectureSummaryAndInfoSection(lecture, navController, conflicts, isContained)
        }

        Column(
            modifier = Modifier
                .weight(1.2f)
                .verticalScroll(rememberScrollState())
                .padding(top = topInset)
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
    topInset: Dp,
    header: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = topInset)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        header()
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
        LectureStatusPill(isContained, conflicts, Modifier.padding(bottom = 8.dp))
    }
    LectureInformation(lecture, navController)
}

/** Says the lecture is in the selected timetable, or which lectures it would clash with. */
@Composable
private fun LectureStatusPill(isContained: Boolean, conflicts: List<String>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (isContained) colors.secondaryContainer else colors.errorContainer,
        contentColor = if (isContained) colors.onSecondaryContainer else colors.onErrorContainer,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (isContained) Icons.Filled.CheckCircle else Icons.Outlined.Warning,
                contentDescription = null,
                tint = if (isContained) colors.primary else LocalContentColor.current,
                modifier = Modifier.size(18.dp)
            )
            Text(
                if (isContained) stringResource(R.string.lecture_in_timetable)
                else stringResource(R.string.lecture_conflicts, conflicts.joinToString(", ")),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
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
