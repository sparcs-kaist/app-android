package org.sparcs.soap.app.features.feedPost

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.features.feed.FeedViewModel
import org.sparcs.soap.app.features.feed.FeedViewModelProtocol
import org.sparcs.soap.app.features.feed.components.FeedPostRow
import org.sparcs.soap.app.features.feedPost.components.FeedCommentReplyPreview
import org.sparcs.soap.app.features.feedPost.components.FeedCommentRow
import org.sparcs.soap.app.features.feedPost.components.FeedPostNavigationBar
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
import org.sparcs.soap.app.shared.views.contentViews.PostTranslationSheet
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
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
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val missingCommentMessage = stringResource(R.string.feed_notification_comment_missing)
    val commentItems = comments.flatMap { listOf(it) + it.replies }
    var requestedCommentID by rememberSaveable(post.id, viewModel.initialCommentID) {
        mutableStateOf(viewModel.initialCommentID)
    }
    var highlightedCommentID by rememberSaveable(post.id) { mutableStateOf<String?>(null) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var targetComment by remember { mutableStateOf<FeedComment?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    val translationState by viewModel.translationState.collectAsState()
    val summarizationState by viewModel.summarizationState.collectAsState()
    var translationTarget by remember { mutableStateOf(viewModel.defaultTranslationLanguage()) }

    val pullState = rememberPullToRefreshState()
    val topBarScrollBehavior = landscapeHideOnScrollBehavior()

    PullToRefreshHapticHandler(pullState, isRefreshing)

    LaunchedEffect(requestedCommentID, commentItems.map { it.id }, viewModel.isLoadingComments) {
        val id = requestedCommentID ?: return@LaunchedEffect
        if (viewModel.isLoadingComments) return@LaunchedEffect
        val index = commentItems.indexOfFirst { it.id == id }
        if (index < 0) {
            requestedCommentID = null
            snackbarHostState.showSnackbar(missingCommentMessage)
            return@LaunchedEffect
        }
        snapshotFlow { proxy.layoutInfo.totalItemsCount }.first { it > index + 2 }
        proxy.animateScrollToItem(index + 2)
        highlightedCommentID = id
        requestedCommentID = null
    }
    LaunchedEffect(highlightedCommentID) {
        if (highlightedCommentID != null) {
            delay(4000)
            highlightedCommentID = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    focusRequester = focusRequester,
                    onCancelReply = { targetComment = null },
                    onCommentUploaded = {
                        if (viewModel.text.isEmpty() || viewModel.isSubmittingComment) return@InputBar
                        scope.launch {
                            val uploaded = viewModel.submitComment(post.id, targetComment)
                            if (uploaded != null) {
                                post.commentCount += 1
                                targetComment = null
                                focusManager.clearFocus()
                                requestedCommentID = uploaded.id
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
                .consumeWindowInsets(innerPadding)
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
                            focusRequester.requestFocus()
                        },
                        summarizationState = summarizationState,
                        onRetrySummarize = { viewModel.summarizePost() },
                        onDismissSummarize = { viewModel.hideSummary() }
                    )
                }

                item(key = "comments-header") {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text(
                            stringResource(R.string.the_number_of_comments, post.commentCount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                if (viewModel.isLoadingComments) {
                    item(key = "comments-loading") {
                        CircularProgressIndicator(Modifier.padding(24.dp))
                    }
                } else if (commentItems.isEmpty()) {
                    item(key = "comments-empty") {
                        UnavailableView(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            title = stringResource(R.string.no_one_has_commented_yet),
                            description = stringResource(R.string.be_the_first_one_to_share_your_thoughts),
                        )
                    }
                } else {
                    itemsIndexed(commentItems, key = { _, comment -> "comment-${comment.id}" }) { index, comment ->
                        Column(
                            Modifier.fillMaxWidth()
                                .background(if (highlightedCommentID == comment.id)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.background)
                                .padding(horizontal = 20.dp)
                        ) {
                            FeedCommentRow(
                                comment = comment,
                                isReply = comment.parentCommentID != null,
                                onReply = {
                                    if (comment.parentCommentID == null) {
                                        targetComment = comment
                                        focusRequester.requestFocus()
                                    }
                                },
                                viewModel = viewModel,
                            )
                            if (commentItems.getOrNull(index + 1)?.parentCommentID == null) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.lightGray0,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }
                    }
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
    }
}

@Composable
private fun InputBar(
    viewModel: FeedPostViewModelProtocol,
    targetComment: FeedComment?,
    onCommentUploaded: () -> Unit,
    focusRequester: FocusRequester,
    onCancelReply: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var isFocused by remember { mutableStateOf(false) }
    val rawName = targetComment?.authorName ?: ""
    val authorName = if (rawName.contains("Anonymous")) {
        rawName.replace("Anonymous", stringResource(R.string.anonymous))
    } else {
        rawName
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp),
    ) {
        AnimatedVisibility(targetComment != null) {
            targetComment?.let { FeedCommentReplyPreview(it, onCancelReply) }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
        ) {
            BasicTextField(
                value = viewModel.text,
                onValueChange = { viewModel.text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                maxLines = 6,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (viewModel.text.isEmpty()) {
                            Text(
                                text = if (targetComment != null)
                                    stringResource(R.string.write_a_reply_to, authorName)
                                else stringResource(R.string.write_a_comment),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                }
            )
            val showBottomRow = isFocused || viewModel.text.isNotEmpty()
            AnimatedVisibility(visible = showBottomRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .toggleable(
                                value = viewModel.isAnonymous,
                                role = Role.Checkbox,
                                onValueChange = {
                                    haptic.toggle(it)
                                    viewModel.isAnonymous = it
                                },
                            )
                            .padding(horizontal = 8.dp),
                    ) {
                        Checkbox(
                            checked = viewModel.isAnonymous,
                            onCheckedChange = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.anonymous),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onCommentUploaded,
                        enabled = viewModel.text.isNotEmpty() && !viewModel.isSubmittingComment,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (viewModel.isSubmittingComment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_send),
                                modifier = Modifier.size(20.dp),
                                contentDescription = stringResource(R.string.send)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Comment composer", widthDp = 360)
@Preview(showBackground = true, name = "Comment composer dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InputBarPreview() {
    val viewModel = remember {
        MockFeedPostViewModel(FeedPostViewModel.ViewState.Loaded(FeedPost.mock())).apply {
            isAnonymous = true
        }
    }
    Theme { InputBar(viewModel, null, {}, remember { FocusRequester() }, {}) }
}

@Preview(showBackground = true, name = "Reply composer - multiline", widthDp = 320)
@Composable
private fun ReplyInputBarPreview() {
    val viewModel = remember {
        MockFeedPostViewModel(FeedPostViewModel.ViewState.Loaded(FeedPost.mock())).apply {
            text = "? ?? ?? ??? ?????.\n?? ?? ???? ?? ??? ??? ?????."
        }
    }
    Theme { InputBar(viewModel, FeedComment.mockList().first(), {}, remember { FocusRequester() }, {}) }
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
