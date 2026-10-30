package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.summarization.PostSummaryPhase
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryError
import org.sparcs.soap.app.shared.views.contentViews.SummarizationView
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostSummary(
    phase: PostSummaryPhase?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(phase?.let { it::class }) {
        when (phase) {
            is PostSummaryPhase.Finished -> haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            is PostSummaryPhase.Failed -> haptic.performHapticFeedback(HapticFeedbackType.Reject)
            else -> Unit
        }
    }

    SummarizationView(
        state = phase.toSummarizationState(),
        onRetry = onRetry,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

private fun PostSummaryPhase?.toSummarizationState(): SummarizationState = when (this) {
    null -> SummarizationState.Idle
    is PostSummaryPhase.Summarising -> SummarizationState.Streaming(partial)
    is PostSummaryPhase.Finished -> SummarizationState.Summarized(summary)
    is PostSummaryPhase.Failed -> when (reason) {
        ReviewSummaryError.UNAVAILABLE -> SummarizationState.Unavailable
        ReviewSummaryError.UNSAFE_CONTENT -> SummarizationState.Unsafe
        ReviewSummaryError.FAILED -> SummarizationState.Failed
    }
}

@Preview(showBackground = true)
@Composable
private fun PostSummaryPreview() {
    Theme {
        Column(Modifier.padding(16.dp)) {
            PostSummary(PostSummaryPhase.Summarising("A job offer for a lab assistant"), onRetry = {}, onDismiss = {})
            PostSummary(PostSummaryPhase.Finished("A job offer for a lab assistant.\n• 15,000 won an hour"), onRetry = {}, onDismiss = {})
            PostSummary(PostSummaryPhase.Failed(ReviewSummaryError.FAILED), onRetry = {}, onDismiss = {})
        }
    }
}
