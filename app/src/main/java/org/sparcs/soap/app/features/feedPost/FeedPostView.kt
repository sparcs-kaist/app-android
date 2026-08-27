package org.sparcs.soap.app.features.feedPost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.features.feed.FeedViewModel
import org.sparcs.soap.app.features.feed.FeedViewModelProtocol
import org.sparcs.soap.app.features.feed.components.FeedPostRow
import org.sparcs.soap.app.features.feedPost.components.FeedCommentRow
import org.sparcs.soap.app.features.feedPost.components.FeedPostNavigationBar
import org.sparcs.soap.app.features.navigationBar.animation.MoveToLeftFadeIn
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.landscapeHideOnScrollBehavior
import org.sparcs.soap.app.shared.extensions.toggle
import org.sparcs.soap.app.shared.mocks.feed.mock
import org.sparcs.soap.app.shared.mocks.feed.mockList
import org.sparcs.soap.app.shared.viewModelMocks.feed.MockFeedPostViewModel
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.shared.views.contentViews.PostSummarizationSheet
import org.sparcs.soap.app.shared.views.contentViews.PostTranslationSheet
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.lightGray0
import org.sparcs.soap.buddyPreviewSupport.feed.PreviewFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostView(
    viewModel: FeedPostViewModelProtocol = hiltViewModel<FeedPostViewModel>(),
    feedViewModel: FeedViewModelProtocol = hiltViewModel<FeedViewModel>(),
    navController: NavController,
) {
    val postState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFeedUser()
    }

    when (val state = postState) {
        is FeedPostViewModel.ViewState.Loading -> LoadingView(navController)

        is FeedPostViewModel.ViewState.Error -> ErrorView(
            error = state.error,
            onRetry = { viewModel.post?.let { viewModel.fetchComments(it.id, initial = true) } }
        )

        is FeedPostViewModel.ViewState.Loaded -> {
            val post = feedViewModel.posts.find { it.id == state.post.id } ?: state.post
            FeedPostContent(
                post = post,
                comments = viewModel.comments,
                viewModel = viewModel,
                feedViewModel = feedViewModel,
                navController = navController
            )
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )

    GlobalAlertDialog(
        isPresented = feedViewModel.isAlertPresented,
        state = feedViewModel.alertState,
        onDismiss = { feedViewModel.isAlertPresented = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedPostContent(
    post: FeedPost,
    comments: List<FeedComment>,
    viewModel: FeedPostViewModelProtocol,
    feedViewModel: FeedViewModelProtocol,
    navController: NavController,
) {
    val proxy = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isWritingCommentFocusState by remember { mutableStateOf(false) }
    var targetComment by remember { mutableStateOf<FeedComment?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    val translationState by viewModel.translationState.collectAsState()
    val summarizationState by viewModel.summarizationState.collectAsState()
    var translationTarget by remember { mutableStateOf(viewModel.defaultTranslationLanguage()) }

    val pullState = rememberPullToRefreshState()
    val topBarScrollBehavior = landscapeHideOnScrollBehavior()

    PullToRefreshHapticHandler(pullState, isRefreshing)

    Scaffold(
        topBar = {
            FeedPostNavigationBar(
                navController = navController,
                onDelete = { showDeleteConfirmation = true },
                onReport = { reason -> viewModel.reportPost(post.id, reason) },
                onTranslate = {
                    translationTarget = viewModel.defaultTranslationLanguage()
                    viewModel.translatePost(translationTarget)
                },
                onSummarize = { viewModel.summarizePost() },
                isMine = post.isAuthor,
                scrollBehavior = topBarScrollBehavior
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                InputBar(
                    viewModel = viewModel,
                    targetComment = targetComment,
                    isWritingCommentFocusState = isWritingCommentFocusState,
                    focusRequester = focusRequester,
                    onCommentUploaded = {
                        if (viewModel.text.isEmpty()) return@InputBar
                        scope.launch {
                            val uploaded = viewModel.submitComment(post.id, targetComment)
                            if (uploaded != null) {
                                post.commentCount += 1
                                targetComment = null
                                isWritingCommentFocusState = false
                                val index = comments.indexOfFirst { it.id == uploaded.id }
                                if (index != -1) proxy.animateScrollToItem(index)
                            }
                        }
                    }
                )
            }
        },
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen(
                "Feed Post",
                "is_author" to post.isAuthor,
                "has_comments" to (post.commentCount > 0)
            )
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    viewModel.fetchComments(postID = post.id, initial = false)
                    delay(500)
                    isRefreshing = false
                }
            },
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = proxy,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    FeedPostRow(
                        post = post,
                        viewModel = feedViewModel,
                        singleLine = false,
                        onPostDeleted = null,
                        onComment = {
                            targetComment = null
                            isWritingCommentFocusState = true
                        }
                    )
                }

                item {
                    CommentsSection(
                        commentCount = post.commentCount,
                        comments = comments,
                        viewModel = viewModel,
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
                            showDeleteConfirmation = false
                            scope.launch {
                                val success = feedViewModel.deletePost(post.id)
                                if (success) {
                                    navController.popBackStack()
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
                },
                containerColor = MaterialTheme.colorScheme.background
                
            )
        }

        if (translationState !is TranslationState.Idle) {
            PostTranslationSheet(
                state = translationState,
                targetLanguage = translationTarget,
                languages = viewModel.translationLanguages(),
                suggested = viewModel.suggestedTranslationLanguages(),
                onTargetChange = { code ->
                    translationTarget = code
                    viewModel.translatePost(code)
                },
                onRetry = { viewModel.translatePost(translationTarget) },
                onDownload = { viewModel.translatePost(translationTarget, allowDownload = true) },
                onDismiss = { viewModel.showOriginal() }
            )
        }

        if (summarizationState !is SummarizationState.Idle) {
            PostSummarizationSheet(
                state = summarizationState,
                onRetry = { viewModel.summarizePost() },
                onDismiss = { viewModel.hideSummary() }
            )
        }
    }
}

@Composable
private fun CommentsSection(
    commentCount: Int,
    comments: List<FeedComment>,
    viewModel: FeedPostViewModelProtocol,
    onReply: (FeedComment) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )

        Text(
            text = stringResource(R.string.the_number_of_comments, commentCount),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            fontWeight = FontWeight.Medium
        )

        comments.forEach { comment ->
            FeedCommentRow(
                comment = comment,
                isReply = false,
                onReply = { onReply(comment) },
                viewModel = viewModel
            )
            comment.replies.forEach { reply ->
                FeedCommentRow(
                    comment = reply,
                    isReply = true,
                    onReply = {},
                    viewModel = viewModel
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.lightGray0,
                modifier = Modifier.padding(vertical = 8.dp)
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
) {
    LaunchedEffect(isWritingCommentFocusState) {
        if (isWritingCommentFocusState) {
            focusRequester.requestFocus()
        }
    }

    var isFocused by remember { mutableStateOf(isWritingCommentFocusState) }
    val haptic = LocalHapticFeedback.current
    val rawName = targetComment?.authorName ?: ""
    val authorName = if (rawName.contains("Anonymous")) {
        rawName.replace("Anonymous", stringResource(R.string.anonymous))
    } else {
        rawName
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            if (isFocused) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
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

            Row(verticalAlignment = Alignment.Bottom) {
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
                                        style = MaterialTheme.typography.bodyMedium,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
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
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(45.dp)
                    ) {
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

@OptIn(ExperimentalMaterial3Api::class)
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

