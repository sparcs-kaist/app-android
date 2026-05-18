package org.sparcs.soap.App.Features.LectureDetail.Components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
                text = (reviews.size + (if (writtenReview != null) 1 else 0)).toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
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
                    ButtonDefaults.buttonColors(MaterialTheme.colorScheme.lightGray0),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RateReview,
                    contentDescription = null,
                    tint = textColor
                )

                Spacer(Modifier.padding(2.dp))

                Text(
                    text = if (writtenReview == null) stringResource(R.string.write_a_review) else stringResource(
                        R.string.edit_a_review
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ReviewStat(stringResource(R.string.grade), lecture.gradeLetter)
                ReviewStat(stringResource(R.string.load), lecture.loadLetter)
                ReviewStat(stringResource(R.string.speech), lecture.speechLetter)
            }
        }

        Column {
            when (state) {
                is LectureDetailViewModel.ViewState.Loading -> {
                    repeat(3) {
                        LectureReviewSkeletonCell()
                    }
                }

                is LectureDetailViewModel.ViewState.Loaded -> {
                    writtenReview?.let { myReview ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.my_review_title),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                            LectureReviewCell(
                                lectureReview = myReview,
                                onLikeClick = { viewModel.toggleReviewLike(myReview) },
                                isMine = true
                            )
                        }
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
                        error = error
                    ) {
                        viewModel.fetchReviews(lecture)
                    }
                }
            }
        }
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
