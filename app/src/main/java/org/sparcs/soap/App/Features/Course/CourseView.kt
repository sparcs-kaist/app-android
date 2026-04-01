package org.sparcs.soap.App.Features.Course

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Domain.Models.OTL.LectureReviewPage
import org.sparcs.soap.App.Features.Course.Components.CourseNavigationBar
import org.sparcs.soap.App.Features.LectureDetail.Components.LectureDetailRow
import org.sparcs.soap.App.Features.LectureDetail.Components.LectureReviewCell
import org.sparcs.soap.App.Features.LectureDetail.Components.LectureSummaryRow
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.Shared.Mocks.OTL.mockList
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.BuddyPreviewSupport.OTL.PreviewCourseViewModel
import org.sparcs.soap.R

@Composable
fun CourseView(
    viewModel: CourseViewModelProtocol = hiltViewModel(),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            when (state) {
                CourseViewModel.ViewState.Loading -> {
                    CourseSummarySkeleton()
                }

                is CourseViewModel.ViewState.Loaded -> {
                    val course = (state as CourseViewModel.ViewState.Loaded).course
                    val reviews = (state as CourseViewModel.ViewState.Loaded).reviews
                    val reviewPage = (state as CourseViewModel.ViewState.Loaded).reviewPage
                    val writtenReview = (state as CourseViewModel.ViewState.Loaded).writtenReview

                    CourseSummary(course)
                    Spacer(modifier = Modifier.height(16.dp))
                    CourseReviewSection(
                        course = course,
                        reviews = reviews,
                        myReview = writtenReview,
                        reviewPage = reviewPage,
                        viewModel = viewModel
                    )
                }

                is CourseViewModel.ViewState.Error -> {
                    val error = (state as CourseViewModel.ViewState.Error).error
                    ErrorView(
                        icon = Icons.Default.Warning,
                        defaultMessageResId = R.string.failed_to_load_course,
                        error = error,
                        onRetry = { viewModel.loadCourse() }
                    )
                }
            }
        }
    }
}

@Composable
fun CourseSummary(course: Course) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            LectureSummaryRow(
                stringResource(R.string.hours).uppercase(),
                course.classDuration.toString()
            )
            LectureSummaryRow(
                stringResource(R.string.lab).uppercase(),
                course.expDuration.toString()
            )
            if (course.credit == 0) {
                LectureSummaryRow(
                    stringResource(R.string.au).uppercase(),
                    course.creditAu.toString()
                )
            } else {
                LectureSummaryRow(
                    stringResource(R.string.credit).uppercase(),
                    course.credit.toString()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Text(
                text = stringResource(R.string.information),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            LectureDetailRow(stringResource(R.string.code), course.code)
            LectureDetailRow(stringResource(R.string.type), course.type)
            LectureDetailRow(
                stringResource(R.string.department),
                course.department.name
            )

            if (course.summary.isNotEmpty()) {
                Text(
                    stringResource(R.string.summary),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    course.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun CourseSummarySkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(3) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(40.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(30.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(100.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )

            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(60.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(120.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(70.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
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

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.reviews), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            val totalCredit = course.credit + course.creditAu
            LectureSummaryRow(
                stringResource(R.string.grade),
                reviewPage.getGradeLetter(totalCredit)
            )
            Spacer(modifier = Modifier.weight(1f))
            LectureSummaryRow(stringResource(R.string.load), reviewPage.getLoadLetter(totalCredit))
            Spacer(modifier = Modifier.weight(1f))
            LectureSummaryRow(
                stringResource(R.string.speech),
                reviewPage.getSpeechLetter(totalCredit)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            myReview?.let { myReview ->
                Text(
                    text = stringResource(R.string.my_review_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LectureReviewCell(
                    lectureReview = myReview,
                    onLikeClick = { viewModel.toggleReviewLike(myReview) },
                    isMine = true
                )
                Spacer(Modifier.padding(8.dp))
                HorizontalDivider(thickness = 0.5.dp)
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
                        isMine = false
                    )
                }
            }
        }
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