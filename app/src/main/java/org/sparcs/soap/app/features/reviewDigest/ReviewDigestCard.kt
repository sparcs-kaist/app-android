package org.sparcs.soap.app.features.reviewDigest

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestError
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.views.contentViews.SummarizationSkeleton
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.creditCompleteColor
import org.sparcs.soap.app.theme.ui.creditIncompleteColor

@Composable
fun ReviewDigestCard(
    state: ReviewDigestState,
    isAutomatic: Boolean,
    onSummarise: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = state.phase
    AnimatedContent(
        targetState = phase.kind(isAutomatic),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth(),
        label = "ReviewDigestCard",
    ) { kind ->
        when (kind) {
            DigestKind.BUTTON -> SummariseButton(onSummarise)
            DigestKind.READING -> DigestSurface {
                val reading = phase as? ReviewDigestPhase.Reading
                ReadingContent(reading?.completed ?: 0, reading?.total ?: 0)
            }
            DigestKind.DIGEST -> DigestSurface {
                when (phase) {
                    is ReviewDigestPhase.Writing -> DigestContent(phase.digest, isFinished = false, reviewCount = state.reviewCount)
                    is ReviewDigestPhase.Finished -> DigestContent(phase.digest, isFinished = true, reviewCount = state.reviewCount)
                    else -> Unit
                }
            }
            DigestKind.FAILED -> DigestSurface {
                (phase as? ReviewDigestPhase.Failed)?.let { FailureContent(it.reason, onSummarise) }
            }
        }
    }
}

private enum class DigestKind { BUTTON, READING, DIGEST, FAILED }

private fun ReviewDigestPhase.kind(isAutomatic: Boolean): DigestKind = when (this) {
    ReviewDigestPhase.Idle -> if (isAutomatic) DigestKind.READING else DigestKind.BUTTON
    is ReviewDigestPhase.Reading -> DigestKind.READING
    is ReviewDigestPhase.Writing, is ReviewDigestPhase.Finished -> DigestKind.DIGEST
    is ReviewDigestPhase.Failed -> DigestKind.FAILED
}

@Composable
private fun SummariseButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.review_digest_summarise), modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun DigestSurface(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .glassBorder(shape = RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) { content() }
    }
}

@Composable
private fun DigestHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReadingContent(completed: Int, total: Int) {
    DigestHeader(stringResource(R.string.review_digest_summarising))
    DigestProgressBar(completed, total)
    Text(
        stringResource(R.string.review_digest_reading),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DigestProgressBar(completed: Int, total: Int) {
    val reached = remember { mutableFloatStateOf(0f) }
    val target = if (total > 0) (completed + 0.9f) / (total + 1) else 0.15f
    reached.floatValue = maxOf(reached.floatValue, target)
    val fraction by animateFloatAsState(
        targetValue = reached.floatValue,
        animationSpec = tween(if (total > 0) 15_000 else 4_000, easing = LinearOutSlowInEasing),
        label = "ReviewDigestProgress",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) }
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun DigestContent(digest: LectureReviewDigest, isFinished: Boolean, reviewCount: Int) {
    DigestHeader(stringResource(if (isFinished) R.string.review_digest_title else R.string.review_digest_summarising))
    val overview = digest.overview
    if (overview.isNullOrBlank()) {
        SummarizationSkeleton()
    } else {
        Text(overview, style = MaterialTheme.typography.bodyMedium)
    }
    if (digest.liked.isNotEmpty()) {
        PointList(stringResource(R.string.review_digest_liked), Icons.Rounded.ThumbUp, creditCompleteColor, digest.liked)
    }
    if (digest.disliked.isNotEmpty()) {
        PointList(stringResource(R.string.review_digest_disliked), Icons.Rounded.Warning, creditIncompleteColor, digest.disliked)
    }
    digest.workload?.takeIf { it.isNotBlank() }?.let {
        Aspect(stringResource(R.string.review_digest_workload), Icons.Outlined.Schedule, it)
    }
    digest.grading?.takeIf { it.isNotBlank() }?.let {
        Aspect(stringResource(R.string.review_digest_grading), Icons.Outlined.School, it)
    }
    if (isFinished) {
        Text(
            stringResource(R.string.review_digest_note, reviewCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PointList(title: String, icon: ImageVector, tint: Color, points: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = tint, fontWeight = FontWeight.SemiBold)
        }
        points.forEach { point ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(point, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Aspect(title: String, icon: ImageVector, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FailureContent(reason: LectureReviewDigestError, onRetry: () -> Unit) {
    DigestHeader(stringResource(R.string.review_digest_title))
    Text(
        stringResource(reason.messageRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    TextButton(onClick = onRetry) {
        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.review_digest_try_again), modifier = Modifier.padding(start = 6.dp))
    }
}

private val LectureReviewDigestError.messageRes: Int
    get() = when (this) {
        LectureReviewDigestError.UNAVAILABLE -> R.string.review_digest_unavailable
        LectureReviewDigestError.NO_REVIEWS -> R.string.review_digest_no_reviews
        LectureReviewDigestError.UNSAFE_CONTENT -> R.string.review_digest_unsafe
        LectureReviewDigestError.BUSY -> R.string.review_digest_busy
        LectureReviewDigestError.FAILED -> R.string.review_digest_failed
    }

private val previewDigest = LectureReviewDigest(
    overview = "Clear lectures and fair exams, but weekly assignments take time.",
    liked = listOf("Clear explanations", "Helpful TAs"),
    disliked = listOf("Heavy weekly homework"),
    workload = "Weekly problem sets and two exams.",
    grading = "Generous if you submit everything.",
)

@Preview(showBackground = true)
@Composable
private fun ReviewDigestFinishedPreview() {
    Theme {
        ReviewDigestCard(ReviewDigestState(ReviewDigestPhase.Finished(previewDigest), true, 12), true, {}, Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewDigestWritingPreview() {
    Theme {
        ReviewDigestCard(ReviewDigestState(ReviewDigestPhase.Writing(LectureReviewDigest()), true, 12), true, {}, Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewDigestReadingPreview() {
    Theme {
        ReviewDigestCard(ReviewDigestState(ReviewDigestPhase.Reading(1, 3), true, 40), true, {}, Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewDigestButtonPreview() {
    Theme {
        ReviewDigestCard(ReviewDigestState(ReviewDigestPhase.Idle, true, 0), false, {}, Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewDigestFailedPreview() {
    Theme {
        ReviewDigestCard(
            ReviewDigestState(ReviewDigestPhase.Failed(LectureReviewDigestError.UNSAFE_CONTENT), true, 5),
            true,
            {},
            Modifier.padding(16.dp)
        )
    }
}
