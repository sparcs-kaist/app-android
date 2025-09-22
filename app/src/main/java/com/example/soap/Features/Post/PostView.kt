package com.example.soap.Features.Post

import PostCommentCell
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraPostAuthor
import com.example.soap.Domain.Models.Ara.AraPostComment
import com.example.soap.Domain.Repositories.Ara.AraCommentRepositoryProtocol
import com.example.soap.Features.Post.Components.DynamicHeightWebView
import com.example.soap.Features.Post.Components.PostBookmarkButton
import com.example.soap.Features.Post.Components.PostCommentButton
import com.example.soap.Features.Post.Components.PostNavigationBar
import com.example.soap.Features.Post.Components.PostShareButton
import com.example.soap.Features.Post.Components.PostVoteButton
import com.example.soap.R
import com.example.soap.Shared.Extensions.formattedString
import com.example.soap.ui.theme.grayBB
import com.example.soap.ui.theme.lightGray0
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PostView(
    viewModel: PostViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var htmlHeight by remember { mutableStateOf(0.dp) }
    var tappedURL by remember { mutableStateOf<Uri?>(null) }

    var comment by remember { mutableStateOf("") }
    var isWritingComment by remember { mutableStateOf(false) }
    var targetComment by remember { mutableStateOf<AraPostComment?>(null) }
    var commentOnEdit by remember { mutableStateOf<AraPostComment?>(null) }
    var isUploadingComment by remember { mutableStateOf(false) }

    var selectedAuthor by remember { mutableStateOf<AraPostAuthor?>(null) }

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
            PostNavigationBar(navController = navController)
        }
    ){innerPadding ->
        LazyColumn (Modifier
            .padding(innerPadding)
            .padding(16.dp)) {
            item{
                Header(
                    viewModel = viewModel,
                    targetComment = targetComment,
                    scope = scope,
                    post = post,
                    selectedAuthor = selectedAuthor
                ) { selectedAuthor = it }
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
                }
            }
            item {
                Comments(
                    viewModel = viewModel,
                    scope = scope,
                    commentOnEdit = commentOnEdit,
                    targetComment = targetComment,
                    comment = comment,
                    isWritingComment = isWritingComment,
                    post = post
                ) {
                    commentOnEdit = it.commentOnEdit
                    targetComment = it.targetComment
                    comment = it.comment
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        LaunchedEffect(showDeleteConfirmation) {
            try {
                viewModel.deletePost()
            } catch (e: Exception) {
                showAlert(title = "Error", message = "Failed to delete a post. Please try again later.")
            }
            showDeleteConfirmation = false
        }
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
    viewModel: PostViewModelProtocol,
    scope: CoroutineScope,
    post: AraPost,
    targetComment: AraPostComment?,
    selectedAuthor: AraPostAuthor?,
    onAuthorClick: (AraPostAuthor) -> Unit
) {

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title(viewModel, post),
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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val profileUrl = post.author.profile.profilePictureURL
            if (profileUrl != null) {
                AsyncImage(
                    model = profileUrl,
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
            Text(
                text = post.author.profile.nickname,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall
            )
            if (post.author.username != "anonymous") {
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
    viewModel: PostViewModelProtocol,
    scope: CoroutineScope,
    commentOnEdit: AraPostComment?,
    targetComment: AraPostComment?,
    comment: String,
    isWritingComment: Boolean,
    post: AraPost,
    onCommentChange: (CommentUpdate) -> Unit
) {
    val repo: AraCommentRepositoryProtocol = hiltViewModel<PostViewModel>().araCommentRepository

    Column {
        post.comments.forEach { commentItem ->
            PostCommentCell(
                comment = commentItem,
                isThreaded = false,
                onComment = { onCommentChange(CommentUpdate(targetComment = commentItem, commentOnEdit = null, comment = "")) },
                onEdit = {
                    onCommentChange(CommentUpdate(targetComment = null, commentOnEdit = commentItem, comment = commentItem.content ?: "")) },
                onDelete = { post.commentCount -= 1 },
                onTranslate = {},
                araCommentRepository = repo
            )
            //Threads
            commentItem.comments.forEach { thread ->
                PostCommentCell(
                    comment = thread,
                    isThreaded = true,
                    onComment = { onCommentChange(CommentUpdate(targetComment = thread, commentOnEdit = null, comment = "")) },
                    onEdit = { onCommentChange(CommentUpdate(targetComment = null, commentOnEdit = thread, comment = thread.content ?: "")) },
                    onDelete = { post.commentCount -= 1 },
                    onTranslate = {},
                    araCommentRepository = repo
                )
            }
        }
    }
}

private fun title(viewModel: PostViewModelProtocol, post: AraPost): String {
    val topicName = post.topic?.name?.localized()
    return (topicName?.let { "[$it] " } ?: "") + (post.title ?: "Untitled")
}

private fun showAlert(title: String, message: String) {
    // Compose에서 Alert 상태로 관리
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