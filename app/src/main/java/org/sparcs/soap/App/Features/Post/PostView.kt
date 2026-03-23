package org.sparcs.soap.App.Features.Post

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Features.NavigationBar.Animation.MoveToLeftFadeIn
import org.sparcs.soap.App.Features.NavigationBar.Animation.MoveToLeftFadeOut
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.Post.Components.DynamicHeightWebView
import org.sparcs.soap.App.Features.Post.Components.PostBookmarkButton
import org.sparcs.soap.App.Features.Post.Components.PostCommentButton
import org.sparcs.soap.App.Features.Post.Components.PostCommentsSection
import org.sparcs.soap.App.Features.Post.Components.PostNavigationBar
import org.sparcs.soap.App.Features.Post.Components.PostShareButton
import org.sparcs.soap.App.Features.Post.Components.PostViewSkeleton
import org.sparcs.soap.App.Features.Post.Components.PostVoteButton
import org.sparcs.soap.App.Shared.Extensions.PullToRefreshHapticHandler
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Extensions.formattedString
import org.sparcs.soap.App.Shared.Extensions.postfixEuroRo
import org.sparcs.soap.App.Shared.Mocks.Ara.mock
import org.sparcs.soap.App.Shared.Mocks.Ara.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Ara.MockPostViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.R
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostView(
    viewModel: PostViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val post = viewModel.post.collectAsState().value
    val context = LocalContext.current
    val state = viewModel.state.collectAsState().value
    val pullState = rememberPullToRefreshState()

    val scope = rememberCoroutineScope()
    val proxy = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var htmlHeight by remember { mutableStateOf(0.dp) }
    var tappedURL by remember { mutableStateOf<Uri?>(null) }
    var comment by remember { mutableStateOf("") }
    var isWritingComment by remember { mutableStateOf(false) }
    var targetComment by remember { mutableStateOf<AraPostComment?>(null) }
    var commentOnEdit by remember { mutableStateOf<AraPostComment?>(null) }
    var isUploadingComment by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    var showTranslationView by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val summarisedContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchPost()
    }

    LaunchedEffect(tappedURL) {
        tappedURL?.let { uri ->
            try {
                val urlString = uri.toString()
                val finalUri =
                    if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                        Uri.parse("http://$urlString")
                    } else {
                        uri
                    }

                val intent = Intent(Intent.ACTION_VIEW, finalUri)
                context.startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e, "Failed to open URL: $tappedURL")
            } finally {
                tappedURL = null
            }
        }
    }

    PullToRefreshHapticHandler(pullState, isRefreshing)

    Scaffold(
        topBar = {
            PostNavigationBar(
                boardGroup = post?.board?.group?.name?.localized() ?: "",
                onClick = { navController.popBackStack() },
                onDelete = { showDeleteConfirmation = true },
                onReport = { type ->
                    scope.launch { viewModel.report(type) }
                },
                onTranslate = {
                    showTranslationView = true
                    //TODO-Translate
                },
                isMine = post?.isMine
            )
        },
        bottomBar = {
            InputBar(
                comment = comment,
                onCommentChange = { comment = it },
                isWritingComment = isWritingComment,
                onWritingCommentChange = {
                    isWritingComment = it
                    commentOnEdit = null
                    comment = ""
                },
                commentOnEdit = commentOnEdit,
                isUploadingComment = isUploadingComment,
                onUploadComment = {
                    scope.launch {
                        isUploadingComment = true
                        try {
                            val uploadedComment = when {
                                commentOnEdit != null -> viewModel.editComment(
                                    commentOnEdit!!.id,
                                    comment
                                )

                                targetComment != null -> viewModel.writeThreadedComment(
                                    targetComment!!.id,
                                    comment
                                )

                                else -> viewModel.writeComment(comment)
                            }

                            comment = ""; targetComment = null; commentOnEdit = null
                            keyboardController?.hide()

                            uploadedComment.let { comment ->
                                scope.launch {
                                    proxy.scrollToItem(comment.id)
                                }
                            }
                        } catch (e: Exception) {
                            viewModel.alertState = AlertState(
                                titleResId = R.string.unexpected_error_uploading_comment,
                                message = e.localizedMessage
                                    ?: context.getString(R.string.error_unknown_try_again)
                            )
                            viewModel.isAlertPresented = true
                        } finally {
                            isUploadingComment = false
                        }
                    }
                },
                profilePicture = { ProfilePicture(post, true) },
                placeholder = placeholder(viewModel, targetComment, commentOnEdit),
                focusRequester = focusRequester
            )
        },
        modifier = Modifier.analyticsScreen(
            "Ara Post",
            "is_author" to (post?.isMine ?: false),
            "has_comments" to ((post?.commentCount ?: 0) > 0)
        ),
    ) { innerPadding ->
        when (state) {
            is PostViewModel.ViewState.Loading -> {
                PostViewSkeleton()
            }

            is PostViewModel.ViewState.Error -> {
                ErrorView(
                    icon = Icons.Default.Warning,
                    message = state.message,
                    onRetry = { scope.launch { viewModel.fetchPost() } }
                )
            }

            is PostViewModel.ViewState.Loaded -> {
                if (post == null) {
                    PostViewSkeleton()
                    return@Scaffold
                }
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        scope.launch {
                            viewModel.fetchPost()
                            delay(500)
                            isRefreshing = false
                        }
                    },
                    state = pullState,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    LazyColumn(state = proxy) {
                        item {
                            Header(
                                post = post,
                                onAuthorClick = {
                                    val json = Uri.encode(Gson().toJson(post.author))
                                    navController.navigate(Channel.UserPostListView.name + "?author_json=$json")
                                }
                            )
                        }
                        item {
                            Content(
                                summarisedContent = summarisedContent,
                                htmlHeight = htmlHeight,
                                onHtmlHeightChange = { htmlHeight = it },
                                onLinkTapped = { tappedURL = Uri.parse(it) },
                                post = post
                            )
                        }
                        item {
                            Footer(viewModel, scope = scope, post = post) {
                                targetComment = null
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        }
                        item {
                            Comments(
                                post = post,
                                onCommentChange = { update ->
                                    targetComment = update.targetComment
                                    commentOnEdit = update.commentOnEdit
                                    comment = update.comment
                                },
                                viewModel = viewModel,
                                focusRequester = focusRequester,
                                keyboardController = keyboardController
                            )
                        }
                        item { Spacer(modifier = Modifier.height(64.dp)) }
                    }
                }
            }
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    viewModel.deletePost()
                                    showDeleteConfirmation = false
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("listNeedsRefresh", true)
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    viewModel.alertState = AlertState(
                                        titleResId = R.string.error,
                                        messageResId = R.string.unexpected_error_deleting_post
                                    )
                                    viewModel.isAlertPresented = true
                                    showDeleteConfirmation = false
                                }
                            }

                        },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceContainer)
                    )
                    {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.delete_post),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_post)) }
            )
        }
        if (viewModel.isAlertPresented) {
            AlertDialog(
                onDismissRequest = { viewModel.isAlertPresented = false },
                confirmButton = {
                    TextButton(onClick = { viewModel.isAlertPresented = false }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                title = {
                    viewModel.alertState?.titleResId?.let { Text(stringResource(it)) }
                },
                text = {
                    viewModel.alertState?.let { state ->
                        Text(
                            state.message ?: stringResource(
                                state.messageResId ?: R.string.unexpected_error
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun Header(
    post: AraPost,
    onAuthorClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title(post),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = post.createdAt.formattedString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
            Text(
                text = stringResource(R.string.views, post.views),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = if (post.author.username != "익명" && post.author.username != "Anonymous") {
                Modifier.clickable { onAuthorClick() }
            } else {
                Modifier
            }
        ) {
            ProfilePicture(post, false)
            Text(
                text = post.author.profile.nickname,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall
            )
            if (post.author.username != "익명" && post.author.username != "Anonymous") {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0)
    }
}

@Composable
private fun Content(
    summarisedContent: String?,
    htmlHeight: Dp,
    onHtmlHeightChange: (Dp) -> Unit,
    onLinkTapped: (String) -> Unit,
    post: AraPost,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        if (!summarisedContent.isNullOrEmpty()) {
//            SummarisationView(text = summarisedContent)
            Spacer(modifier = Modifier.height(8.dp))
        }
        val postContent = post.content
        if (!postContent.isNullOrEmpty()) {
            DynamicHeightWebView(
                htmlString = postContent,
                modifier = Modifier
                    .height(htmlHeight)
                    .fillMaxWidth(),
                onHeightChanged = { pxHeight ->
                    onHtmlHeightChange(pxHeight.dp)
                },
                onLinkTapped = onLinkTapped
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun Footer(
    viewModel: PostViewModelProtocol,
    scope: CoroutineScope,
    post: AraPost,
    onCommentClick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PostVoteButton(
            myVote = post.myVote,
            votes = post.upVotes - post.downVotes,
            onUpVote = { scope.launch { viewModel.upVote() } },
            onDownVote = { scope.launch { viewModel.downVote() } },
            enabled = post.isMine == false
        )
        PostCommentButton(commentCount = post.commentCount) { onCommentClick() }
        Spacer(Modifier.weight(1f))
        PostBookmarkButton(
            post.myScrap,
            onToggleBookmark = { scope.launch { viewModel.toggleBookmark() } })
        PostShareButton(url = Constants.araShareURL + post.id.toString(), context = context)
    }
}

@Composable
private fun Comments(
    post: AraPost,
    onCommentChange: (CommentUpdate) -> Unit,
    viewModel: PostViewModelProtocol,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .animateContentSize()
    ) {
        PostCommentsSection(
            comments = post.comments,
            onReply = { selectedComment ->
                onCommentChange(
                    CommentUpdate(
                        targetComment = selectedComment,
                        commentOnEdit = null,
                        comment = ""
                    )
                )
                focusRequester.requestFocus()
                keyboardController?.show()
            },
            onCommentDeleted = {
                post.commentCount -= 1
            },
            onEdit = { selectedComment ->
                onCommentChange(
                    CommentUpdate(
                        targetComment = null,
                        commentOnEdit = selectedComment,
                        comment = selectedComment.content ?: ""
                    )
                )
                focusRequester.requestFocus()
                keyboardController?.show()
            },
            onUpVote = { target ->
                scope.launch { viewModel.upVoteComment(target) }
            },
            onDownVote = { target ->
                scope.launch { viewModel.downVoteComment(target) }
            },
            onReport = { commentID, type ->
                scope.launch {
                    viewModel.reportComment(commentID, type)
                }
            },
            onDeleteComment = { target ->
                scope.launch { viewModel.deleteComment(target) }
            }
        )
    }
}

@Composable
private fun InputBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    isWritingComment: Boolean,
    onWritingCommentChange: (Boolean) -> Unit,
    commentOnEdit: AraPostComment?,
    isUploadingComment: Boolean,
    onUploadComment: () -> Unit,
    profilePicture: @Composable () -> Unit,
    placeholder: String,
    focusRequester: FocusRequester,
) {
    val scope = rememberCoroutineScope()
    val isWritingState by remember { mutableStateOf(isWritingComment) }

    val showProfile = (!isWritingState && comment.isEmpty())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // comment textfield
        Column(Modifier.weight(1f)) {
            if (commentOnEdit != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${stringResource(R.string.editing)}...",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {
                        onCommentChange("")
                        onWritingCommentChange(false)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                MoveToLeftFadeOut(showProfile) { profilePicture() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = comment,
                        onValueChange = {
                            onCommentChange(it)
                        },
                        modifier = Modifier.focusRequester(focusRequester),
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
                                if (comment.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        if (comment.isNotEmpty()) {
            MoveToLeftFadeIn(!showProfile) {
                Button(
                    onClick = {
                        scope.launch {
                            onUploadComment()
                        }
                    },
                    enabled = !isUploadingComment && comment.isNotEmpty()
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
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePicture(
    post: AraPost?,
    isMe: Boolean,
) {
    if (post == null) return
    val profileUrl =
        if (isMe) post.myCommentProfile?.profile?.profilePictureURL else post.author.profile.profilePictureURL
    if (profileUrl != null) {
        AsyncImage(
            model = profileUrl.toString(),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.grayBB, CircleShape)
        )
    }
}

@Composable
private fun title(post: AraPost): AnnotatedString {
    return buildAnnotatedString {
        post.topic?.name?.localized()?.let { topicName ->
            val topicText = "[$topicName] "
            append(topicText)
            addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                start = 0,
                end = topicText.length
            )
        }

        val postTitle = post.title ?: stringResource(R.string.untitled)
        val startIndex = length
        append(postTitle)
        addStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            start = startIndex,
            end = length
        )
    }
}

@Composable
fun placeholder(
    viewModel: PostViewModelProtocol,
    targetComment: AraPostComment?,
    commentOnEdit: AraPostComment?,
): String {
    return when {
        targetComment != null -> stringResource(
            R.string.write_a_reply_to,
            targetComment.author.profile.nickname
        )

        commentOnEdit != null -> commentOnEdit.content.orEmpty()
        else -> {
            val anonymousName = stringResource(R.string.anonymous)
            stringResource(
                R.string.reply_as,
                viewModel.post.value?.myCommentProfile?.profile?.nickname?.postfixEuroRo()
                    ?: anonymousName.postfixEuroRo()
            )
        }
    }
}

data class CommentUpdate(
    val targetComment: AraPostComment? = null,
    val commentOnEdit: AraPostComment? = null,
    val comment: String = "",
)

/* ____________________________________________________________________*/
@Preview(name = "Loaded", showBackground = true)
@Composable
private fun LoadedPreview() {
    val mockViewModel =
        remember {
            MockPostViewModel(
                initialState = PostViewModel.ViewState.Loaded,
                post = AraPost.mock()
            )
        }
    Theme {
        PostView(viewModel = mockViewModel, navController = rememberNavController())
    }
}

@Preview(name = "Loading Content", showBackground = true)
@Composable
private fun LoadingPreview() {
    val mockViewModel =
        remember {
            MockPostViewModel(
                initialState = PostViewModel.ViewState.Loading,
                post = AraPost.mockList()[0]
            )
        }
    Theme {
        PostView(viewModel = mockViewModel, navController = rememberNavController())
    }
}

@Preview(name = "No Comments", showBackground = true)
@Composable
private fun NoCommentsPreview() {
    val mockViewModel =
        remember {
            MockPostViewModel(
                initialState = PostViewModel.ViewState.Loaded,
                post = AraPost.mockList()[1]
            )
        }
    Theme {
        PostView(viewModel = mockViewModel, navController = rememberNavController())
    }
}