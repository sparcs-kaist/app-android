package com.sparcs.soap.Features.LectureDetail.Components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.sparcs.soap.Domain.Helpers.gradeLetter
import com.sparcs.soap.Domain.Helpers.loadLetter
import com.sparcs.soap.Domain.Helpers.speechLetter
import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.sparcs.soap.Features.LectureDetail.LectureDetailViewModel
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Views.ContentViews.ErrorView
import com.sparcs.soap.Shared.Views.ContentViews.UnavailableView
import com.sparcs.soap.ui.theme.grayBB
import com.sparcs.soap.ui.theme.lightGray0

@Composable
fun LectureReviews(
    lecture: Lecture,
    viewModel: LectureDetailViewModel,
    repo: OTLCourseRepositoryProtocol,
    navController: NavController,
    canWriteReview: Boolean
){
    val state by viewModel.state.collectAsState()
    val textColor = if(canWriteReview) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.grayBB
    val reviews = viewModel.reviews.collectAsState().value

    Column {
        Row {
            Text(
                text = stringResource(R.string.reviews),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.padding(4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LectureSummaryRow(title = stringResource(R.string.grade), description = lecture.gradeLetter)

            Spacer(Modifier.weight(1f))

            LectureSummaryRow(title = stringResource(R.string.load), description = lecture.loadLetter)

            Spacer(Modifier.weight(1f))

            LectureSummaryRow(title = stringResource(R.string.speech), description = lecture.speechLetter)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if(canWriteReview) {
                        val json = Uri.encode(Gson().toJson(lecture))
                        navController.navigate(Channel.ReviewCompose.name + "?lecture_json=${json}")
                    }
                          },
                colors = if(canWriteReview) ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface) else
                ButtonDefaults.buttonColors(MaterialTheme.colorScheme.lightGray0)
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_rate_review),
                    contentDescription = null,
                    tint = textColor
                )

                Spacer(Modifier.padding(4.dp))

                Text(
                    text = stringResource(R.string.write_a_review),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }

        }
        Spacer(Modifier.padding(4.dp))

        Column {
            when(state){
                is LectureDetailViewModel.ViewState.Loading -> {
                    repeat(3) {
                        LectureReviewSkeletonCell()
                    }
                }
                is LectureDetailViewModel.ViewState.Loaded -> {
                    if(reviews.isEmpty()) {
                        UnavailableView(
                            icon = painterResource(R.drawable.rounded_book_2),
                            title = stringResource(R.string.no_reviews),
                            description = stringResource(R.string.there_are_no_reviews_yet)
                        )
                    } else {
                        reviews.forEach { review ->
                            LectureReviewCell(review, repo)
                        }
                    }
                }
                is LectureDetailViewModel.ViewState.Error -> {
                    val message = (state as LectureDetailViewModel.ViewState.Error).message
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = message
                    ) {
                        viewModel.fetchReviews(lecture.id)
                    }
                }
            }
        }
    }
}

//@Composable
//@Preview
//private fun Preview(){
//    val repo by remember { mutableStateOf(FakeOTLCourseRepository()) }
//    Theme { LectureReviews(
//        lecture = Lecture.mock(),
//        viewModel = MockLec,
//        repo = repo,
//        navController = NavController(LocalContext.current)
//    ) }
//}