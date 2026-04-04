package org.sparcs.soap.App.Features.LectureDetail.Components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Helpers.gradeLetter
import org.sparcs.soap.App.Domain.Helpers.loadLetter
import org.sparcs.soap.App.Domain.Helpers.speechLetter
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModel
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.GlobalAlertDialog
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.BuddyPreviewSupport.OTL.PreviewLectureDetailViewModel
import org.sparcs.soap.R

@Composable
fun LectureReviews(
    lecture: Lecture,
    viewModel: LectureDetailViewModelProtocol,
    canWriteReview: Boolean,
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val writtenReview by viewModel.writtenReview.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val textColor =
        if (canWriteReview) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.grayBB

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
            LectureSummaryRow(
                title = stringResource(R.string.grade),
                description = lecture.gradeLetter
            )

            Spacer(Modifier.weight(1f))

            LectureSummaryRow(
                title = stringResource(R.string.load),
                description = lecture.loadLetter
            )

            Spacer(Modifier.weight(1f))

            LectureSummaryRow(
                title = stringResource(R.string.speech),
                description = lecture.speechLetter
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (canWriteReview) {
                        val json = Uri.encode(Gson().toJson(lecture))
                        val writtenReviewJSON =
                            Uri.encode(Gson().toJson(viewModel.writtenReview.value))
                        navController.navigate(Channel.ReviewCompose.name + "?lecture_json=${json}&written_review_json=${writtenReviewJSON}")
                    }
                },
                colors = if (canWriteReview) ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface) else
                    ButtonDefaults.buttonColors(MaterialTheme.colorScheme.lightGray0)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RateReview,
                    contentDescription = null,
                    tint = textColor
                )

                Spacer(Modifier.padding(4.dp))

                Text(
                    text = if (writtenReview == null) stringResource(R.string.write_a_review) else stringResource(
                        R.string.edit_a_review
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }
        }
        Spacer(Modifier.padding(4.dp))

        Column {
            when (state) {
                is LectureDetailViewModel.ViewState.Loading -> {
                    repeat(3) {
                        LectureReviewSkeletonCell()
                    }
                }

                is LectureDetailViewModel.ViewState.Loaded -> {
                    writtenReview?.let { myReview ->
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

                    if (reviews.isEmpty() && writtenReview == null) {
                        UnavailableView(
                            icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                            title = stringResource(R.string.no_reviews),
                            description = stringResource(R.string.there_are_no_reviews_yet)
                        )
                    } else {
                        reviews.forEach { review ->
                            if (review.id != writtenReview?.id) {
                                LectureReviewCell(review, onLikeClick = {
                                    viewModel.toggleReviewLike(review)
                                }, false)
                            }
                        }
                    }
                }

                is LectureDetailViewModel.ViewState.Error -> {
                    val error = (state as LectureDetailViewModel.ViewState.Error).error
                    ErrorView(
                        icon = Icons.Default.Warning,
                        error = error
                    ) {
                        viewModel.fetchReviews(lecture)
                    }
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


/* ____________________________________________________________________*/

@Composable
private fun MockView(state: LectureDetailViewModel.ViewState) {
    val mockViewModel = remember { PreviewLectureDetailViewModel(initialState = state) }
    LectureReviews(
        lecture = Lecture.mock(),
        viewModel = mockViewModel,
        navController = rememberNavController(),
        canWriteReview = true,
    )
}

@Composable
@Preview
private fun LoadingPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loaded) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Error(Exception())) }
}