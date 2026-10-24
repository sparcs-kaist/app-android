package org.sparcs.soap.app.features.lectureDetail.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.ReportMailComposer
import org.sparcs.soap.app.domain.models.otl.SectionLabel
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryUseCase
import org.sparcs.soap.app.features.post.components.VoteArrow
import org.sparcs.soap.app.features.post.components.pillHeight
import org.sparcs.soap.app.features.post.components.pillTextStyle
import org.sparcs.soap.app.features.post.components.pressedStateLayer
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.shared.views.contentViews.SummarizationView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.upvote
import timber.log.Timber


@Composable
fun LectureReviewCell(
    lectureReview: LectureReview,
    onLikeClick: () -> Unit,
    isMine: Boolean,
    onTranslate: () -> Unit,
    onSummarize: () -> Unit,
    summarizationState: SummarizationState = SummarizationState.Idle,
    onRetrySummarize: () -> Unit,
    onDismissSummarize: () -> Unit,
    subtitle: String? = null,
    showsSummariseTip: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val tipStore = LocalTipStore.current
    val context = LocalContext.current

    val haptic = LocalHapticFeedback.current
    val unknown = stringResource(R.string.unknown)
    val label = SectionLabel.of(subtitle.orEmpty(), lectureReview.professors)

    Card(modifier = Modifier.padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 12.dp, bottom = if (isMine) 0.dp else 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = label.title ?: unknown,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${
                                lectureReview.year.toString().takeLast(2)
                            }${lectureReview.semester.shortCode}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    label.professors?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!isMine) {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.more)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.translate)) },
                                onClick = { onTranslate(); expanded = false },
                                leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null) }
                            )

                            if (ReviewSummaryUseCase.canSummarise(lectureReview.content)) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.summarise)) },
                                    onClick = {
                                        tipStore?.invalidate(BuddyTip.SummariseReview)
                                        onSummarize()
                                        expanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Outlined.Summarize, contentDescription = null) }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.report)) },
                                onClick = { report(lectureReview, context, unknown); expanded = false },
                                leadingIcon = { Icon(Icons.Outlined.Flag, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(end = 12.dp)) {
                Spacer(modifier = Modifier.height(8.dp))

                TipView(
                    BuddyTip.SummariseReview,
                    enabled = showsSummariseTip && summarizationState == SummarizationState.Idle,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                SummarizationView(
                    state = summarizationState,
                    onRetry = onRetrySummarize,
                    onDismiss = onDismissSummarize,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = lectureReview.content,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ReviewRatingLetter(
                        title = stringResource(R.string.grade),
                        value = lectureReview.grade
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    ReviewRatingLetter(
                        title = stringResource(R.string.load),
                        value = lectureReview.load
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    ReviewRatingLetter(
                        title = stringResource(R.string.speech),
                        value = lectureReview.speech
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    ReviewLikePill(
                        likes = lectureReview.like,
                        liked = lectureReview.likedByUser,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onLikeClick()
                        }
                    )
                }
            }
        }
    }
}

/** Compact pill with an up arrow and the like count, styled like the Ara vote pill. */
@Composable
private fun ReviewLikePill(likes: Int, liked: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val container by animateColorAsState(
        // Surface rather than the post pills' gray, which is the card's own color.
        if (liked) upvote else MaterialTheme.colorScheme.surface,
        label = "LikeContainer"
    )
    val content = if (liked) Color.White else MaterialTheme.colorScheme.onSurface
    val textStyle = pillTextStyle(isCompact = true)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .height(pillHeight(isCompact = true))
            .clip(CircleShape)
            .background(container)
            .pressedStateLayer(pressed, onColor = liked)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClickLabel = "UpVote",
                onClick = onClick
            )
            .padding(horizontal = 10.dp)
    ) {
        VoteArrow(
            up = true,
            filled = liked,
            tint = if (!liked && pressed) upvote else content,
            textStyle = textStyle
        )
        AnimatedContent(
            targetState = likes,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { -it } + fadeIn())
                        .togetherWith(slideOutVertically { it } + fadeOut())
                } else {
                    (slideInVertically { it } + fadeIn())
                        .togetherWith(slideOutVertically { -it } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
            label = "VotesTransition"
        ) { targetCount ->
            Text(text = targetCount.toString(), color = content, style = textStyle)
        }
    }
}

fun summariseTipReviewID(reviews: List<LectureReview>, myReviewID: Int?): Int? =
    reviews.firstOrNull { it.id != myReviewID && ReviewSummaryUseCase.canSummarise(it.content) }?.id

/** Placeholder mirroring [LectureReviewCell]: title row, three lines of text, ratings and likes. */
@Composable
fun LectureReviewSkeletonCell() {
    val typography = MaterialTheme.typography
    Card(modifier = Modifier.padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
            // 48dp: the height of the overflow IconButton in a loaded review.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                SkeletonLine(typography.titleMedium, Modifier.width(120.dp))
                SkeletonLine(typography.titleMedium, Modifier.width(32.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            listOf(1f, 1f, 0.7f).forEach { fraction ->
                SkeletonLine(typography.bodyMedium, Modifier.fillMaxWidth(fraction))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { SkeletonLine(typography.labelLarge, Modifier.width(56.dp)) }
                Spacer(modifier = Modifier.weight(1f))
                SkeletonBlock(
                    Modifier
                        .width(52.dp)
                        .height(pillHeight(isCompact = true)),
                    CircleShape
                )
            }
        }
    }
}

@Composable
private fun ReviewRatingLetter(title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

fun report(lectureReview: LectureReview, context: Context, unknown: String) {
    val urlString = ReportMailComposer.compose(
        title = lectureReview.courseName,
        code = "Unknown",
        year = lectureReview.year,
        semester = lectureReview.semester,
        professorName = lectureReview.professors.firstOrNull()?.name ?: unknown,
        content = lectureReview.content
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = urlString.toUri()
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Error launching email app")
        Toast.makeText(context, "Failed to open email app", Toast.LENGTH_SHORT).show()
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        LectureReviewCell(LectureReview.mock(), {}, false, {}, {}, SummarizationState.Idle, {}, {})
    }
}

@Composable
@Preview
private fun SubtitlePreview() {
    Theme {
        LectureReviewCell(
            LectureReview.mock(),
            {},
            false,
            {},
            {},
            SummarizationState.Idle,
            {},
            {},
            subtitle = "<디자인 과제로부터 살아남기>"
        )
    }
}

@Composable
@Preview
private fun SkeletonPreview() {
    Theme {
        LectureReviewSkeletonCell()
    }
}