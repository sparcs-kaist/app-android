package org.sparcs.soap.app.features.reviewDigest

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import org.sparcs.soap.app.domain.helpers.rememberSummarisesReviewsAutomatically
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewReviewDigestViewModel

@Composable
fun ReviewDigestSection(
    reviews: List<LectureReview>,
    isLoaded: Boolean,
    screen: String,
    modifier: Modifier = Modifier,
    viewModel: ReviewDigestViewModelProtocol = if (LocalInspectionMode.current) {
        remember { PreviewReviewDigestViewModel() }
    } else {
        hiltViewModel<ReviewDigestViewModel>()
    },
) {
    val state by viewModel.state.collectAsState()
    val isAutomatic by rememberSummarisesReviewsAutomatically()
    val reviewIDs = reviews.map { it.id }

    LaunchedEffect(Unit) { viewModel.checkAvailability() }
    LifecycleResumeEffect(isLoaded, reviewIDs, isAutomatic, state.isAvailable) {
        if (isLoaded) viewModel.summariseIfNeeded(reviews, isAutomatic, screen)
        onPauseOrDispose { viewModel.stop() }
    }

    if (isLoaded && viewModel.canSummarise(reviews)) {
        ReviewDigestCard(
            state = state,
            isAutomatic = isAutomatic,
            onSummarise = { viewModel.requestSummary(reviews, screen) },
            modifier = modifier,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewDigestSectionPreview() {
    Theme {
        ReviewDigestSection(
            reviews = LectureReview.mockList(),
            isLoaded = true,
            screen = "Preview",
            modifier = Modifier.padding(16.dp),
            viewModel = PreviewReviewDigestViewModel(),
        )
    }
}
