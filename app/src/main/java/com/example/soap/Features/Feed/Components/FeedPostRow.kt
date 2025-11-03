package com.example.soap.Features.Feed.Components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
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
import coil.compose.AsyncImage
import com.example.soap.Domain.Enums.FeedReportType
import com.example.soap.Domain.Enums.FeedVoteType
import com.example.soap.Domain.Models.Feed.FeedCreatePost
import com.example.soap.Domain.Models.Feed.FeedPost
import com.example.soap.Domain.Models.Feed.FeedPostPage
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Features.Post.Components.PostCommentButton
import com.example.soap.Features.Post.Components.PostShareButton
import com.example.soap.Features.Post.Components.PostVoteButton
import com.example.soap.R
import com.example.soap.Shared.Extensions.noRippleClickable
import com.example.soap.Shared.Extensions.relativeTimeString
import com.example.soap.Shared.Extensions.timeAgoDisplay
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FeedPostRow(
    post: FeedPost,
    onPostDeleted: (String) -> Unit,
    onComment: () -> Unit, //post 또는 comment click
    singleLine: Boolean,
    feedPostRepository: FeedPostRepositoryProtocol,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier
        .fillMaxWidth()
        .noRippleClickable { onComment() }
    ) {
        Header(
            post,
            onPostDeleted,
            showDeleteConfirmation,
        ) { showDeleteConfirmation = it }
        Content(post, singleLine, onComment)
        Footer(post, onComment, onPostDeleted, feedPostRepository, coroutineScope)
    }
}

@Composable
fun ProfileImage(post: FeedPost) {
    if (post.profileImageURL != null) {
        AsyncImage(
            model = post.profileImageURL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text("😀", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun Header(
    post: FeedPost,
    onPostDeleted: ((String) -> Unit)?,
    showDeleteConfirmation: Boolean,
    setShowDelete: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        ProfileImage(post)
        Spacer(Modifier.width(8.dp))
        Text(post.authorName, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(// onPostDeleted == nil here means FeedPostRow is in the FeedPostView.
            text = if (onPostDeleted != null) post.createdAt.timeAgoDisplay() else post.createdAt.relativeTimeString(),
            color = MaterialTheme.colorScheme.grayBB,
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { setShowDelete(false) },
            confirmButton = {
                TextButton(onClick = {
                    onPostDeleted?.invoke(post.id)
                    setShowDelete(false)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { setShowDelete(false) }) { Text(stringResource(R.string.cancel)) }
            },
            title = { Text(stringResource(R.string.delete_post)) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_post)) }
        )
    }
}

@Composable
fun Content(
    post: FeedPost,
    singleLine: Boolean,
    onComment: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }
    var hasMeasured by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val moreColor = MaterialTheme.colorScheme.grayBB
    val moreText = stringResource(R.string.more)
    val displayText = remember(post.content, expanded, isOverflowing) {
        if (expanded || !isOverflowing) {
            AnnotatedString(post.content)
        } else {
            val visibleEnd =
                textLayoutResult?.getLineEnd(0, visibleEnd = true) ?: post.content.length
            val safeEnd = visibleEnd.coerceAtMost(post.content.length)
            val visibleText = post.content.substring(0, safeEnd).trimEnd()

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
        maxLines = if (singleLine && !expanded) 2 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layoutResult ->
            if (!hasMeasured && !expanded) {
                hasMeasured = true
                isOverflowing = layoutResult.hasVisualOverflow
                textLayoutResult = layoutResult
            }
        },
        onClick = { offset ->
            val moreAnnotation =
                displayText.getStringAnnotations("MORE", offset, offset).firstOrNull()
            if (moreAnnotation != null) {
                if (!expanded && isOverflowing) expanded = true
            } else {
                onComment()
            }
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
    )

    if (post.images.isNotEmpty()) {
        PostImagesStrip(images = post.images, onComment)
    }
}

@Composable
fun Footer(
    post: FeedPost,
    onComment: () -> Unit,
    onPostDeleted: ((String) -> Unit)?,
    feedPostRepository: FeedPostRepositoryProtocol,
    coroutineScope: CoroutineScope,
) {
    val context = LocalContext.current
    var postState by remember { mutableStateOf(post) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PostVoteButton(
            myVote = when (postState.myVote) {
                FeedVoteType.UP -> true
                FeedVoteType.DOWN -> false
                else -> null
            },
            votes = postState.upVotes - postState.downVotes,
            onUpVote = {
                coroutineScope.launch {
                    upVote(
                        postState,
                        feedPostRepository,
                        update = { postState = it })
                }
            },
            onDownVote = {
                coroutineScope.launch {
                    downVote(
                        postState,
                        feedPostRepository,
                        update = { postState = it })
                }
            },
            enabled = true
        )

        Spacer(Modifier.width(8.dp))
        PostCommentButton(commentCount = postState.commentCount) { onComment() }
        Spacer(Modifier.weight(1f))
        if (onPostDeleted == null) PostShareButton(
            url = "https://sparcs.org/feed/${post.id}",
            context = context
        )
    }
}

// MARK: - Functions
suspend fun upVote(post: FeedPost, repo: FeedPostRepositoryProtocol, update: (FeedPost) -> Unit) {
    val prev = post.copy()

    val updated = when (post.myVote) {
        // cancel upvote
        FeedVoteType.UP -> post.copy(myVote = null, upVotes = post.upVotes - 1)

        // upvote
        FeedVoteType.DOWN -> post.copy(
            myVote = FeedVoteType.UP,
            upVotes = post.upVotes + 1,
            // remove downvote if there was
            downVotes = post.downVotes - 1
        )

        else -> post.copy(myVote = FeedVoteType.UP, upVotes = post.upVotes + 1)
    }

    update(updated)

    try {
        if (prev.myVote == FeedVoteType.UP) {
            repo.deleteVote(prev.id)
        } else {
            repo.vote(prev.id, FeedVoteType.UP)
        }
    } catch (e: Exception) {
        update(prev)
    }
}

suspend fun downVote(post: FeedPost, repo: FeedPostRepositoryProtocol, update: (FeedPost) -> Unit) {
    val prev = post.copy()

    val updated = when (post.myVote) {
        //cancel upvote
        FeedVoteType.DOWN -> post.copy(myVote = null, downVotes = post.downVotes - 1)
        //upvote
        FeedVoteType.UP -> post.copy(
            myVote = FeedVoteType.DOWN,
            upVotes = post.upVotes - 1,
            // remove downvote if there was
            downVotes = post.downVotes + 1
        )

        else -> post.copy(myVote = FeedVoteType.DOWN, downVotes = post.downVotes + 1)
    }

    update(updated)

    try {
        if (prev.myVote == FeedVoteType.DOWN) {
            repo.deleteVote(prev.id)
        } else {
            repo.vote(prev.id, FeedVoteType.DOWN)
        }
    } catch (e: Exception) {
        update(prev)
    }
}

@Composable
fun FeedPostRowSkeleton() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(12.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                Box(
                    modifier = Modifier
                        .size(width = 400.dp, height = 200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(32.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(32.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        FeedPostRow(
            post = FeedPost.mock(),
            singleLine = true,
            onPostDeleted = {},
            onComment = {},
            feedPostRepository = object : FeedPostRepositoryProtocol {
                override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage {
                    return FeedPostPage(
                        items = FeedPost.mockList(),
                        nextCursor = "",
                        hasNext = false
                    )
                }

                override suspend fun writePost(request: FeedCreatePost) {}
                override suspend fun deletePost(postID: String) {}
                override suspend fun vote(postID: String, type: FeedVoteType) {}
                override suspend fun deleteVote(postID: String) {}
                override suspend fun reportPost(postID: String, reason: FeedReportType) {}
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun SkeletonPreview() {
    Theme { FeedPostRowSkeleton() }
}