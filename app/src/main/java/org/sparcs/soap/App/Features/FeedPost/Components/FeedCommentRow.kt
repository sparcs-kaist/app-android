package org.sparcs.soap.App.Features.FeedPost.Components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Feed.FeedDeletionError
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Repositories.Feed.FakeFeedCommentRepository
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import org.sparcs.soap.App.Features.Post.Components.PostCommentButton
import org.sparcs.soap.App.Features.Post.Components.PostVoteButton
import org.sparcs.soap.App.Features.Settings.Components.InfoTooltip
import org.sparcs.soap.App.Shared.Extensions.isNetworkError
import org.sparcs.soap.App.Shared.Extensions.timeAgoDisplay
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.grayF8
import org.sparcs.soap.Features.Post.PostCommentActionsMenu
import org.sparcs.soap.R

@Composable
fun FeedCommentRow(
    comment: FeedComment,
    isReply: Boolean,
    onReply: () -> Unit,
    feedCommentRepository: FeedCommentRepositoryProtocol,
) {
    var localComment by remember { mutableStateOf(comment) }
    val coroutineScope = rememberCoroutineScope()

    var showAlert by remember { mutableStateOf(false) }
    @StringRes var alertTitle: Int by remember { mutableStateOf(0) }
    @StringRes var alertMessage: Int by remember { mutableStateOf(0) }

    fun showAlert(@StringRes title: Int, @StringRes message: Int) {
        alertTitle = title
        alertMessage = message
        showAlert = true
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (isReply) {
            Icon(
                imageVector = Icons.Rounded.SubdirectoryArrowRight,
                contentDescription = null,
                modifier = Modifier.padding(top = 8.dp, end = 4.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(vertical = 4.dp)
            ) //TODO - 추가할지 말지 고민
            Header(
                comment = localComment,
                onDelete = {
                    coroutineScope.launch {
                        try {
                            feedCommentRepository.deleteComment(localComment.id)
                            localComment = localComment.copy(isDeleted = true)
                        } catch (e: Exception) {
                            val message = if (e.isNetworkError()) {
                                R.string.network_connection_error
                            } else if (e is FeedDeletionError) {
                                e.errorDescription()
                            } else {
                                R.string.unexpected_error_deleting_comment
                            }
                            showAlert(
                                title = R.string.error,
                                message = message
                            )
                        }
                    }
                },
                onReport = {
                    coroutineScope.launch {
                        try {
                            feedCommentRepository.reportComment(localComment.id, it)
                            showAlert(
                                title = R.string.report_submitted,
                                message = R.string.reported_successfully
                            )
                        } catch (e: Exception) {
                            val message = if (e.isNetworkError()) {
                                R.string.network_connection_error
                            } else {
                                R.string.unexpected_error_reporting_comment
                            }
                            showAlert(
                                title = R.string.error,
                                message = message
                            )
                        }
                    }
                }
            )

            Content(localComment)

            Footer(localComment, onReply, feedCommentRepository) { updated ->
                localComment = updated
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) { Text(stringResource(R.string.ok)) }
            },
            title = { Text(stringResource(alertTitle)) },
            text = { Text(stringResource(alertMessage)) }
        )
    }
}

@Composable
private fun Header(
    comment: FeedComment,
    onDelete: () -> Unit,
    onReport: (FeedReportType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val author =
        if (comment.isAnonymous) {
            val number = comment.authorName.substringAfter("Anonymous", "").trim()
            "${stringResource(R.string.anonymous)} $number"
        } else {
            comment.authorName
        }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileImage(comment)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (comment.isAuthor) "$author (${stringResource(R.string.author)})" else author,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = if (comment.isAuthor) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
        Spacer(Modifier.width(8.dp))
        if (comment.isKaistIP) {
            InfoTooltip(
                tooltipText = stringResource(R.string.kaist_ip_verified),
                icon = painterResource(R.drawable.checkmark_seal_fill),
                tint = MaterialTheme.colorScheme.primary,
                iconSize = 15.dp
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = comment.createdAt.timeAgoDisplay(),
            color = MaterialTheme.colorScheme.grayBB,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!comment.isDeleted) {
            PostCommentActionsMenu(
                enumClass = FeedReportType::class,
                isMine = comment.isMyComment,
                onEdit = {/*Todo - edit*/ },
                onDelete = {
                    expanded = false
                    onDelete()
                },
                onReport = onReport,
                onTranslate = {/*Todo - translate*/ },
                isComment = true
            )
        }
    }
}

@Composable
private fun ProfileImage(comment: FeedComment) {
    if (comment.profileImageURL != null) {
        AsyncImage(
            model = comment.profileImageURL,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.grayF8),
            contentAlignment = Alignment.Center
        ) {
            Text("😀", fontSize = 12.sp)
        }
    }
}

@Composable
private fun Content(comment: FeedComment) {
    val text =
        if (comment.isDeleted) stringResource(R.string.this_comment_has_been_deleted) else comment.content
    val color =
        if (comment.isDeleted) MaterialTheme.colorScheme.grayBB.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }
    var hasMeasured by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val moreText = stringResource(R.string.more)
    val moreColor = MaterialTheme.colorScheme.grayBB

    val displayText = remember(text, expanded, isOverflowing) {
        if (expanded || !isOverflowing) {
            AnnotatedString(text)
        } else {
            val visibleEnd =
                textLayoutResult?.getLineEnd(2, visibleEnd = true) ?: text.length
            val safeEnd = visibleEnd.coerceAtMost(text.length)
            val visibleText = text.substring(0, safeEnd).trimEnd()
            buildAnnotatedString {
                append(visibleText)
                pushStringAnnotation(tag = "MORE", annotation = "expand")
                append("… ")
                withStyle(SpanStyle(color = moreColor, fontWeight = FontWeight.SemiBold)) {
                    append(moreText)
                }
            }
        }
    }

    ClickableText(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium.copy(color = color),
        maxLines = if (!expanded) 4 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layoutResult ->
            if (!hasMeasured && !expanded) {
                hasMeasured = true
                isOverflowing = layoutResult.hasVisualOverflow
                textLayoutResult = layoutResult
            }
        },
        onClick = { offset ->
            displayText.getStringAnnotations("MORE", offset, offset)
                .firstOrNull()?.let {
                    if (!comment.isDeleted) expanded = true
                }
        },
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun Footer(
    comment: FeedComment,
    onReply: (() -> Unit)?,
    repo: FeedCommentRepositoryProtocol,
    update: (FeedComment) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1f))

        if (comment.parentCommentID == null) {
            PostCommentButton(
                commentCount = comment.replyCount,
                onClick = { onReply?.invoke() }
            )
            Spacer(modifier = Modifier.padding(4.dp))
        }

        if (!comment.isDeleted) {
            PostVoteButton(
                myVote = when (comment.myVote) {
                    FeedVoteType.UP -> true
                    FeedVoteType.DOWN -> false
                    else -> null
                },
                votes = comment.upVotes - comment.downVotes,
                onUpVote = {
                    CoroutineScope(Dispatchers.IO).launch {
                        handleVote(comment, true, repo, update)
                    }
                },
                onDownVote = {
                    CoroutineScope(Dispatchers.IO).launch {
                        handleVote(comment, false, repo, update)
                    }
                },
                enabled = true
            )
        }
    }
}

suspend fun handleVote(
    comment: FeedComment,
    isUpVote: Boolean,
    repo: FeedCommentRepositoryProtocol,
    update: (FeedComment) -> Unit,
) {
    val prev = comment.copy()

    val updated = when {
        isUpVote && comment.myVote == FeedVoteType.UP -> comment.copy(
            myVote = null,
            upVotes = comment.upVotes - 1
        )

        isUpVote && comment.myVote == FeedVoteType.DOWN -> comment.copy(
            myVote = FeedVoteType.UP,
            upVotes = comment.upVotes + 1,
            downVotes = comment.downVotes - 1
        )

        isUpVote -> comment.copy(myVote = FeedVoteType.UP, upVotes = comment.upVotes + 1)
        !isUpVote && comment.myVote == FeedVoteType.DOWN -> comment.copy(
            myVote = null,
            downVotes = comment.downVotes - 1
        )

        !isUpVote && comment.myVote == FeedVoteType.UP -> comment.copy(
            myVote = FeedVoteType.DOWN,
            upVotes = comment.upVotes - 1,
            downVotes = comment.downVotes + 1
        )

        else -> comment.copy(myVote = FeedVoteType.DOWN, downVotes = comment.downVotes + 1)
    }

    update(updated)

    try {
        if (isUpVote) {
            if (prev.myVote == FeedVoteType.UP) repo.deleteVote(prev.id) else repo.vote(
                prev.id,
                FeedVoteType.UP
            )
        } else {
            if (prev.myVote == FeedVoteType.DOWN) repo.deleteVote(prev.id) else repo.vote(
                prev.id,
                FeedVoteType.DOWN
            )
        }
    } catch (e: Exception) {
        update(prev)
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        FeedCommentRow(
            comment = FeedComment.mock(),
            isReply = false,
            onReply = {},
            feedCommentRepository = FakeFeedCommentRepository(),
        )
    }
}


@Composable
@Preview
private fun Preview2() {
    Theme {
        FeedCommentRow(
            comment = FeedComment.mockList()[0],
            isReply = true,
            onReply = {},
            feedCommentRepository = FakeFeedCommentRepository(),
        )
    }
}