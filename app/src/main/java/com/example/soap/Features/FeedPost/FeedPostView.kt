package com.example.soap.Features.FeedPost

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soap.Domain.Models.Feed.FeedComment
import com.example.soap.Domain.Models.Feed.FeedPost
import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Features.Feed.Components.FeedPostRow
import com.example.soap.Features.Feed.FeedViewModel
import com.example.soap.Features.FeedPost.Components.FeedCommentRow
import com.example.soap.Shared.Mocks.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostView(
    post: MutableState<FeedPost>,
    onDelete: () -> Unit,
    viewModel: FeedPostViewModelProtocol = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var feedUser by remember { mutableStateOf<FeedUser?>(null) }
    var isWritingCommentFocusState by remember { mutableStateOf(false) }
    var targetComment by remember { mutableStateOf<FeedComment?>(null) }
    var isUploadingComment by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val repo: FeedPostRepositoryProtocol = hiltViewModel<FeedViewModel>().feedPostRepository

    val repo1: FeedCommentRepositoryProtocol = hiltViewModel<FeedPostViewModel>().feedCommentRepository

    LaunchedEffect(Unit) {
        viewModel.fetchComments(postID = post.value.id)
    }

    Column {
        TopAppBar(
            title = { Text("Post") },
            actions = {
                DropdownMenu(
                    expanded = showDeleteConfirmation,
                    onDismissRequest = { showDeleteConfirmation = false }
                ) {
                    if (post.value.isAuthor) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showDeleteConfirmation = true }
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            FeedPostRow(
                post = post.value,
                onPostDeleted = {},
                onComment = {
                targetComment = null
                isWritingCommentFocusState = true
            },
                feedPostRepository = repo )

            Comments(viewModel = viewModel, post = post, targetComment = targetComment, isWritingCommentFocusState = isWritingCommentFocusState, feedCommentRepository = repo1, onReply = { c ->
                targetComment = c
                isWritingCommentFocusState = true
            })
        }


        InputBar(
            post = post,
            viewModel = viewModel,
            targetComment = targetComment,
            isWritingCommentFocusState = isWritingCommentFocusState,
            isUploadingComment = isUploadingComment,
            onCommentUploaded = { uploadedComment ->
                post.value.commentCount += 1
                targetComment = null
                viewModel.text = ""
                isWritingCommentFocusState = false
                coroutineScope.launch {
                    // scroll to uploadedComment.id if needed
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
                Button(onClick = {
                    onDelete()
                    showDeleteConfirmation = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InputBar(
    post: MutableState<FeedPost>,
    viewModel: FeedPostViewModelProtocol,
    targetComment: FeedComment?,
    isWritingCommentFocusState: Boolean,
    isUploadingComment: Boolean,
    onCommentUploaded: (FeedComment?) -> Unit
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Column {
            TextField(
                value = viewModel.text,
                onValueChange = { viewModel.text = it },
                placeholder = {
                    Text(
                        if (targetComment != null)
                            "Write a reply to ${targetComment.authorName}"
                        else
                            "Write a comment"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // TODO: toggle anonymous if needed
        }

        if (viewModel.text.isNotEmpty()) {
            Button(
                onClick = {
                    if (viewModel.text.isEmpty()) return@Button

                    CoroutineScope(Dispatchers.Main).launch {
                        var uploadedComment: FeedComment? = null
                        try {
                            if (targetComment != null) {
                                uploadedComment = viewModel.writeReply(targetComment.id)
                            } else {
                                uploadedComment = viewModel.writeComment(post.value.id)
                            }
                        } catch (e: Exception) {
                            // handle error
                        }
                        onCommentUploaded(uploadedComment)
                    }
                },
                enabled = !isUploadingComment
            ) {
                if (isUploadingComment) {
                    CircularProgressIndicator()
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun Comments(
    viewModel: FeedPostViewModelProtocol,
    post: MutableState<FeedPost>,
    targetComment: FeedComment?,
    isWritingCommentFocusState: Boolean,
    feedCommentRepository: FeedCommentRepositoryProtocol,
    onReply: (FeedComment) -> Unit
) {
    when (val state = viewModel.state.collectAsState().value) {
        is FeedPostViewModel.ViewState.Loading -> {
            Column {
                HorizontalDivider()
                Text("${post.value.commentCount} comments", style = MaterialTheme.typography.bodyMedium)
                repeat(4) {
                    FeedCommentRow(comment = FeedComment.mock(), isReply = false, feedCommentRepository = feedCommentRepository, onReply = {})
                    HorizontalDivider()
                }
            }
        }
        is FeedPostViewModel.ViewState.Loaded -> {
            Column {
                HorizontalDivider()
                Text("${post.value.commentCount} comments", style = MaterialTheme.typography.bodyMedium)
                viewModel.comments.forEach { comment ->
                    FeedCommentRow(comment = comment, isReply = false, onReply = { onReply(comment) }, feedCommentRepository = feedCommentRepository )
                    comment.replies.forEach { reply ->
                        FeedCommentRow(comment = reply, isReply = true, onReply = {}, feedCommentRepository = feedCommentRepository )
                    }
                    HorizontalDivider()
                }
            }
        }
        is FeedPostViewModel.ViewState.Error -> {
            Text("Error: ${state.message}")
        }
    }
}
