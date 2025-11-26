package com.sparcs.soap.Features.FeedPost.Components

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import com.sparcs.soap.Domain.Enums.Feed.FeedReportType
import com.sparcs.soap.Domain.Enums.Feed.FeedVoteType
import com.sparcs.soap.Domain.Models.Feed.FeedComment
import com.sparcs.soap.Domain.Repositories.Feed.FakeFeedCommentRepository
import com.sparcs.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.sparcs.soap.Features.FeedPost.FeedPostViewModel
import com.sparcs.soap.Features.FeedPost.FeedPostViewModelProtocol
import com.sparcs.soap.Features.Post.Components.PostCommentButton
import com.sparcs.soap.Features.Post.Components.PostVoteButton
import com.sparcs.soap.Features.Post.PostCommentActionsMenu
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Extensions.timeAgoDisplay
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.Shared.Mocks.mockList
import com.sparcs.soap.Shared.ViewModelMocks.Feed.MockFeedPostViewModel
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import com.sparcs.soap.ui.theme.grayF8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FeedCommentRow(
    viewModel: FeedPostViewModelProtocol,
    comment: FeedComment,
    isMine: Boolean? = null,
    isReply: Boolean,
    onReply: () -> Unit,
    feedCommentRepository: FeedCommentRepositoryProtocol,
) {
    var localComment by remember { mutableStateOf(comment) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val deleteSuccessText = stringResource(R.string.deleted_successfully)

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
                painter = painterResource(R.drawable.round_subdirectory_arrow_right),
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
                isMine = isMine,
                onDelete = {
                    coroutineScope.launch {
                        localComment = localComment.copy(isDeleted = true)
                        feedCommentRepository.deleteComment(localComment.id)
                    }
                    Toast.makeText(context, deleteSuccessText, Toast.LENGTH_SHORT).show()
                },
                onReport = {
                    coroutineScope.launch {
                        try {
                            feedCommentRepository.reportComment(localComment.id, it)
                            showAlert(title = R.string.report_submitted, message= R.string.reported_successfully)
                        } catch (e: Exception) {
                            viewModel.handleException(error = e)
                            showAlert = true
                            showAlert(title= R.string.error, message= R.string.unexpected_error_reporting_comment)
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
    isMine: Boolean?,
    onDelete: () -> Unit,
    onReport: (FeedReportType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileImage(comment)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (comment.isAuthor) comment.authorName + " (${stringResource(R.string.author)})" else comment.authorName,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = if (comment.isAuthor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.scrim
        )
        Spacer(Modifier.width(8.dp))
        if(comment.isKaistIP){
            Icon(
                painter = painterResource(R.drawable.checkmark_seal_fill),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(15.dp)
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
                isMine = isMine,
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
            viewModel = MockFeedPostViewModel(initialState = FeedPostViewModel.ViewState.Loaded),
            comment = FeedComment.mock(),
            isMine = true,
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
            viewModel = MockFeedPostViewModel(initialState = FeedPostViewModel.ViewState.Loaded),
            comment = FeedComment.mockList()[0],
            isMine = false,
            isReply = true,
            onReply = {},
            feedCommentRepository = FakeFeedCommentRepository(),
        )
    }
}