package org.sparcs.soap.app.features.course

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.features.course.components.CourseNavigationBar
import org.sparcs.soap.app.features.course.components.CourseReviewSectionSkeleton
import org.sparcs.soap.app.features.course.components.CourseSummarySkeleton
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviewCell
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.glassBorder
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
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CourseNavigationBar(
                navController = navController,
                text = (state as? CourseViewModel.ViewState.Loaded)?.course?.name ?: ""
            )
        },
        modifier = Modifier.analyticsScreen("Course")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state is CourseViewModel.ViewState.Error) {
                val error = (state as CourseViewModel.ViewState.Error).error
                ErrorView(
                    defaultMessageResId = R.string.failed_to_load_course,
                    error = error,
                    onRetry = { viewModel.loadCourse() }
                )
            } else {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                if (isLandscape) {
                    CourseLandscapeLayout(state, viewModel)
                } else {
                    CoursePortraitLayout(state, viewModel)
                }
            }
        }
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
    viewModel: CourseViewModelProtocol
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SummarySection(state)
        }

        Column(
            modifier = Modifier
                .weight(1.2f)
                .verticalScroll(rememberScrollState())
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
    viewModel: CourseViewModelProtocol
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SummarySection(state)
        Spacer(modifier = Modifier.height(32.dp))
        ReviewSection(state, viewModel)
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SummarySection(state: CourseViewModel.ViewState) {
    when (state) {
        is CourseViewModel.ViewState.Loading -> CourseSummarySkeleton()
        is CourseViewModel.ViewState.Loaded -> CourseSummary(state.course)
        else -> {}
    }
}

@Composable
private fun ReviewSection(state: CourseViewModel.ViewState, viewModel: CourseViewModelProtocol) {
    when (state) {
        is CourseViewModel.ViewState.Loading -> CourseReviewSectionSkeleton()
        is CourseViewModel.ViewState.Loaded -> {
            CourseReviewSection(
                course = state.course,
                reviews = state.reviews,
                myReview = state.writtenReview,
                reviewPage = state.reviewPage,
                viewModel = viewModel
            )
        }
        else -> {}
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
            SummaryStat(stringResource(R.string.hours), course.classDuration.toString())
            StatSeparator()
            SummaryStat(stringResource(R.string.lab), course.expDuration.toString())
            StatSeparator()
            val creditLabel =
                if (course.credit == 0) stringResource(R.string.au) else stringResource(R.string.credit)
            val creditValue = if (course.credit == 0) course.creditAu else course.credit
            SummaryStat(creditLabel, creditValue.toString())
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.information),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = RoundedCornerShape(16.dp))
        ) {

            Column(modifier = Modifier.padding(20.dp)) {
                DetailRow(stringResource(R.string.code), course.code)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                DetailRow(stringResource(R.string.type), course.type)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                DetailRow(stringResource(R.string.department), course.department.name)

                if (course.summary.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        stringResource(R.string.summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        course.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatSeparator() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
fun CourseReviewSection(
    viewModel: CourseViewModelProtocol,
    course: Course,
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

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.reviews),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = (reviews.size + (if (myReview != null) 1 else 0)).toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = RoundedCornerShape(16.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val totalCredit = course.credit + course.creditAu
                ReviewStat(stringResource(R.string.grade), reviewPage.getGradeLetter(totalCredit))
                ReviewStat(stringResource(R.string.load), reviewPage.getLoadLetter(totalCredit))
                ReviewStat(stringResource(R.string.speech), reviewPage.getSpeechLetter(totalCredit))
            }
        }

        Column {
            myReview?.let {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.my_review_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
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
                            viewModel.summarize(it.content, scope = scope)
                        },
                        summarizationState = if (!showTranslationSheet && activeReviewId == it.id) summarizationState else SummarizationState.Idle,
                        onRetrySummarize = { viewModel.summarize(it.content, scope = scope) },
                        onDismissSummarize = {
                            activeReviewId = null
                            viewModel.hideSummary()
                        }
                    )
                    Spacer(Modifier.padding(8.dp))
                    HorizontalDivider(thickness = 0.5.dp)
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
                            viewModel.summarize(review.content, scope = scope)
                        },
                        summarizationState = if (!showTranslationSheet && activeReviewId == review.id) summarizationState else SummarizationState.Idle,
                        onRetrySummarize = { viewModel.summarize(review.content, scope = scope) },
                        onDismissSummarize = {
                            activeReviewId = null
                            viewModel.hideSummary()
                        }
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
            onRetry = { viewModel.translate(activeReviewContent, translationTarget, scope = scope) },
            onDownload = { viewModel.translate(activeReviewContent, translationTarget, allowDownload = true, scope = scope) },
            onDismiss = {
                showTranslationSheet = false
                activeReviewId = null
                activeReviewContent = ""
                viewModel.showOriginal()
            }
        )
    }
}

@Composable
private fun ReviewStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
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
