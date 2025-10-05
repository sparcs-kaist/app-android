package com.example.soap.Features.LectureDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import com.example.soap.Features.LectureDetail.Components.LectureDetailNavigationBar
import com.example.soap.Features.LectureDetail.Components.LectureInformation
import com.example.soap.Features.LectureDetail.Components.LectureReviews
import com.example.soap.Features.LectureDetail.Components.LectureSummary
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.ui.theme.Theme

@Composable
fun LectureDetailView(
    viewModel: LectureDetailViewModel = hiltViewModel(),
    navController: NavController
) {
    val lecture = viewModel.lecture.collectAsState().value

    val scope = rememberCoroutineScope()
    var canWriteReview by remember { mutableStateOf(false) }
    var Overrapping by remember { mutableStateOf(false) }

    val repo: OTLCourseRepositoryProtocol =
        hiltViewModel<LectureDetailViewModel>().otlCourseRepository
    val userUseCase: UserUseCaseProtocol = hiltViewModel<LectureDetailViewModel>().userUseCase

    LaunchedEffect(lecture.id) {
        viewModel.fetchReviews(lecture.id)
        val otl = userUseCase.otlUser
        canWriteReview = otl?.reviewWritableLectures?.any { it.id == lecture.id } ?: false
    }

    Scaffold(
        topBar = {
            LectureDetailNavigationBar(
                navController = navController,
                text = lecture.title.localized(),
                onAdd = { navController.navigate(Channel.ReviewCompose.name) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Lecture Summary
            item { LectureSummary(lecture) }

            // Lecture Information
            item { LectureInformation(lecture) }

            // Lecture Reviews
            item {
                LectureReviews(lecture = lecture, viewModel = viewModel, repo = repo, navController = navController)
            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { LectureDetailView(navController = rememberNavController()) }
}