package com.sparcs.soap.Features.Feed.Components

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sparcs.soap.Domain.Enums.Feed.FeedVoteType
import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Domain.Models.Feed.FeedPost
import com.sparcs.soap.Features.Feed.FeedViewModelProtocol
import com.sparcs.soap.Features.Post.Components.PostCommentButton
import com.sparcs.soap.Features.Post.Components.PostShareButton
import com.sparcs.soap.Features.Post.Components.PostVoteButton
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Extensions.noRippleClickable
import com.sparcs.soap.Shared.Extensions.relativeTimeString
import com.sparcs.soap.Shared.Extensions.timeAgoDisplay
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import com.sparcs.soap.ui.theme.lightGray0
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FeedPostRow(
    post: FeedPost,
    viewModel: FeedViewModelProtocol,
    onPostDeleted: (String) -> Unit,
    onComment: () -> Unit, //post 또는 comment click
    singleLine: Boolean,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier
        .fillMaxWidth()
        .noRippleClickable { onComment() }
    ) {
        Header(
            post,
            onPostDeleted,
            showDeleteConfirmation,
        ) { showDeleteConfirmation = it }
        Content(post, singleLine, onComment)
        Footer(post, viewModel, onComment, onPostDeleted, !singleLine, coroutineScope)
    }
}

@Composable
fun ProfileImage(post: FeedPost) {
    if (post.profileImageURL != null) {
        AsyncImage(
            model = post.profileImageURL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.lightGray0)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text("😀", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun Header(
    post: FeedPost,
    onPostDeleted: ((String) -> Unit)?,
    showDeleteConfirmation: Boolean,
    setShowDelete: (Boolean) -> Unit,
) {
    val authorName =
        if (post.authorName == "Anonymous") stringResource(R.string.anonymous) else post.authorName

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        ProfileImage(post)
        Spacer(Modifier.width(8.dp))
        Text(authorName, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        if(post.isKaistIP){
            Icon(
                painter = painterResource(R.drawable.checkmark_seal_fill),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(8.dp))
        }

        Text(// onPostDeleted == null here means FeedPostRow is in the FeedPostView.
            text = if (onPostDeleted != null) post.createdAt.timeAgoDisplay() else post.createdAt.relativeTimeString(),
            color = MaterialTheme.colorScheme.grayBB,
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { setShowDelete(false) },
            confirmButton = {
                TextButton(onClick = {
                    onPostDeleted?.invoke(post.id)
                    setShowDelete(false)
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { setShowDelete(false) }) { Text(stringResource(R.string.cancel)) }
            },
            title = { Text(stringResource(R.string.delete_post)) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_post)) }
        )
    }
}

@Composable
fun Content(
    post: FeedPost,
    singleLine: Boolean,
    onComment: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }
    var hasMeasured by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val context = LocalContext.current
    val moreColor = MaterialTheme.colorScheme.grayBB
    val moreText = stringResource(R.string.more)
    val linkColor = MaterialTheme.colorScheme.primary

    val displayText = remember(post.content, expanded, isOverflowing, textLayoutResult) {
        val baseText = if (expanded || !isOverflowing) {
            post.content
        } else {
            val visibleEnd =
                textLayoutResult?.getLineEnd(0, visibleEnd = true) ?: post.content.length
            post.content.substring(0, visibleEnd.coerceAtMost(post.content.length)).trimEnd()
        }

        buildAnnotatedString {
            val regex = "(https?://[\\w./?=&%-]+)".toRegex()
            var lastIndex = 0

            regex.findAll(baseText).forEach { match ->
                append(baseText.substring(lastIndex, match.range.first))
                val url = match.value
                pushStringAnnotation("URL", url)
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(url)
                }
                pop()
                lastIndex = match.range.last + 1
            }

            if (lastIndex < baseText.length) {
                append(baseText.substring(lastIndex))
            }

            if (!expanded && isOverflowing) {
                pushStringAnnotation("MORE", "expand")
                append("… ")
                withStyle(
                    SpanStyle(
                        color = moreColor,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(moreText)
                }
                pop()
            }
        }
    }

    ClickableText(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        maxLines = if (singleLine && !expanded) 2 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layoutResult ->
            if (!hasMeasured && !expanded) {
                hasMeasured = true
                isOverflowing = layoutResult.hasVisualOverflow
                textLayoutResult = layoutResult
            }
        },
        onClick = { offset ->
            displayText.getStringAnnotations("URL", offset, offset)
                .firstOrNull()
                ?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                    context.startActivity(intent)
                    return@ClickableText
                }

            displayText.getStringAnnotations("MORE", offset, offset)
                .firstOrNull()
                ?.let {
                    if (!expanded && isOverflowing) expanded = true
                    return@ClickableText
                }

            onComment()
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    if (post.images.isNotEmpty()) {
        PostImagesStrip(images = post.images, onComment)
    }
}

@Composable
fun Footer(
    post: FeedPost,
    viewModel: FeedViewModelProtocol,
    onComment: () -> Unit,
    onPostDeleted: ((String) -> Unit)?,
    isDetailedView: Boolean,
    coroutineScope: CoroutineScope,
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PostVoteButton(
            myVote = when (post.myVote) {
                FeedVoteType.UP -> true
                FeedVoteType.DOWN -> false
                else -> null
            },
            votes = post.upVotes - post.downVotes,
            onUpVote = {
                coroutineScope.launch {
                    viewModel.upVote(post.id)
                }
            },
            onDownVote = {
                coroutineScope.launch {
                    viewModel.downVote(post.id)
                }
            },
            enabled = true
        )

        Spacer(Modifier.width(8.dp))
        PostCommentButton(commentCount = post.commentCount) { onComment() }
        Spacer(Modifier.weight(1f))
        if (onPostDeleted != null && isDetailedView) PostShareButton(
            url = Constants.feedShareURL + post.id,
            context = context
        )
    }
}

@Composable
fun FeedPostRowSkeleton() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(12.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                Box(
                    modifier = Modifier
                        .size(width = 400.dp, height = 200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(32.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(32.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        Column(Modifier
            .fillMaxWidth()
            .noRippleClickable {}
        ) {
            Header(
                FeedPost.mock(),
                {},
                false,
            ) {}
            Content(FeedPost.mock(), true, {})
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun SkeletonPreview() {
    Theme { FeedPostRowSkeleton() }
}