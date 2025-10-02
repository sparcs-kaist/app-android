package com.example.soap.Features.FeedPost.Components

import PostCommentActionsMenu
import android.widget.Toast
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soap.Domain.Enums.FeedReportType
import com.example.soap.Domain.Enums.FeedVoteType
import com.example.soap.Domain.Models.Feed.FeedComment
import com.example.soap.Domain.Repositories.Feed.FakeFeedCommentRepository
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.example.soap.Features.Post.Components.PostCommentButton
import com.example.soap.Features.Post.Components.PostVoteButton
import com.example.soap.R
import com.example.soap.Shared.Extensions.timeAgoDisplay
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.example.soap.ui.theme.grayF8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FeedCommentRow(
    comment: FeedComment,
    isMine: Boolean? = null,
    isReply: Boolean,
    onReply: () -> Unit,
    feedCommentRepository: FeedCommentRepositoryProtocol,
) {
    var localComment by remember { mutableStateOf(comment) }

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
            Header(localComment, isMine, feedCommentRepository) { updated ->
                localComment = updated
            }

            Content(localComment)

            Footer(localComment, onReply, feedCommentRepository) { updated ->
                localComment = updated
            }
        }
    }
}

@Composable
private fun Header(
    comment: FeedComment,
    isMine: Boolean?,
    repo: FeedCommentRepositoryProtocol,
    update: (FeedComment) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileImage(comment)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (comment.isAuthor) comment.authorName + " (Author)" else comment.authorName,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = if (comment.isAuthor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.scrim
        )

        Text(
            text = comment.createdAt.timeAgoDisplay(),
            color = MaterialTheme.colorScheme.grayBB,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!comment.isDeleted) {
            PostCommentActionsMenu(
                enumClass = FeedReportType::class,
                isMine = isMine,
                onEdit = {/*Todo - edit*/ },
                onDelete = {
                    expanded = false
                    coroutineScope.launch {
                        val prev = comment.copy()
                        update(comment.copy(isDeleted = true))
                        try {
                            repo.deleteComment(comment.id)
                        } catch (e: Exception) {
                            update(prev)
                        }
                    }
                },
                onReport = {
                    coroutineScope.launch {
                        repo.reportComment(comment.id, it)
                    }
                    Toast.makeText(context, "신고가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                },
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
    val text = if (comment.isDeleted) "This comment has been deleted." else comment.content
    val color =
        if (comment.isDeleted) MaterialTheme.colorScheme.grayBB.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
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
            isMine = true,
            isReply = false,
            onReply = {},
            feedCommentRepository = FakeFeedCommentRepository()
        )
    }
}


@Composable
@Preview
private fun Preview2() {
    Theme {
        FeedCommentRow(
            comment = FeedComment.mockList()[0],
            isMine = false,
            isReply = true,
            onReply = {},
            feedCommentRepository = FakeFeedCommentRepository()
        )
    }
}