package org.sparcs.soap.App.Features.FeedPost

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
import androidx.compose.material3.TextButton
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
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRow
import org.sparcs.soap.App.Features.Feed.FeedViewModelProtocol
import org.sparcs.soap.App.Features.FeedPost.Components.FeedCommentRow
import org.sparcs.soap.App.Features.FeedPost.Components.FeedPostNavigationBar
import org.sparcs.soap.App.Features.NavigationBar.Animation.MoveToLeftFadeIn
import org.sparcs.soap.App.Shared.Extensions.PullToRefreshHapticHandler
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Extensions.toggle
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Feed.MockFeedPostViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.BuddyPreviewSupport.Feed.PreviewFeedViewModel
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostView(
    viewModel: FeedPostViewModelProtocol = hiltViewModel(),
    feedViewModel: FeedViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val postState by viewModel.state.collectAsState()
    val isAlertPresented = viewModel.isAlertPresented
    val alertState = viewModel.alertState

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
    LaunchedEffect(Unit) {
        viewModel.fetchFeedUser()
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
            val post = feedViewModel.posts.find { it.id == state.post.id } ?: state.post

            Scaffold(
                topBar = {
                    FeedPostNavigationBar(
                        navController = navController,
                        onDelete = { showDeleteConfirmation = true },
                        onReport = { reason ->
                            coroutineScope.launch {
                                viewModel.reportPost(post.id, reason)
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
                            coroutineScope.launch {
                                val uploaded = viewModel.submitComment(post.id, targetComment)
                                if (uploaded != null) {
                                    post.commentCount += 1
                                    targetComment = null
                                    isWritingCommentFocusState = false

                                    val index =
                                        viewModel.comments.indexOfFirst { it.id == uploaded.id }
                                    if (index != -1) {
                                        proxy.animateScrollToItem(index)
                                    }
                                }
                            }
                        },
                        focusRequester = focusRequester,
                        isUploadingComment = isUploadingComment
                    )
                },
                modifier = Modifier
                    .analyticsScreen(
                        name = "Feed Post",
                        "is_author" to post.isAuthor,
                        "has_comments" to (post.commentCount > 0)
                    ),
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
                                            feedViewModel.deletePost(post.id)
                                            showDeleteConfirmation = false
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            showDeleteConfirmation = false
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
    if (isAlertPresented && alertState != null) {
        AlertDialog(
            onDismissRequest = { viewModel.isAlertPresented = false },
            confirmButton = {
                TextButton(onClick = { viewModel.isAlertPresented = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(alertState.titleResId)) },
            text = {
                alertState.message?.let { Text(it) } ?: alertState.messageResId?.let {
                    Text(
                        stringResource(it)
                    )
                }
            }
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
                        viewModel
                    )
                    comment.replies.forEach { reply ->
                            FeedCommentRow(
                                comment = reply,
                                isReply = true,
                                onReply = {},
                                viewModel
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

// MARK: - Previews
@Preview(showBackground = true, name = "Post Detail")
@Composable
private fun PostDetailPreview() {
    val mockVM = MockFeedPostViewModel(
        initialState = FeedPostViewModel.ViewState.Loaded(FeedPost.mock())
    ).apply {
        comments = FeedComment.mockList()
    }
    val mockFeedVM = PreviewFeedViewModel()

    Theme {
        FeedPostView(viewModel = mockVM, mockFeedVM, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "With Comments")
@Composable
private fun WithCommentsPreview() {
    val mockVM = MockFeedPostViewModel(
        initialState = FeedPostViewModel.ViewState.Loaded(FeedPost.mockList()[3])
    ).apply {
        comments = FeedComment.mockList()
    }
    val mockFeedVM = PreviewFeedViewModel()

    Theme {
        FeedPostView(viewModel = mockVM, mockFeedVM, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Author Post")
@Composable
private fun AuthorPostPreview() {
    val mockVM = MockFeedPostViewModel(
        initialState = FeedPostViewModel.ViewState.Loaded(FeedPost.mockList()[0])
    ).apply {
        isAnonymous = false
    }
    val mockFeedVM = PreviewFeedViewModel()

    Theme {
        FeedPostView(viewModel = mockVM, mockFeedVM, navController = rememberNavController())
    }
}