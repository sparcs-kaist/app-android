package com.example.soap.Features.LectureDetail.Components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.soap.Domain.Helpers.gradeLetter
import com.example.soap.Domain.Helpers.loadLetter
import com.example.soap.Domain.Helpers.speechLetter
import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import com.example.soap.Features.LectureDetail.LectureDetailViewModel
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.R
import com.google.gson.Gson

@Composable
fun LectureReviews(
    lecture: Lecture,
    viewModel: LectureDetailViewModel,
    repo: OTLCourseRepositoryProtocol,
    navController: NavController
){
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
                    val json = Uri.encode(Gson().toJson(lecture))
                    navController.navigate(Channel.ReviewCompose.name + "?lecture_json=${json}")
                          },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_rate_review),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.padding(4.dp))

                Text(
                    text = stringResource(R.string.write_a_review),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

        }
        Spacer(Modifier.padding(4.dp))

        Column {
            if (viewModel.state.collectAsState().value == LectureDetailViewModel.ViewState.Loading) {
                repeat(3) {
                   LectureReviewSkeletonCell()
                }
            } else {
                viewModel.reviews.collectAsState().value.forEach { review ->
                    LectureReviewCell(review, repo)
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