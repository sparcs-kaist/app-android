package com.example.soap.Features.FeedPost.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soap.Domain.Enums.FeedVoteType
import com.example.soap.Domain.Models.Feed.FeedComment
import com.example.soap.Domain.Models.Feed.FeedCreateComment
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
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
    isReply: Boolean,
    onReply: () -> Unit,
    feedCommentRepository: FeedCommentRepositoryProtocol
) {
    var localComment by remember { mutableStateOf(comment) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (isReply) {
            Icon(
                painter = painterResource(R.drawable.arrow_forward_ios),
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Header(localComment, feedCommentRepository) { updated ->
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
    repo: FeedCommentRepositoryProtocol,
    update: (FeedComment) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileImage(comment)

        Text(
            text = comment.authorName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Text(
            text = comment.createdAt.timeAgoDisplay(),
            color = MaterialTheme.colorScheme.grayBB,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    painterResource(R.drawable.more_horiz),
                    contentDescription = null
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (comment.isMyComment) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            // suspend call → launch coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                val prev = comment.copy()
                                update(comment.copy(isDeleted = true))
                                try {
                                    repo.deleteComment(comment.id)
                                } catch (e: Exception) {
                                    update(prev)
                                }
                            }
                        }
                    )
                }
            }
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
    val color = if (comment.isDeleted) MaterialTheme.colorScheme.grayBB.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun Footer(
    comment: FeedComment,
    onReply: (() -> Unit)?,
    repo: FeedCommentRepositoryProtocol,
    update: (FeedComment) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        if (comment.parentCommentID == null) {
            TextButton(onClick = { onReply?.invoke() }) {
                Text("Reply (${comment.replyCount})")
            }
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
                enabled = !comment.isMyComment
            )
        }
    }
}

suspend fun handleVote(
    comment: FeedComment,
    isUpVote: Boolean,
    repo: FeedCommentRepositoryProtocol,
    update: (FeedComment) -> Unit
) {
    val prev = comment.copy()

    val updated = when {
        isUpVote && comment.myVote == FeedVoteType.UP ->
            comment.copy(myVote = null, upVotes = comment.upVotes - 1)

        isUpVote && comment.myVote == FeedVoteType.DOWN ->
            comment.copy(myVote = FeedVoteType.UP, upVotes = comment.upVotes + 1, downVotes = comment.downVotes - 1)

        isUpVote ->
            comment.copy(myVote = FeedVoteType.UP, upVotes = comment.upVotes + 1)

        !isUpVote && comment.myVote == FeedVoteType.DOWN ->
            comment.copy(myVote = null, downVotes = comment.downVotes - 1)

        !isUpVote && comment.myVote == FeedVoteType.UP ->
            comment.copy(myVote = FeedVoteType.DOWN, upVotes = comment.upVotes - 1, downVotes = comment.downVotes + 1)

        else ->
            comment.copy(myVote = FeedVoteType.DOWN, downVotes = comment.downVotes + 1)
    }

    update(updated)

    try {
        if (isUpVote) {
            if (prev.myVote == FeedVoteType.UP) repo.deleteVote(prev.id)
            else repo.vote(prev.id, FeedVoteType.UP)
        } else {
            if (prev.myVote == FeedVoteType.DOWN) repo.deleteVote(prev.id)
            else repo.vote(prev.id, FeedVoteType.DOWN)
        }
    } catch (e: Exception) {
        update(prev)
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        FeedCommentRow(
            comment = FeedComment.mock(),
            isReply = false,
            onReply = {},
            feedCommentRepository = object : FeedCommentRepositoryProtocol {
                private val mockComments = FeedComment.mockList()
                override suspend fun fetchComments(postId: String): List<FeedComment> {
                    return mockComments
                }
                override suspend fun writeComment(
                    postId: String,
                    request: FeedCreateComment
                ): FeedComment {
                    return FeedComment.mock()
                }
                override suspend fun writeReply(
                    commentId: String,
                    request: FeedCreateComment
                ): FeedComment {
                    return FeedComment.mock()
                }
                override suspend fun deleteComment(commentId: String) {}
                override suspend fun vote(commentId: String, type: FeedVoteType) {}
                override suspend fun deleteVote(commentId: String) {}
            }
        )
    }
}