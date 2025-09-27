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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.soap.Domain.Enums.FeedVoteType
import com.example.soap.Domain.Models.Feed.FeedCreatePost
import com.example.soap.Domain.Models.Feed.FeedPost
import com.example.soap.Domain.Models.Feed.FeedPostPage
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Features.Post.Components.PostCommentButton
import com.example.soap.Features.Post.Components.PostShareButton
import com.example.soap.Features.Post.Components.PostVoteButton
import com.example.soap.R
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
    onComment: () -> Unit,
    feedPostRepository: FeedPostRepositoryProtocol
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        Header(post, onPostDeleted, showDeleteConfirmation, onPostDeleted) { showDeleteConfirmation = it }
        Content(post)
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
    onDelete: ((String) -> Unit)?,
    setShowDelete: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        ProfileImage(post)
        Spacer(Modifier.width(8.dp))
        Text(post.authorName, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (onPostDeleted != null) post.createdAt.timeAgoDisplay() else post.createdAt.relativeTimeString(),
            color = MaterialTheme.colorScheme.grayBB,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.weight(1f))
        if (onPostDeleted != null && post.isAuthor) {
            IconButton(onClick = { setShowDelete(true) }) {
                Icon(painterResource(R.drawable.more_horiz), contentDescription = null)
            }
        } else {
            Box(modifier = Modifier.size(32.dp))
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { setShowDelete(false) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete?.invoke(post.id)
                    setShowDelete(false)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { setShowDelete(false) }) { Text("Cancel") }
            },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") }
        )
    }
}

@Composable
fun Content(post: FeedPost) {
    Text(post.content, modifier = Modifier.padding(horizontal = 16.dp))
    if (post.images.isNotEmpty()) {
        PostImagesStrip(images = post.images)
    }
}

@Composable
fun Footer(
    post: FeedPost,
    onComment: () -> Unit,
    onPostDeleted: ((String) -> Unit)?,
    feedPostRepository: FeedPostRepositoryProtocol,
    coroutineScope: CoroutineScope
) {
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
            onUpVote = { coroutineScope.launch { upVote(postState, feedPostRepository, update = { postState = it }) } },
            onDownVote = { coroutineScope.launch { downVote(postState, feedPostRepository, update = { postState = it }) } },
            enabled = post.isAuthor
        )

        Spacer(Modifier.width(8.dp))
        PostCommentButton(commentCount = postState.commentCount, onClick = onComment)
        Spacer(Modifier.weight(1f))
        if (onPostDeleted == null) PostShareButton()
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
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
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
                        .size(width = 300.dp, height = 75.dp)
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
private fun Preview(){

    Theme {
        FeedPostRow(
            post = FeedPost.mock(),
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
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun SkeletonPreview(){
    Theme { FeedPostRowSkeleton() }
}