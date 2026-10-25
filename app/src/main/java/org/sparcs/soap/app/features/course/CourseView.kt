package org.sparcs.soap.app.features.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage
import org.sparcs.soap.app.domain.models.otl.sectionSubtitles
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.features.course.components.CourseHistorySection
import org.sparcs.soap.app.features.course.components.CourseHistorySkeleton
import org.sparcs.soap.app.features.course.components.CourseNavigationBar
import org.sparcs.soap.app.features.course.components.CourseProfessorPicker
import org.sparcs.soap.app.features.course.components.CourseReviewSectionSkeleton
import org.sparcs.soap.app.features.course.components.CourseSummarySkeleton
import org.sparcs.soap.app.features.lectureDetail.components.DetailInfoCard
import org.sparcs.soap.app.features.lectureDetail.components.DetailSectionHeader
import org.sparcs.soap.app.features.lectureDetail.components.DetailSectionHeaderSkeleton
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviewCell
import org.sparcs.soap.app.features.lectureDetail.components.LectureSummaryRow
import org.sparcs.soap.app.features.lectureDetail.components.MyReviewLabel
import org.sparcs.soap.app.features.lectureDetail.components.ReviewStatsCard
import org.sparcs.soap.app.features.lectureDetail.components.StatSeparator
import org.sparcs.soap.app.features.lectureDetail.components.summariseTipReviewID
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestSection
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchDestination
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchResultsLayout
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.shared.views.contentViews.PostTranslationSheet
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewCourseViewModel

@Composable
fun CourseView(
    viewModel: CourseViewModelProtocol = hiltViewModel<CourseViewModel>(),
    navController: NavController,
) {
    LectureSearchDestination(navController) { search, topContent, actions, floatingPreview ->
        CourseContent(
            viewModel = viewModel,
            navController = navController,
            isSearchContext = search != null,
            floatingTopContent = floatingPreview,
            topContent = topContent,
            navigationActions = actions,
        )
    }
}

@Composable
private fun CourseContent(
    viewModel: CourseViewModelProtocol = hiltViewModel<CourseViewModel>(),
    navController: NavController,
    navigationActions: @Composable RowScope.() -> Unit = {},
    topContent: @Composable () -> Unit = {},
    floatingTopContent: Boolean = false,
    isSearchContext: Boolean = false,
) {
    val state by viewModel.state.collectAsState()
    val course by viewModel.course.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CourseNavigationBar(
                navController = navController,
                text = course?.name ?: "",
                isSearchContext = isSearchContext,
                navigationActions = navigationActions,
            )
        },
        modifier = Modifier.analyticsScreen("Course")
    ) { paddingValues ->
        LectureSearchResultsLayout(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            floatingPreview = floatingTopContent,
            header = {},
            preview = topContent,
            results = { topInset ->
                BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    val isLandscape = maxWidth >= 840.dp
                    if (isLandscape) {
                        CourseLandscapeLayout(state, viewModel, topInset)
                    } else {
                        CoursePortraitLayout(state, viewModel, topInset)
                    }
                }
            },
        )
    }


    GlobalAlertDialog(
        state = viewModel.alertState,
        isPresented = viewModel.isAlertPresented,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

@Composable
private fun CourseLandscapeLayout(
    state: CourseViewModel.ViewState,
    viewModel: CourseViewModelProtocol,
    topInset: Dp,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SummarySection(viewModel)
        }

        Column(
            modifier = Modifier
                .weight(1.2f)
                .verticalScroll(rememberScrollState())
                .padding(top = topInset)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReviewSection(state, viewModel)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CoursePortraitLayout(
    state: CourseViewModel.ViewState,
    viewModel: CourseViewModelProtocol,
    topInset: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = topInset)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SummarySection(viewModel)
        Spacer(modifier = Modifier.height(32.dp))
        ReviewSection(state, viewModel)
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SummarySection(viewModel: CourseViewModelProtocol) {
    val course by viewModel.course.collectAsState()
    val error by viewModel.courseError.collectAsState()
    val selectedProfessorID by viewModel.selectedProfessorID.collectAsState()
    when {
        course != null -> {
            CourseSummary(course!!)
            Spacer(Modifier.height(24.dp))
            CourseHistorySection(course!!.history, selectedProfessorID, viewModel::selectProfessor)
        }

        error != null -> ErrorView(
            defaultMessageResId = R.string.failed_to_load_course,
            error = error!!,
            onRetry = viewModel::loadCourse
        )

        else -> {
            CourseSummarySkeleton()
            Spacer(Modifier.height(24.dp))
            CourseHistorySkeleton()
        }
    }
}

@Composable
private fun ReviewSection(state: CourseViewModel.ViewState, viewModel: CourseViewModelProtocol) {
    val course by viewModel.course.collectAsState()
    val selectedProfessorID by viewModel.selectedProfessorID.collectAsState()
    val loaded = state as? CourseViewModel.ViewState.Loaded
    // A placeholder until the reviews first load. After that the header stays while they reload,
    // so the professor filter doesn't disappear when a professor is picked.
    var hasLoadedReviews by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(loaded) { if (loaded != null) hasLoadedReviews = true }
    if (hasLoadedReviews || loaded != null) {
        DetailSectionHeader(
            title = stringResource(R.string.reviews),
            count = loaded?.let { it.reviews.size + (if (it.writtenReview != null) 1 else 0) },
        ) {
            if (viewModel.professors.size > 1) {
                CourseProfessorPicker(viewModel.professors, selectedProfessorID, viewModel::selectProfessor)
            }
        }
    } else {
        DetailSectionHeaderSkeleton(titleWidth = 72.dp)
    }
    when (state) {
        is CourseViewModel.ViewState.Loading -> CourseReviewSectionSkeleton()
        is CourseViewModel.ViewState.Loaded -> {
            CourseReviewSection(
                course = course,
                reviews = state.reviews,
                myReview = state.writtenReview,
                reviewPage = state.reviewPage,
                viewModel = viewModel
            )
        }

        is CourseViewModel.ViewState.Error -> ErrorView(
            defaultMessageResId = R.string.course_reviews_load_failed,
            error = state.error,
            onRetry = viewModel::fetchReviews
        )
    }
}

@Composable
fun CourseSummary(course: Course) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LectureSummaryRow(stringResource(R.string.hours), course.classDuration.toString())
            StatSeparator()
            LectureSummaryRow(stringResource(R.string.lab), course.expDuration.toString())
            StatSeparator()
            val creditLabel =
                if (course.credit == 0) stringResource(R.string.au) else stringResource(R.string.credit)
            val creditValue = if (course.credit == 0) course.creditAu else course.credit
            LectureSummaryRow(creditLabel, creditValue.toString())
        }

        Spacer(modifier = Modifier.height(8.dp))

        DetailSectionHeader(stringResource(R.string.information)) {
            if (course.history.any { it.myLectureID != null }) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.course_taken),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        DetailInfoCard(
            rows = listOf(
                stringResource(R.string.code) to course.code,
                stringResource(R.string.type) to course.type,
                stringResource(R.string.department) to course.department.name,
            ),
            footer = if (course.summary.isEmpty()) null else {
                {
                    Text(
                        stringResource(R.string.summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(course.summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }
}


@Composable
fun CourseReviewSection(
    viewModel: CourseViewModelProtocol,
    course: Course?,
    reviews: List<LectureReview>,
    myReview: LectureReview?,
    reviewPage: LectureReviewPage,
) {
    val translationState by viewModel.translationState.collectAsState()
    val summarizationState by viewModel.summarizationState.collectAsState()

    var showTranslationSheet by remember { mutableStateOf(false) }
    var translationTarget by remember { mutableStateOf(viewModel.defaultTranslationLanguage()) }
    var activeReviewId by remember { mutableStateOf<Int?>(null) }
    var activeReviewContent by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val sectionSubtitles = remember(course) { course?.sectionSubtitles.orEmpty() }
    val summariseTipID = remember(reviews, myReview) { summariseTipReviewID(reviews, myReview?.id) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val totalCredit = course?.let { it.credit + it.creditAu }
        ReviewStatsCard(
            grade = totalCredit?.let { reviewPage.getGradeLetter(it) } ?: "?",
            load = totalCredit?.let { reviewPage.getLoadLetter(it) } ?: "?",
            speech = totalCredit?.let { reviewPage.getSpeechLetter(it) } ?: "?",
        )

        ReviewDigestSection(
            reviews = listOfNotNull(myReview) + reviews.filter { it.id != myReview?.id },
            isLoaded = true,
            screen = "Course",
            modifier = Modifier.padding(vertical = 6.dp),
        )

        Column {
            myReview?.let {
                Column {
                    MyReviewLabel()
                    LectureReviewCell(
                        lectureReview = it,
                        onLikeClick = { viewModel.toggleReviewLike(it) },
                        isMine = true,
                        onTranslate = {
                            viewModel.hideSummary()
                            activeReviewId = it.id
                            activeReviewContent = it.content
                            translationTarget = viewModel.defaultTranslationLanguage()
                            showTranslationSheet = true
                            viewModel.translate(it.content, translationTarget, scope = scope)
                        },
                        onSummarize = {
                            viewModel.showOriginal()
                            showTranslationSheet = false
                            activeReviewId = it.id
                            viewModel.summarizeReview(it.content, scope = scope)
                        },
                        summarizationState = if (!showTranslationSheet && activeReviewId == it.id) summarizationState else SummarizationState.Idle,
                        onRetrySummarize = { viewModel.summarizeReview(it.content, scope = scope) },
                        onDismissSummarize = {
                            activeReviewId = null
                            viewModel.hideSummary()
                        },
                        subtitle = sectionSubtitles[it.lectureID]
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            if (reviews.isEmpty() && myReview == null) {
                UnavailableView(
                    icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                    title = stringResource(R.string.no_reviews),
                    description = stringResource(R.string.there_are_no_reviews_yet)
                )
            } else {
                reviews.forEach { review ->
                    LectureReviewCell(
                        lectureReview = review,
                        onLikeClick = { viewModel.toggleReviewLike(review) },
                        isMine = false,
                        onTranslate = {
                            viewModel.hideSummary()
                            activeReviewId = review.id
                            activeReviewContent = review.content
                            translationTarget = viewModel.defaultTranslationLanguage()
                            showTranslationSheet = true
                            viewModel.translate(review.content, translationTarget, scope = scope)
                        },
                        onSummarize = {
                            viewModel.showOriginal()
                            showTranslationSheet = false
                            activeReviewId = review.id
                            viewModel.summarizeReview(review.content, scope = scope)
                        },
                        summarizationState = if (!showTranslationSheet && activeReviewId == review.id) summarizationState else SummarizationState.Idle,
                        onRetrySummarize = { viewModel.summarizeReview(review.content, scope = scope) },
                        onDismissSummarize = {
                            activeReviewId = null
                            viewModel.hideSummary()
                        },
                        subtitle = sectionSubtitles[review.lectureID],
                        showsSummariseTip = review.id == summariseTipID
                    )
                }
            }
        }
    }

    if (showTranslationSheet) {
        PostTranslationSheet(
            state = translationState,
            targetLanguage = translationTarget,
            languages = viewModel.translationLanguages(),
            suggested = viewModel.suggestedTranslationLanguages(),
            onTargetChange = { code ->
                translationTarget = code
                viewModel.translate(activeReviewContent, code, scope = scope)
            },
            onRetry = {
                viewModel.translate(
                    activeReviewContent,
                    translationTarget,
                    scope = scope
                )
            },
            onDownload = {
                viewModel.translate(
                    activeReviewContent,
                    translationTarget,
                    allowDownload = true,
                    scope = scope
                )
            },
            onDismiss = {
                showTranslationSheet = false
                activeReviewId = null
                activeReviewContent = ""
                viewModel.showOriginal()
            }
        )
    }
}

// MARK: - Previews
@Preview(showBackground = true, name = "Loading")
@Composable
private fun PreviewLoading() {
    val viewModel = PreviewCourseViewModel(CourseViewModel.ViewState.Loading)
    Theme {
        CourseView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Loaded")
@Composable
private fun PreviewLoaded() {
    val viewModel = PreviewCourseViewModel(
        CourseViewModel.ViewState.Loaded(
            course = Course.mock(),
            reviews = LectureReview.mockList(),
            writtenReview = null,
            reviewPage = LectureReviewPage.mock()
        )
    )
    Theme {
        CourseView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Loaded Landscape", widthDp = 840, heightDp = 480)
@Composable
private fun PreviewLoadedLandscape() {
    val viewModel = PreviewCourseViewModel(
        CourseViewModel.ViewState.Loaded(
            course = Course.mock(),
            reviews = LectureReview.mockList(),
            writtenReview = null,
            reviewPage = LectureReviewPage.mock()
        )
    )
    Theme {
        CourseView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun PreviewError() {
    val viewModel = PreviewCourseViewModel(
        CourseViewModel.ViewState.Error(Exception())
    )
    Theme {
        CourseView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Empty Reviews")
@Composable
private fun PreviewEmptyReviews() {
    val viewModel = PreviewCourseViewModel(
        CourseViewModel.ViewState.Loaded(
            course = Course.mock(),
            reviews = emptyList(),
            writtenReview = null,
            reviewPage = LectureReviewPage.mock()
        )
    )
    Theme {
        CourseView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Course loaded, reviews loading")
@Composable
private fun PreviewReviewsLoading() {
    val viewModel = PreviewCourseViewModel(CourseViewModel.ViewState.Loading)
    Theme { CourseView(viewModel, rememberNavController()) }
}

@Preview(showBackground = true, name = "Reviews loaded, course loading")
@Composable
private fun PreviewCourseLoading() {
    val viewModel = PreviewCourseViewModel(
        CourseViewModel.ViewState.Loaded(
            course = null,
            reviews = LectureReview.mockList(),
            writtenReview = null,
            reviewPage = LectureReviewPage.mock(),
        )
    )
    Theme { CourseView(viewModel, rememberNavController()) }
}
