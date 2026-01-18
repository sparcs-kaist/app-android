package org.sparcs.App.Features.LectureDetail.Components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.App.Domain.Models.OTL.LectureReview
import org.sparcs.App.Domain.Models.OTL.ReportMailComposer
import org.sparcs.App.Domain.Repositories.OTL.FakeOTLCourseRepository
import org.sparcs.App.Domain.Repositories.OTL.OTLCourseRepositoryProtocol
import org.sparcs.App.Shared.Mocks.mock
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.gray64
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.R


@Composable
fun LectureReviewCell(
    review: LectureReview,
    repo: OTLCourseRepositoryProtocol,
) {

    var expanded by remember { mutableStateOf(false) }
    var isLikeButtonRunning = false
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var reviewChange by remember { mutableStateOf(review) }
    val haptic = LocalHapticFeedback.current
    val unknown = stringResource(R.string.unknown)

    Box(
        Modifier
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.lecture.professors.firstOrNull()?.name?.localized()
                        ?: unknown,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "${
                        review.lecture.year.toString().takeLast(2)
                    }${review.lecture.semester.shortCode}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.grayBB
                )

                Spacer(modifier = Modifier.weight(1f))

                Box {
                    Icon(
                        painter = painterResource(R.drawable.more_horiz),
                        contentDescription = "More",
                        modifier = Modifier.clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
//                        DropdownMenuItem(
//                            text = { Text(stringResource(R.string.translate)) },
//                            onClick = { // },
//                            leadingIcon = {
//                                Icon(
//                                    painter = painterResource(R.drawable.baseline_translate),
//                                    contentDescription = null
//                                )
//                            }
//                        )
//                        DropdownMenuItem(
//                            text = { Text(stringResource(R.string.summarise)) },
//                            onClick = { // },
//                            leadingIcon = {
//                                Icon(
//                                    painter = painterResource(R.drawable.baseline_summarize),
//                                    contentDescription = null
//                                )
//                            }
//                        )
//                        HorizontalDivider() - TODO REVIEW TRANSLATE AND SUMMARIZE
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.report)) },
                            onClick = { report(review, context, unknown) },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(4.dp))

            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                ReviewRatingLetter(
                    title = stringResource(R.string.grade),
                    value = review.gradeLetter
                )
                Spacer(modifier = Modifier.padding(8.dp))
                ReviewRatingLetter(title = stringResource(R.string.load), value = review.loadLetter)
                Spacer(modifier = Modifier.padding(8.dp))
                ReviewRatingLetter(
                    title = stringResource(R.string.speech),
                    value = review.speechLetter
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(
                        targetState = reviewChange.like.toString(),
                        label = "VotesTransition"
                    ) { targetCount ->
                        Text(targetCount)
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Icon(
                        painter = if (reviewChange.isLiked) painterResource(R.drawable.icon_arrowup) else painterResource(
                            R.drawable.icon_arrowup
                        ),
                        contentDescription = "Vote",
                        tint = if (reviewChange.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            if (isLikeButtonRunning) return@clickable
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            isLikeButtonRunning = true
                            toggleLike(reviewChange, repo, scope, context) { updated ->
                                reviewChange = updated
                            }
                            isLikeButtonRunning = false
                        }
                    )//TODO - icon 이미지 받아서 fill / outlined로 수정
                }
            }
        }
    }
}

@Composable
fun LectureReviewSkeletonCell() {
    Box(
        Modifier
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
        ) {
            // 상단 Row (교수명, 학기, more 아이콘 자리)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 20.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (it == 2) 0.7f else 1f)
                            .height(16.dp)
                            .padding(vertical = 2.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 20.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 20.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewRatingLetter(title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.gray64
        )

        Spacer(modifier = Modifier.padding(2.dp))

        Text(
            text = value,
            color = MaterialTheme.colorScheme.grayBB,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// MARK: - Helpers
private fun toggleLike(
    review: LectureReview,
    otlCourseRepository: OTLCourseRepositoryProtocol,
    scope: CoroutineScope,
    context: Context,
    update: (LectureReview) -> Unit,
) {
    scope.launch {
        val prev = review.copy()

        val updated = if (review.isLiked) {
            // If already liked, clicking will unlike and decrease count
            review.copy(
                isLiked = false,
                like = review.like - 1
            )
        } else {
            // If not liked yet, clicking will like and increase count
            review.copy(
                isLiked = true,
                like = review.like + 1
            )
        }

        update(updated)

        try {
            if (prev.isLiked) otlCourseRepository.unlikeReview(review.id)
            else otlCourseRepository.likeReview(review.id)
        } catch (e: Exception) {
            update(prev)
            Toast.makeText(context, "Error toggling like", Toast.LENGTH_SHORT).show()
            Log.e("toggleLike", "Error toggling like", e)
        }
    }
}

fun report(review: LectureReview, context: Context, unknown: String) {
    val urlString = ReportMailComposer.compose(
        title = review.lecture.title.localized(),
        code = review.lecture.code,
        year = review.lecture.year,
        semester = review.lecture.semester,
        professorName = review.lecture.professors.firstOrNull()?.name?.localized() ?: unknown,
        content = review.content
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(urlString)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("SettingsView", "Error launching email app", e)
        Toast.makeText(context, "Failed to open email app", Toast.LENGTH_SHORT).show()
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        LectureReviewCell(review = LectureReview.mock(), FakeOTLCourseRepository())
    }
}

@Composable
@Preview
private fun SkeletonPreview() {
    Theme {
        LectureReviewSkeletonCell()
    }
}