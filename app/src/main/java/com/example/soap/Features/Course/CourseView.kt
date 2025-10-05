package com.example.soap.Features.Course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.soap.Domain.Models.OTL.Course
import com.example.soap.Domain.Models.OTL.LectureReview
import com.example.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.example.soap.Features.LectureDetail.Components.LectureDetailRow
import com.example.soap.Features.LectureDetail.Components.LectureReviewCell
import com.example.soap.Features.LectureDetail.Components.LectureReviewSkeletonCell
import com.example.soap.Features.LectureDetail.Components.LectureSummaryRow
import com.example.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseView(
    viewModel: CourseViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val repo: OTLCourseRepositoryProtocol = hiltViewModel<CourseViewModel>().otlCourseRepository
    val course = viewModel.course.collectAsState().value
    val state by viewModel.state.collectAsState()
    val reviews by viewModel.reviews.collectAsState()

    LaunchedEffect(course.id) {
        viewModel.fetchReviews(courseId = course.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course.title.localized()) },
                navigationIcon = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            when (state) {
                CourseViewModel.ViewState.Loading, CourseViewModel.ViewState.Loaded -> {
                    CourseSummary(course)
                    Spacer(modifier = Modifier.height(16.dp))
                    CourseReviewSection(course, reviews, state, repo)
                }

                is CourseViewModel.ViewState.Error -> {
                    val message = (state as CourseViewModel.ViewState.Error).message
                    Text(
                        text = "Error: $message",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun CourseSummary(course: Course) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LectureSummaryRow("Hours", course.numClasses.toString())
            LectureSummaryRow("Lab", course.numLabs.toString())
            if (course.credit == 0) {
                LectureSummaryRow("AU", course.creditAu.toString())
            } else {
                LectureSummaryRow("Credit", course.credit.toString())
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
            LectureDetailRow(stringResource(R.string.type), course.type.localized())
            LectureDetailRow(
                stringResource(R.string.department),
                course.department.name.localized()
            )

            if (course.summary.isNotEmpty()) {
                Text(
                    "Summary",
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
fun CourseReviewSection(
    course: Course,
    reviews: List<LectureReview>,
    state: CourseViewModel.ViewState,
    repo: OTLCourseRepositoryProtocol,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            LectureSummaryRow("Grade", course.grade.toString())
            LectureSummaryRow("Load", course.load.toString())
            LectureSummaryRow("Speech", course.speech.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (state == CourseViewModel.ViewState.Loaded) {
                reviews.forEach { review ->
                    LectureReviewCell(review, repo)
                }
            } else {
                repeat(3) {
                    LectureReviewSkeletonCell()
                }
            }
        }
    }
}

@Preview
@Composable
fun CourseSummaryPreview() {
    val fakeVM = remember { FakeCourseViewModel() }
    CourseView(fakeVM, rememberNavController())
}