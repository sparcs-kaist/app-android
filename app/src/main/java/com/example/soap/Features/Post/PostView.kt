package com.example.soap.Features.Post

import PostCommentCell
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostComment
import com.example.soap.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import com.example.soap.Features.NavigationBar.Animation.MoveToLeftFadeIn
import com.example.soap.Features.NavigationBar.Animation.MoveToLeftFadeOut
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Post.Components.DynamicHeightWebView
import com.example.soap.Features.Post.Components.PostBookmarkButton
import com.example.soap.Features.Post.Components.PostCommentButton
import com.example.soap.Features.Post.Components.PostNavigationBar
import com.example.soap.Features.Post.Components.PostShareButton
import com.example.soap.Features.Post.Components.PostVoteButton
import com.example.soap.R
import com.example.soap.Shared.Extensions.formattedString
import com.example.soap.Shared.Mocks.board
import com.example.soap.Shared.Views.ContentViews.UnavailableView
import com.example.soap.ui.theme.grayBB
import com.example.soap.ui.theme.lightGray0
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostView(
    viewModel: PostViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
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

    var summarisedContent by remember { mutableStateOf<String?>(null) }

    var showAlert by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }

    val post by viewModel.post.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPost()
    }

    Scaffold(
        topBar = {
            PostNavigationBar(
                boardGroup = board.group.name.localized(),
                navController = navController,
                onDelete = { showDeleteConfirmation = true },
                onReport = { type ->
                    scope.launch{ viewModel.report(type) }
                           },
                onTranslate = {
                    showTranslationView = true
                              //TODO-Translate
                    },
                isMine = post.isMine
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
                            val uploadedComment: AraPostComment = when {
                                commentOnEdit != null -> viewModel.editComment(commentID = commentOnEdit!!.id, content = comment)
                                targetComment != null -> viewModel.writeThreadedComment(commentID = targetComment!!.id, content = comment)
                                else -> viewModel.writeComment(content = comment)
                            }

                            targetComment = null
                            commentOnEdit = null
                            comment = ""
                            isWritingComment = false

                            uploadedComment.let { comment ->
                                scope.launch {
                                    proxy.scrollToItem(comment.id)
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("PostView", "Failed to upload comment", e)
                            showAlert = true
                            alertTitle = "Error"
                            alertMessage = "Failed to upload comment"
                        } finally {
                            isUploadingComment = false
                        }
                    }
                                  },
                profilePicture = { ProfilePicture(post, true) },
                placeholder = placeholder(viewModel, targetComment, commentOnEdit),
                focusRequester = focusRequester
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    viewModel.fetchPost()
                }
                isRefreshing = false
            }
        ) {
            LazyColumn(
                Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                state = proxy
            ) {
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
                        isWritingComment = true
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
                item {
                    Comments(
                        post = post
                    ) {
                        commentOnEdit = it.commentOnEdit
                        targetComment = it.targetComment
                        comment = it.comment
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
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
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("PostView", "Failed to delete post", e)
                                showAlert = true
                                alertTitle = "Error"
                                alertMessage = "Failed to delete post"
                            }
                        }

                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceContainer)
                )
                {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            title = { Text(text = "Delete Post", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this post?") }
        )
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                Button(onClick = { showAlert = false }) { Text("Okay") }
            },
            title = { Text(alertTitle) },
            text = { Text(alertMessage) }
        )
    }
}

@Composable
private fun Header(
    post: AraPost,
    onAuthorClick: () -> Unit
) {

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title(post),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement  = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = post.createdAt.formattedString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
            Text(
                text = "${post.views} views",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = if (post.author.username != stringResource(R.string.anonymous)) {
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
            if (post.author.username != stringResource(R.string.anonymous)) {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward_ios),
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
    post: AraPost
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PostVoteButton(
            myVote = post.myVote,
            votes = post.upVotes - post.downVotes,
            onUpVote = { scope.launch{ viewModel.upVote() } },
            onDownVote = { scope.launch{ viewModel.downVote() } }
        )
        PostCommentButton(commentCount = post.commentCount) { onCommentClick() }
        Spacer(Modifier.weight(1f))
        PostBookmarkButton()
        PostShareButton()
    }
}

@Composable
private fun Comments(
    post: AraPost,
    onCommentChange: (CommentUpdate) -> Unit
) {
    val repo: AraCommentRepositoryProtocol = hiltViewModel<PostViewModel>().araCommentRepository
    Column {
        if (post.comments.isEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0, modifier = Modifier.padding(vertical = 4.dp))

            UnavailableView(
                icon = painterResource(R.drawable.chat_bubble_outline),
                title = "No one has commented yet.",
                description = "Be the first one to share your thoughts."
            )
        } else {
            post.comments.forEach { commentItem ->
                PostCommentCell(
                    comment = commentItem,
                    isThreaded = false,
                    onComment = {
                        onCommentChange(
                            CommentUpdate(
                                targetComment = commentItem,
                                commentOnEdit = null,
                                comment = ""
                            )
                        )
                    },
                    onEdit = {
                        onCommentChange(
                            CommentUpdate(
                                targetComment = null,
                                commentOnEdit = commentItem,
                                comment = commentItem.content ?: ""
                            )
                        )
                    },
                    onDelete = {
                        post.commentCount -= 1
                               },
                    onTranslate = {},
                    araCommentRepository = repo
                )
                //Threads
                commentItem.comments.forEach { thread ->
                    PostCommentCell(
                        comment = thread,
                        isThreaded = true,
                        onComment = {
                            onCommentChange(
                                CommentUpdate(
                                    targetComment = thread,
                                    commentOnEdit = null,
                                    comment = ""
                                )
                            )
                        },
                        onEdit = {
                            onCommentChange(
                                CommentUpdate(
                                    targetComment = null,
                                    commentOnEdit = thread,
                                    comment = thread.content ?: ""
                                )
                            )
                        },
                        onDelete = { post.commentCount -= 1 },
                        onTranslate = {
                            //TODO - translate
                        },
                        araCommentRepository = repo
                    )
                }
            }
        }
    }
}

@Composable
fun InputBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    isWritingComment: Boolean,
    onWritingCommentChange: (Boolean) -> Unit,
    commentOnEdit: AraPostComment?,
    isUploadingComment: Boolean,
    onUploadComment: () -> Unit,
    profilePicture: @Composable () -> Unit,
    placeholder: String,
    focusRequester: FocusRequester
) {
    val scope = rememberCoroutineScope()
    val isWritingState = remember { mutableStateOf(isWritingComment) }

    val showProfile = (!isWritingState.value && comment.isEmpty())

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
                        text = "Editing...",
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
                        modifier = Modifier
                            .focusRequester(focusRequester),
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
            MoveToLeftFadeIn(!showProfile){
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
                            painter = painterResource(id = R.drawable.paperplane),
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
    post: AraPost,
    isMe: Boolean
){
    val profileUrl = if(isMe) post.myCommentProfile?.profile?.profilePictureURL else post.author.profile.profilePictureURL
    if (profileUrl != null) {
        AsyncImage(
            model = profileUrl.toString(),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
        )
    } else {
        Box(Modifier
            .size(28.dp)
            .background(MaterialTheme.colorScheme.grayBB, CircleShape))
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

        val postTitle = post.title ?: "Untitled"
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

fun placeholder(
    viewModel: PostViewModelProtocol,
    targetComment: AraPostComment?,
    commentOnEdit: AraPostComment?
): String {
    return when {
        targetComment != null -> "reply to ${targetComment.author.profile.nickname}"
        commentOnEdit != null -> commentOnEdit.content.orEmpty()
        else -> "reply as ${viewModel.post.value.myCommentProfile?.profile?.nickname ?: "anonymous"}"
    }
}

data class CommentUpdate(
    val targetComment: AraPostComment? = null,
    val commentOnEdit: AraPostComment? = null,
    val comment: String = ""
)


@Preview
@Composable
private fun PreviewPostView() {
    val vm = remember { MockPostViewModel() }
    val navController = rememberNavController()
    PostView(vm, navController = navController)
}