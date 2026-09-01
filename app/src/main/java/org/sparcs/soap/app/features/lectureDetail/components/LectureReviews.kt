package org.sparcs.soap.app.features.lectureDetail.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.gradeLetter
import org.sparcs.soap.app.domain.helpers.loadLetter
import org.sparcs.soap.app.domain.helpers.speechLetter
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModel
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.adaptiveIconSize
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.PostTranslationSheet
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import org.sparcs.soap.app.theme.ui.lightGray0
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureDetailViewModel

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
    var showOwnReviewLikeAlert by remember { mutableStateOf(false) }

    val translationState by viewModel.translationState.collectAsState()
    val summarizationState by viewModel.summarizationState.collectAsState()
    var showTranslationSheet by remember { mutableStateOf(false) }
    var translationTarget by remember { mutableStateOf(viewModel.defaultTranslationLanguage()) }
    var activeReview by remember { mutableStateOf<LectureReview?>(null) }

    val textColor =
        if (canWriteReview) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.grayBB.copy(
            alpha = 0.7f
        )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Header
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
            WriteReviewButton(lecture, writtenReview, canWriteReview, textColor, navController)
        }

        ReviewStatsSurface(lecture)

        // Reviews List
        Column {
            when (val currentState = state) {
                is LectureDetailViewModel.ViewState.Loading -> repeat(3) { LectureReviewSkeletonCell() }
                is LectureDetailViewModel.ViewState.Loaded -> {
                    val allReviews =
                        listOfNotNull(writtenReview) + reviews.filter { it.id != writtenReview?.id }

                    if (allReviews.isEmpty()) {
                        UnavailableView(
                            icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                            title = stringResource(R.string.no_reviews),
                            description = stringResource(R.string.there_are_no_reviews_yet)
                        )
                    } else {
                        allReviews.forEach { review ->
                            val isMine = review.id == writtenReview?.id
                            if (isMine) {
                                Text(
                                    text = stringResource(R.string.my_review_title),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LectureReviewCell(
                                lectureReview = review,
                                onLikeClick = {
                                    if (isMine) showOwnReviewLikeAlert =
                                        true else viewModel.toggleReviewLike(review)
                                },
                                isMine = isMine,
                                onTranslate = {
                                    viewModel.hideSummary()
                                    activeReview = review
                                    translationTarget = viewModel.defaultTranslationLanguage()
                                    showTranslationSheet = true
                                    viewModel.translateReview(review.content, translationTarget)
                                },
                                onSummarize = {
                                    viewModel.showOriginal()
                                    showTranslationSheet = false
                                    activeReview = review
                                    viewModel.summarizeReview(review.content)
                                },
                                summarizationState = if (!showTranslationSheet && activeReview?.id == review.id) summarizationState else SummarizationState.Idle,
                                onRetrySummarize = { viewModel.summarizeReview(review.content) },
                                onDismissSummarize = {
                                    activeReview = null
                                    viewModel.hideSummary()
                                }
                            )
                            if (isMine && allReviews.size > 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                                        alpha = 0.5f
                                    ), modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                is LectureDetailViewModel.ViewState.Error -> ErrorView(error = currentState.error) {
                    viewModel.fetchReviews(
                        lecture
                    )
                }
            }
        }

        if (showOwnReviewLikeAlert) {
            AlertDialog(
                onDismissRequest = { showOwnReviewLikeAlert = false },
                title = { Text(text = stringResource(R.string.warning)) },
                text = { Text(text = stringResource(R.string.review_like_warning)) },
                confirmButton = {
                    TextButton(onClick = { showOwnReviewLikeAlert = false }) {
                        Text(
                            text = stringResource(R.string.confirm)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
            )
        }

        if (showTranslationSheet) {
            PostTranslationSheet(
                state = translationState,
                targetLanguage = translationTarget,
                languages = viewModel.translationLanguages(),
                suggested = viewModel.suggestedTranslationLanguages(),
                onTargetChange = { code ->
                    translationTarget = code
                    activeReview?.let { viewModel.translateReview(it.content, code) }
                },
                onRetry = {
                    activeReview?.let {
                        viewModel.translateReview(
                            it.content,
                            translationTarget
                        )
                    }
                },
                onDownload = {
                    activeReview?.let {
                        viewModel.translateReview(
                            it.content,
                            translationTarget,
                            allowDownload = true
                        )
                    }
                },
                onDismiss = {
                    showTranslationSheet = false
                    activeReview = null
                    viewModel.showOriginal()
                }
            )
        }
    }
}

@Composable
private fun WriteReviewButton(
    lecture: Lecture,
    writtenReview: LectureReview?,
    enabled: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    navController: NavController,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .glassBorder(RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.lightGray0)
            .clickable(enabled = enabled) {
                val json = Uri.encode(Gson().toJson(lecture))
                val writtenJSON = Uri.encode(Gson().toJson(writtenReview))
                navController.navigate(Channel.ReviewCompose.name + "?lecture_json=$json&written_review_json=$writtenJSON")
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.RateReview,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.adaptiveIconSize(
                MaterialTheme.typography.bodyLarge,
                scaleFactor = 1.4f
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (writtenReview == null) stringResource(R.string.write_a_review) else stringResource(
                R.string.edit_a_review
            ), style = MaterialTheme.typography.bodyLarge, color = tint
        )
    }
}

@Composable
private fun ReviewStatsSurface(lecture: Lecture) {
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
            ReviewStat(stringResource(R.string.grade), lecture.gradeLetter)
            ReviewStat(stringResource(R.string.load), lecture.loadLetter)
            ReviewStat(stringResource(R.string.speech), lecture.speechLetter)
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

@Composable
private fun MockView(state: LectureDetailViewModel.ViewState) {
    val mockViewModel = remember { PreviewLectureDetailViewModel(initialState = state) }
    LectureReviews(
        lecture = Lecture.mock(),
        viewModel = mockViewModel,
        navController = rememberNavController(),
        canWriteReview = true
    )
}

@Composable
@Preview(showBackground = true)
private fun LoadingPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loading) }
}

@Composable
@Preview(showBackground = true)
private fun LoadedPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Loaded) }
}

@Composable
@Preview(showBackground = true)
private fun ErrorPreview() {
    Theme { MockView(LectureDetailViewModel.ViewState.Error(Exception())) }
}
