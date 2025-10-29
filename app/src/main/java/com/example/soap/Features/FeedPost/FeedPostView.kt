package com.example.soap.Features.FeedPost

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.soap.Domain.Models.Feed.FeedComment
import com.example.soap.Domain.Models.Feed.FeedPost
import com.example.soap.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Features.Feed.Components.FeedPostRow
import com.example.soap.Features.Feed.FeedViewModel
import com.example.soap.Features.FeedPost.Components.FeedCommentRow
import com.example.soap.Features.FeedPost.Components.FeedPostNavigationBar
import com.example.soap.Features.NavigationBar.Animation.MoveToLeftFadeIn
import com.example.soap.R
import com.example.soap.ui.theme.lightGray0
import com.google.gson.Gson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostView(
    viewModel: FeedPostViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val proxy = rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isWritingCommentFocusState by remember { mutableStateOf(false) }
    var targetComment by remember { mutableStateOf<FeedComment?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isUploadingComment by remember { mutableStateOf(false) }

    val repo: FeedPostRepositoryProtocol = hiltViewModel<FeedViewModel>().feedPostRepository
    val repo1: FeedCommentRepositoryProtocol =
        hiltViewModel<FeedPostViewModel>().feedCommentRepository
    val vm = hiltViewModel<FeedViewModel>()

    val backStackEntry = navController.currentBackStackEntry!!
    val json = backStackEntry.savedStateHandle.get<String>("feed_json")
    val post = remember { mutableStateOf(Gson().fromJson(json, FeedPost::class.java)) }
    val context = LocalContext.current

    val deleteSuccessText = stringResource(R.string.deleted_successfully)
    val reportSuccessText = stringResource(R.string.reported_successfully)

    LaunchedEffect(Unit) {
        viewModel.fetchComments(postID = post.value.id)
    }

    Scaffold(
        topBar = {
            FeedPostNavigationBar(
                navController = navController,
                onDelete = { showDeleteConfirmation = true },
                onReport = {
                    coroutineScope.launch {
                        repo.reportPost(post.value.id, it)
                    }
                    Toast.makeText(context, reportSuccessText, Toast.LENGTH_SHORT).show()
                },
                onTranslate = {/*Todo - translate*/ },
                isMine = post.value.isAuthor
            )
        },
        bottomBar = {
            InputBar(
                viewModel = viewModel,
                targetComment = targetComment,
                isWritingCommentFocusState = isWritingCommentFocusState,
                onCommentUploaded = {
                    if (viewModel.text.isEmpty()) return@InputBar
                    isUploadingComment = true
                    coroutineScope.launch {
                        var uploadedComment: FeedComment?
                        try {
                            uploadedComment = if (targetComment != null) {
                                viewModel.writeReply(targetComment!!.id)
                            } else {
                                viewModel.writeComment(post.value.id)
                            }

                            uploadedComment.let { comment ->
                                post.value.commentCount += 1
                                targetComment = null
                                viewModel.text = ""
                                isWritingCommentFocusState = false

                                val index = viewModel.comments.indexOfFirst { it.id == comment.id }
                                if (index != -1) {
                                    proxy.animateScrollToItem(index)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FeedPostView", "Failed to upload comment", e)
                        } finally {
                            isUploadingComment = false
                        }
                    }
                },
                focusRequester = focusRequester,
                isUploadingComment = isUploadingComment
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.fetchComments(postID = post.value.id)
                }
                isRefreshing = false
            }
        ) {
            LazyColumn(
                Modifier.padding(innerPadding),
                state = proxy
            ) {
                item {
                    FeedPostRow(
                        post = post.value,
                        singleLine = false,
                        onPostDeleted = {},
                        onComment = {
                            targetComment = null
                            isWritingCommentFocusState = true
                        },
                        feedPostRepository = repo
                    )
                }

                item {
                    Comments(
                        viewModel = viewModel,
                        post = post,
                        isMine = post.value.isAuthor,
                        targetComment = targetComment,
                        isWritingCommentFocusState = isWritingCommentFocusState,
                        feedCommentRepository = repo1,
                        onReply = { c ->
                            targetComment = c
                            isWritingCommentFocusState = true
                        }
                    )
                }
            }
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text(text = stringResource(R.string.delete_post), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_post)) },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch { vm.deletePost(post.value.id) }
                            showDeleteConfirmation = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun InputBar(
    viewModel: FeedPostViewModelProtocol,
    targetComment: FeedComment?,
    isWritingCommentFocusState: Boolean,
    onCommentUploaded: () -> Unit,
    focusRequester: FocusRequester,
    isUploadingComment: Boolean
) {
    var isFocused by remember { mutableStateOf(isWritingCommentFocusState) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            if (isFocused) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.write_anonymously))
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = viewModel.isAnonymous,
                        onCheckedChange = { viewModel.isAnonymous = it },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = viewModel.text,
                        onValueChange = { viewModel.text = it },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                            },
                        maxLines = 6,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (viewModel.text.isEmpty()) {
                                    Text(
                                        text = if (targetComment != null)
                                            stringResource(
                                                R.string.write_a_reply_to,
                                                targetComment.authorName
                                            )
                                        else
                                            stringResource(R.string.write_a_comment),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                MoveToLeftFadeIn(viewModel.text.isNotEmpty()) {

                    Button(
                        onClick = onCommentUploaded,
                        enabled = !isUploadingComment
                    ) {
                        if (isUploadingComment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.paperplane),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = stringResource(R.string.send)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Comments(
    viewModel: FeedPostViewModelProtocol,
    post: MutableState<FeedPost>,
    isMine: Boolean,
    targetComment: FeedComment?,
    isWritingCommentFocusState: Boolean,
    feedCommentRepository: FeedCommentRepositoryProtocol,
    onReply: (FeedComment) -> Unit,
) {
    when (val state = viewModel.state.collectAsState().value) {
        is FeedPostViewModel.ViewState.Loading -> {
            Column(Modifier.padding(horizontal = 8.dp)) {
                HorizontalDivider()
                Text(
                    stringResource(
                        R.string.the_number_of_comments,
                        post.value.commentCount
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        is FeedPostViewModel.ViewState.Loaded -> {
            Column(Modifier.padding(horizontal = 8.dp)) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    stringResource(
                        R.string.the_number_of_comments,
                        post.value.commentCount
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                viewModel.comments.forEach { comment ->
                    FeedCommentRow(
                        comment = comment,
                        isMine = isMine,
                        isReply = false,
                        onReply = { onReply(comment) },
                        feedCommentRepository = feedCommentRepository
                    )
                    comment.replies.forEach { reply ->
                        FeedCommentRow(
                            comment = reply,
                            isMine = isMine,
                            isReply = true,
                            onReply = {},
                            feedCommentRepository = feedCommentRepository
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.lightGray0,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        is FeedPostViewModel.ViewState.Error -> {
            Text("Error: ${state.message}", Modifier.padding(horizontal = 8.dp))
        }
    }
}