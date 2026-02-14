package org.sparcs.soap.App.Features.FeedPost

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Feed.FeedDeletionError
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Repositories.Feed.FakeFeedCommentRepository
import org.sparcs.soap.App.Domain.Repositories.Feed.FakeFeedPostRepository
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedCommentRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRow
import org.sparcs.soap.App.Features.Feed.FeedViewModel
import org.sparcs.soap.App.Features.Feed.FeedViewModelProtocol
import org.sparcs.soap.App.Features.FeedPost.Components.FeedCommentRow
import org.sparcs.soap.App.Features.FeedPost.Components.FeedPostNavigationBar
import org.sparcs.soap.App.Features.NavigationBar.Animation.MoveToLeftFadeIn
import org.sparcs.soap.App.Shared.Extensions.PullToRefreshHapticHandler
import org.sparcs.soap.App.Shared.Extensions.isNetworkError
import org.sparcs.soap.App.Shared.Extensions.toggle
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Feed.MockFeedPostViewModel
import org.sparcs.soap.App.Shared.ViewModelMocks.Feed.MockFeedViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostView(
    viewModel: FeedPostViewModelProtocol = hiltViewModel(),
    feedViewModel: FeedViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val postState by viewModel.state.collectAsState()
    val feedState by feedViewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val proxy = rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isWritingCommentFocusState by remember { mutableStateOf(false) }
    var targetComment by remember { mutableStateOf<FeedComment?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isUploadingComment by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    PullToRefreshHapticHandler(pullState, isRefreshing)

    val isPreview = LocalInspectionMode.current
    val repo: FeedPostRepositoryProtocol =
        if (!isPreview) hiltViewModel<FeedViewModel>().feedPostRepository else FakeFeedPostRepository()
    val repo1: FeedCommentRepositoryProtocol =
        if (!isPreview) hiltViewModel<FeedPostViewModel>().feedCommentRepository else FakeFeedCommentRepository()
    val vm = if (!isPreview) hiltViewModel<FeedViewModel>() else MockFeedViewModel(
        initialState = FeedViewModel.ViewState.Loaded(FeedPost.mockList())
    )

    var showAlert by remember { mutableStateOf(false) }
    @StringRes var alertTitle: Int by remember { mutableStateOf(0) }
    @StringRes var alertMessage: Int by remember { mutableStateOf(0) }

    fun showAlert(@StringRes title: Int, @StringRes message: Int) {
        alertTitle = title
        alertMessage = message
        showAlert = true
    }

    when (val state = postState) {
        is FeedPostViewModel.ViewState.Loading -> {
            LoadingView(navController)
        }

        is FeedPostViewModel.ViewState.Error -> {
            ErrorView(
                icon = Icons.Default.Warning,
                message = state.message,
                onRetry = {
                    coroutineScope.launch {
                        viewModel.post?.let { viewModel.fetchComments(it.id, initial = true) }
                    }
                }
            )
        }

        is FeedPostViewModel.ViewState.Loaded -> {
            val post = if (feedState is FeedViewModel.ViewState.Loaded) {
                (feedState as FeedViewModel.ViewState.Loaded).posts.find { it.id == state.post.id }
                    ?: state.post
            } else {
                state.post
            }

            LaunchedEffect(Unit) {
                viewModel.fetchComments(postID = post.id, initial = true)
            }

            Scaffold(
                topBar = {
                    FeedPostNavigationBar(
                        navController = navController,
                        onDelete = { showDeleteConfirmation = true },
                        onReport = {
                            coroutineScope.launch {
                                try {
                                    repo.reportPost(post.id, it)
                                    showAlert(
                                        title = R.string.report_submitted,
                                        message = R.string.reported_successfully
                                    )
                                } catch (e: Exception) {
                                    val message = if (e.isNetworkError()) {
                                        R.string.network_connection_error
                                    } else {
                                        R.string.unexpected_error_reporting_post
                                    }

                                    showAlert(
                                        title = R.string.error,
                                        message = message
                                    )
                                }
                            }
                        },
                        onTranslate = {/*Todo - translate*/ },
                        isMine = post.isAuthor
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
                                val uploadedComment: FeedComment?
                                try {
                                    uploadedComment = if (targetComment != null) {
                                        viewModel.writeReply(targetComment!!.id)
                                    } else {
                                        viewModel.writeComment(post.id)
                                    }

                                    uploadedComment.let { comment ->
                                        post.commentCount += 1
                                        targetComment = null
                                        viewModel.text = ""
                                        isWritingCommentFocusState = false

                                        val index =
                                            viewModel.comments.indexOfFirst { it.id == comment.id }
                                        if (index != -1) {
                                            proxy.animateScrollToItem(index)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("FeedPostView", "Failed to upload comment", e)
                                    showAlert(
                                        title = R.string.error,
                                        message = R.string.unexpected_error_uploading_comment
                                    )
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
                            viewModel.fetchComments(postID = post.id, initial = false)
                            delay(500)
                            isRefreshing = false
                        }
                    },
                    state = pullState,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    LazyColumn(
                        state = proxy
                    ) {
                        item {
                            FeedPostRow(
                                post = post,
                                viewModel = feedViewModel,
                                singleLine = false,
                                onPostDeleted = {},
                                onComment = {
                                    targetComment = null
                                    isWritingCommentFocusState = true
                                }
                            )
                        }

                        item {
                            Comments(
                                viewModel = viewModel,
                                post = post,
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
                        title = {
                            Text(
                                text = stringResource(R.string.delete_post),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_post)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            vm.deletePost(post.id)
                                            showDeleteConfirmation = false
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("listNeedsRefresh", true)
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            showDeleteConfirmation = false

                                            val resId = when (e) {
                                                is FeedDeletionError -> e.errorDescription()
                                                else -> R.string.unexpected_error_deleting_post
                                            }

                                            showAlert(R.string.error, resId)
                                        }
                                    }
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
    }
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                Button(onClick = { showAlert = false }) { Text(stringResource(R.string.ok)) }
            },
            title = { Text(stringResource(alertTitle)) },
            text = { Text(stringResource(alertMessage)) }
        )
    }

}


@Composable
private fun InputBar(
    viewModel: FeedPostViewModelProtocol,
    targetComment: FeedComment?,
    isWritingCommentFocusState: Boolean,
    onCommentUploaded: () -> Unit,
    focusRequester: FocusRequester,
    isUploadingComment: Boolean,
) {
    var isFocused by remember { mutableStateOf(isWritingCommentFocusState) }
    val haptic = LocalHapticFeedback.current
    val rawName = targetComment?.authorName ?: ""
    val authorName = if (rawName.contains("Anonymous")) {
        rawName.replace("Anonymous", stringResource(R.string.anonymous))
    } else {
        rawName
    }//TODO: 백엔드에서 번역 지원하면 삭제

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
                        onCheckedChange = {
                            haptic.toggle(it)
                            viewModel.isAnonymous = it
                        },
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
                                                authorName
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
                                painter = painterResource(id = R.drawable.outline_send),
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
    post: FeedPost,
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
                        post.commentCount
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
                        post.commentCount
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                viewModel.comments.forEach { comment ->
                    FeedCommentRow(
                        comment = comment,
                        isReply = false,
                        onReply = { onReply(comment) },
                        feedCommentRepository = feedCommentRepository,
                    )
                    comment.replies.forEach { reply ->
                        FeedCommentRow(
                            comment = reply,
                            isReply = true,
                            onReply = {},
                            feedCommentRepository = feedCommentRepository,
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
            val coroutineScope = rememberCoroutineScope()
            ErrorView(
                icon = Icons.Default.Warning,
                message = state.message,
                onRetry = {
                    coroutineScope.launch {
                        viewModel.fetchComments(
                            post.id,
                            initial = true
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingView(
    navController: NavController,
) {
    Scaffold(
        topBar = {
            FeedPostNavigationBar(
                navController = navController,
                onDelete = {},
                onReport = {},
                onTranslate = {},
                isMine = false
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: FeedPostViewModel.ViewState) {
    val mockViewModel = remember { MockFeedPostViewModel(initialState = state) }
    val mockFeedViewModel =
        remember { MockFeedViewModel(initialState = FeedViewModel.ViewState.Loaded(FeedPost.mockList())) }
    FeedPostView(
        viewModel = mockViewModel,
        feedViewModel = mockFeedViewModel,
        navController = rememberNavController()
    )
}

@Composable
@Preview
private fun LoadingPreview() {
    Theme { MockView(FeedPostViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(FeedPostViewModel.ViewState.Loaded(FeedPost.mock(), emptyList())) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(FeedPostViewModel.ViewState.Error("Error Message")) }
}