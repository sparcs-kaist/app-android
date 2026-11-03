package org.sparcs.soap.app.features.post

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.DeepLink
import org.sparcs.soap.app.domain.enums.DeepLinkEventBus
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.post.components.BlockAuthorDialog
import org.sparcs.soap.app.features.post.components.CommentSkeleton
import org.sparcs.soap.app.features.post.components.PostColumn
import org.sparcs.soap.app.features.post.components.PostInputBar
import org.sparcs.soap.app.features.post.components.PostMenuActions
import org.sparcs.soap.app.features.post.components.PostMenuState
import org.sparcs.soap.app.features.post.components.PostNavigationBar
import org.sparcs.soap.app.features.post.components.PostPanes
import org.sparcs.soap.app.features.post.components.PostSectionActions
import org.sparcs.soap.app.features.post.components.PostSectionState
import org.sparcs.soap.app.features.post.components.commentIndex
import org.sparcs.soap.app.features.post.components.postCommentsSection
import org.sparcs.soap.app.features.post.components.postSection
import org.sparcs.soap.app.features.post.components.usesPostPanes
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.postfixEuroRo
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.PostToolsTipRule
import org.sparcs.soap.app.shared.tips.TipEvent
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostViewModel
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.shared.views.contentViews.PostTranslationSheet
import org.sparcs.soap.app.theme.ui.Theme
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostView(
    viewModel: PostViewModelProtocol = hiltViewModel<PostViewModel>(),
    navController: NavController,
) {
    val post by viewModel.post.collectAsState()
    val state by viewModel.state.collectAsState()
    val body by viewModel.contentBody.collectAsState()
    val translationState by viewModel.postTranslationState.collectAsState()
    val translationTargets by viewModel.translationTargets.collectAsState()
    val summaryPhase by viewModel.summaryPhase.collectAsState()
    val canSummarise by viewModel.canSummarise.collectAsState()
    val canEdit by viewModel.canEdit.collectAsState()
    val commentTranslations by viewModel.commentTranslations.collectAsState()

    val context = LocalContext.current
    val tipStore = LocalTipStore.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val pullState = rememberPullToRefreshState()
    val postListState = rememberLazyListState()
    val commentListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarScrollBehavior = topAppBarScrollBehavior()
    val usesPanes = usesPostPanes()
    val commentsListState = if (usesPanes) commentListState else postListState

    var commentText by remember { mutableStateOf("") }
    var targetComment by remember { mutableStateOf<AraPostComment?>(null) }
    var commentOnEdit by remember { mutableStateOf<AraPostComment?>(null) }
    var isInputFocused by remember { mutableStateOf(false) }
    var isUploadingComment by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var authorToBlock by remember { mutableStateOf<AraPostAuthor?>(null) }
    var translatingComment by remember { mutableStateOf<AraPostComment?>(null) }
    var commentTranslationTarget by remember { mutableStateOf(viewModel.defaultTranslationLanguage()) }

    val showsPostToolsTip = remember(body) { body?.let { PostToolsTipRule.applies(it.plainText) } == true }
    val suggestedTargets = remember(translationTargets) {
        PostTranslator.suggestedTargets(viewModel.suggestedTranslationLanguages(), translationTargets)
    }
    val replyTargetId = targetComment?.id?.takeIf { isInputFocused }

    LaunchedEffect(viewModel.postId) {
        tipStore?.donate(TipEvent.PostOpened)
        viewModel.fetchPost()
    }

    LifecycleResumeEffect(navController) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        if (handle?.get<Boolean>("listNeedsRefresh") == true) {
            handle["listNeedsRefresh"] = false
            viewModel.fetchPost()
        }
        onPauseOrDispose {}
    }

    LaunchedEffect(viewModel.isAlertPresented) {
        if (viewModel.isAlertPresented) isUploadingComment = false
    }

    LaunchedEffect(replyTargetId, usesPanes) {
        val id = replyTargetId ?: return@LaunchedEffect
        val comments = post?.comments ?: return@LaunchedEffect
        withFrameNanos {}
        val index = commentIndex(comments, id, commentsListState.layoutInfo.totalItemsCount, reservesReplySpace = true)
            ?: return@LaunchedEffect
        commentsListState.animateScrollToItem(index)
    }

    PullToRefreshHapticHandler(pullState, isRefreshing)

    fun focusInput() {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun refresh() {
        isRefreshing = true
        scope.launch {
            viewModel.fetchPost()
            delay(REFRESH_INDICATOR_MS)
            isRefreshing = false
        }
    }

    fun openLink(url: String) {
        val uri = url.toUri()
        val deepLink = DeepLink.fromUri(uri)
        if (deepLink != null) {
            scope.launch { DeepLinkEventBus.post(deepLink) }
            return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Timber.e(e, "Failed to open URL: $url")
        }
    }

    fun uploadComment() {
        if (isUploadingComment) return
        isUploadingComment = true
        scope.launch {
            try {
                val editing = commentOnEdit
                val replyingTo = targetComment
                val success = when {
                    editing != null -> viewModel.editComment(editing.id, commentText) != null
                    replyingTo != null -> viewModel.writeThreadedComment(replyingTo.id, commentText) != null
                    else -> viewModel.writeComment(commentText) != null
                }
                if (success) {
                    commentText = ""
                    targetComment = null
                    commentOnEdit = null
                    keyboardController?.hide()
                    commentsListState.animateScrollToItem(commentsListState.layoutInfo.totalItemsCount)
                }
            } finally {
                isUploadingComment = false
            }
        }
    }

    val menuState = PostMenuState(
        isMine = post?.isMine,
        canEdit = canEdit,
        isAuthorBlocked = post?.author?.takeIf { post?.isMine == false && it.blockableUserId != null }?.isBlocked,
        canSummarise = canSummarise,
        isSummaryShown = summaryPhase?.isShown == true,
        translationTargets = translationTargets,
        suggestedTargets = suggestedTargets,
        translationTarget = translationState.targetLanguage,
    )
    val menuActions = PostMenuActions(
        onEdit = {
            val current = post ?: return@PostMenuActions
            val board = current.board ?: return@PostMenuActions
            val boardJson = Uri.encode(Gson().toJson(board))
            val postJson = Uri.encode(Gson().toJson(current.copy(comments = mutableListOf())))
            navController.navigate(Channel.PostCompose.name + "?board_json=$boardJson&post_json=$postJson")
        },
        onDelete = { showDeleteConfirmation = true },
        onReport = viewModel::report,
        onSetAuthorBlocked = { blocked ->
            val author = post?.author ?: return@PostMenuActions
            if (blocked) authorToBlock = author else viewModel.setAuthorBlocked(author, blocked = false)
        },
        onTranslate = { target ->
            tipStore?.invalidate(BuddyTip.PostTools)
            viewModel.translatePost(target)
        },
        onShowOriginal = viewModel::showPostOriginal,
        onSummarise = {
            tipStore?.invalidate(BuddyTip.PostTools)
            viewModel.summarise()
        },
    )

    val sectionState = PostSectionState(
        post = post,
        body = body,
        translationState = translationState,
        summaryPhase = summaryPhase,
        showsToolsTip = showsPostToolsTip,
    )
    val sectionActions = PostSectionActions(
        onAuthorClick = {
            val json = Uri.encode(Gson().toJson(post?.author ?: return@PostSectionActions))
            navController.navigate(Channel.UserPostListView.name + "?author_json=$json")
        },
        onLinkTapped = ::openLink,
        onShowOriginal = viewModel::showPostOriginal,
        onDownloadTranslation = {
            translationState.targetLanguage?.let { viewModel.translatePost(it, allowDownload = true) }
        },
        onRetrySummary = viewModel::summarise,
        onDismissSummary = viewModel::dismissSummary,
        onUpVote = viewModel::upVote,
        onDownVote = viewModel::downVote,
        onToggleBookmark = viewModel::toggleBookmark,
        onCommentClick = {
            targetComment = null
            focusInput()
        },
    )

    val comments: LazyListScope.() -> Unit = {
        commentsSection(
            post = post.takeIf { state != PostViewModel.ViewState.Loading },
            viewModel = viewModel,
            showsTopDivider = !usesPanes,
            reservesReplySpace = replyTargetId != null,
            onReply = { comment ->
                if (commentOnEdit != null) {
                    commentOnEdit = null
                    commentText = ""
                }
                targetComment = comment
                focusInput()
            },
            onEdit = { comment ->
                targetComment = null
                commentOnEdit = comment
                commentText = comment.content.orEmpty()
                focusInput()
            },
            onTranslate = { comment ->
                translatingComment = comment
                commentTranslationTarget = viewModel.defaultTranslationLanguage()
                viewModel.translateComment(comment.id, comment.content.orEmpty(), commentTranslationTarget)
            },
            onSetAuthorBlocked = viewModel::setAuthorBlocked,
        )
    }

    val inputBar: @Composable () -> Unit = {
        PostInputBar(
            comment = commentText,
            onCommentChange = { commentText = it },
            onFocusChange = { focused ->
                isInputFocused = focused
                if (!focused && commentText.isEmpty()) targetComment = null
            },
            isEditing = commentOnEdit != null,
            onCancelEdit = {
                commentOnEdit = null
                commentText = ""
            },
            targetComment = targetComment,
            onCancelReply = { targetComment = null },
            isUploadingComment = isUploadingComment,
            onUploadComment = ::uploadComment,
            profilePictureUrl = post?.myCommentProfile?.profile?.profilePictureURL,
            placeholder = placeholder(post, targetComment, commentOnEdit),
            focusRequester = focusRequester,
        )
    }

    Scaffold(
        topBar = {
            PostNavigationBar(
                boardGroup = post?.board?.group?.name?.localized().orEmpty(),
                onClick = { navController.popBackStack() },
                menuState = menuState,
                menuActions = menuActions,
                scrollBehavior = topBarScrollBehavior.takeUnless { usesPanes },
            )
        },
        bottomBar = { if (!usesPanes) inputBar() },
        modifier = Modifier
            .then(if (usesPanes) Modifier else Modifier.hideTopBarOnScroll(topBarScrollBehavior))
            .analyticsScreen(
                "Ara Post",
                "is_author" to (post?.isMine ?: false),
                "has_comments" to ((post?.commentCount ?: 0) > 0)
            ),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val currentState = state
        if (currentState is PostViewModel.ViewState.Error) {
            ErrorView(error = currentState.error, onRetry = ::refresh)
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = ::refresh,
                state = pullState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (usesPanes) {
                    PostPanes(
                        post = { PostColumn(postListState) { postSection(sectionState, sectionActions, snackbarHostState) } },
                        comments = {
                            PostColumn(commentListState, Modifier.weight(1f), content = comments)
                            inputBar()
                        },
                    )
                } else {
                    PostColumn(postListState) {
                        postSection(sectionState, sectionActions, snackbarHostState)
                        comments()
                    }
                }
            }
        }

        authorToBlock?.let { author ->
            BlockAuthorDialog(
                nickname = author.profile.nickname,
                onConfirm = {
                    authorToBlock = null
                    viewModel.setAuthorBlocked(author, blocked = true)
                },
                onDismiss = { authorToBlock = null }
            )
        }

        if (showDeleteConfirmation) {
            PostDeleteDialog(
                onDismiss = { showDeleteConfirmation = false },
                onConfirm = {
                    showDeleteConfirmation = false
                    scope.launch {
                        if (viewModel.deletePost()) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("listNeedsRefresh", true)
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        translatingComment?.let { comment ->
            val content = comment.content.orEmpty()
            PostTranslationSheet(
                state = commentTranslations[comment.id.toString()] ?: TranslationState.Idle,
                targetLanguage = commentTranslationTarget,
                languages = viewModel.translationLanguages(),
                suggested = viewModel.suggestedTranslationLanguages(),
                onTargetChange = { code ->
                    commentTranslationTarget = code
                    viewModel.translateComment(comment.id, content, code)
                },
                onRetry = { viewModel.translateComment(comment.id, content, commentTranslationTarget) },
                onDownload = { viewModel.translateComment(comment.id, content, commentTranslationTarget, allowDownload = true) },
                onDismiss = {
                    viewModel.showCommentOriginal(comment.id)
                    translatingComment = null
                }
            )
        }

        GlobalAlertDialog(
            isPresented = viewModel.isAlertPresented,
            state = viewModel.alertState,
            onDismiss = { viewModel.isAlertPresented = false }
        )
    }
}

private fun LazyListScope.commentsSection(
    post: AraPost?,
    viewModel: PostViewModelProtocol,
    showsTopDivider: Boolean,
    reservesReplySpace: Boolean,
    onReply: (AraPostComment) -> Unit,
    onEdit: (AraPostComment) -> Unit,
    onTranslate: (AraPostComment) -> Unit,
    onSetAuthorBlocked: (AraPostAuthor, Boolean) -> Unit,
) {
    if (post == null) {
        items(COMMENT_SKELETON_COUNT) { CommentSkeleton() }
        return
    }
    postCommentsSection(
        comments = post.comments,
        onReply = onReply,
        onCommentDeleted = { post.commentCount -= 1 },
        onEdit = onEdit,
        onUpVote = viewModel::upVoteComment,
        onDownVote = viewModel::downVoteComment,
        onReport = viewModel::reportComment,
        onDeleteComment = viewModel::deleteComment,
        onTranslateComment = onTranslate,
        onSetAuthorBlocked = onSetAuthorBlocked,
        showsTopDivider = showsTopDivider,
        reservesReplySpace = reservesReplySpace,
    )
}

@Composable
private fun PostDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_post)) },
        text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_post)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun placeholder(
    post: AraPost?,
    targetComment: AraPostComment?,
    commentOnEdit: AraPostComment?,
): String = when {
    targetComment != null -> stringResource(R.string.write_a_reply_to, targetComment.author.profile.nickname)
    commentOnEdit != null -> commentOnEdit.content.orEmpty()
    else -> stringResource(
        R.string.reply_as,
        post?.myCommentProfile?.profile?.nickname?.postfixEuroRo()
            ?: stringResource(R.string.anonymous).postfixEuroRo()
    )
}

private const val REFRESH_INDICATOR_MS = 500L
private const val COMMENT_SKELETON_COUNT = 2

@Composable
private fun PostPreview(state: PostViewModel.ViewState, post: AraPost) {
    val mockViewModel = remember { MockPostViewModel(initialState = state, post = post) }
    Theme {
        PostView(viewModel = mockViewModel, navController = rememberNavController())
    }
}

@Preview(name = "Loaded", showBackground = true)
@Composable
private fun LoadedPreview() = PostPreview(PostViewModel.ViewState.Loaded, AraPost.mock())

@Preview(name = "Loaded Wide", showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun LoadedWidePreview() = PostPreview(PostViewModel.ViewState.Loaded, AraPost.mock())

@Preview(name = "Loading Content", showBackground = true)
@Composable
private fun LoadingPreview() = PostPreview(PostViewModel.ViewState.Loading, AraPost.mockList()[0])

@Preview(name = "No Comments", showBackground = true)
@Composable
private fun NoCommentsPreview() = PostPreview(PostViewModel.ViewState.Loaded, AraPost.mockList()[1])
