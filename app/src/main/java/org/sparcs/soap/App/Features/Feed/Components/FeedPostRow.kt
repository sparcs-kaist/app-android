package org.sparcs.soap.App.Features.Feed.Components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.DeepLink
import org.sparcs.soap.App.Domain.Enums.DeepLinkEventBus
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.FeedViewModelProtocol
import org.sparcs.soap.App.Features.Post.Components.PostCommentButton
import org.sparcs.soap.App.Features.Post.Components.PostVoteButton
import org.sparcs.soap.App.Features.Settings.Components.InfoTooltip
import org.sparcs.soap.App.Shared.Extensions.noRippleClickable
import org.sparcs.soap.App.Shared.Extensions.relativeTimeString
import org.sparcs.soap.App.Shared.Extensions.timeAgoDisplay
import org.sparcs.soap.App.Shared.Extensions.toDetectedAnnotatedString
import org.sparcs.soap.App.Shared.Mocks.Feed.mock
import org.sparcs.soap.App.Shared.Mocks.Feed.mockList
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.BuddyPreviewSupport.Feed.PreviewFeedViewModel
import org.sparcs.soap.R

@Composable
fun FeedPostRow(
    post: FeedPost,
    viewModel: FeedViewModelProtocol,
    onPostDeleted: ((String) -> Unit)?,
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
        Footer(post, viewModel, onComment, coroutineScope)
    }
}

@Composable
private fun ProfileImage(post: FeedPost) {
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
private fun Header(
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
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        ProfileImage(post)
        Spacer(Modifier.width(8.dp))
        Text(
            text = authorName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f, false),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Spacer(Modifier.width(8.dp))
        if (post.isKaistIP) {
            InfoTooltip(
                tooltipText = stringResource(R.string.kaist_ip_verified),
                icon = painterResource(R.drawable.checkmark_seal_fill),
                tint = MaterialTheme.colorScheme.primary,
                iconSize = 15.dp
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
private fun Content(
    post: FeedPost,
    singleLine: Boolean,
    onComment: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary
    val moreColor = MaterialTheme.colorScheme.grayBB
    val moreText = stringResource(R.string.more)

    val scope = rememberCoroutineScope()

    val annotatedContent = remember(post.content) {
        post.content.toDetectedAnnotatedString(linkColor)
    }

    val displayText = remember(annotatedContent, expanded, isOverflowing, textLayoutResult) {
        if (expanded || !isOverflowing || textLayoutResult == null) {
            annotatedContent
        } else {
            val visibleEnd = textLayoutResult!!.getLineEnd(1, visibleEnd = true)

            buildAnnotatedString {
                val cutIndex = (visibleEnd - 7).coerceAtLeast(0)
                append(annotatedContent.subSequence(0, cutIndex))

                pushStringAnnotation("MORE", "expand")
                append("… ")
                withStyle(SpanStyle(color = moreColor, fontWeight = FontWeight.SemiBold)) {
                    append(moreText)
                }
                pop()
            }
        }
    }

    SelectionContainer {
        ClickableText(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = if (!expanded && singleLine) 2 else Int.MAX_VALUE,
            onTextLayout = { layoutResult ->
                if (textLayoutResult == null && !expanded) {
                    isOverflowing = layoutResult.hasVisualOverflow
                    textLayoutResult = layoutResult
                }
            },
            onClick = { offset ->
                displayText.getStringAnnotations("URL", offset, offset).firstOrNull()
                    ?.let { annotation ->
                        handleURL(context, annotation.item, scope)
                        return@ClickableText
                    }

                displayText.getStringAnnotations("MORE", offset, offset).firstOrNull()?.let {
                    expanded = true
                    return@ClickableText
                }

                onComment()
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    if (post.images.isNotEmpty()) {
        PostImagesStrip(images = post.images, onComment)
    }
}

@Composable
private fun Footer(
    post: FeedPost,
    viewModel: FeedViewModelProtocol,
    onComment: () -> Unit,
    coroutineScope: CoroutineScope,
) {
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

private fun handleURL(
    context: Context,
    urlString: String,
    scope: CoroutineScope,
) {
    val uri = Uri.parse(if (!urlString.startsWith("http")) "http://$urlString" else urlString)

    val deepLink = DeepLink.fromUri(uri)

    if (deepLink != null) {
        scope.launch {
            DeepLinkEventBus.post(deepLink)
        }
    } else {
        try {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}

// MARK: - Previews
@Preview(showBackground = true, name = "With Actions")
@Composable
private fun PreviewWithActions() {
    Theme {
        FeedPostRow(
            post = FeedPost.mock(),
            viewModel = PreviewFeedViewModel(),
            onPostDeleted = { _ -> },
            onComment = { },
            singleLine = false
        )
    }
}

@Preview(showBackground = true, name = "Without Actions")
@Composable
private fun PreviewWithoutActions() {
    Theme {
        FeedPostRow(
            post = FeedPost.mock(),
            viewModel = PreviewFeedViewModel(),
            onPostDeleted = { _ -> },
            onComment = { },
            singleLine = true
        )
    }
}

@Preview(showBackground = true, name = "Anonymous")
@Composable
private fun PreviewAnonymous() {
    Theme {
        FeedPostRow(
            post = FeedPost.mockList()[4],
            viewModel = PreviewFeedViewModel(),
            onPostDeleted = { _ -> },
            onComment = { },
            singleLine = false
        )
    }
}

@Preview(showBackground = true, name = "Long Content")
@Composable
private fun PreviewLongContent() {
    Theme {
        FeedPostRow(
            post = FeedPost.mockList()[3],
            viewModel = PreviewFeedViewModel(),
            onPostDeleted = { _ -> },
            onComment = { },
            singleLine = true
        )
    }
}

@Preview(showBackground = true, name = "Multiple Images")
@Composable
private fun PreviewMultipleImages() {
    Theme {
        FeedPostRow(
            post = FeedPost.mockList()[2],
            viewModel = PreviewFeedViewModel(),
            onPostDeleted = { _ -> },
            onComment = { },
            singleLine = false
        )
    }
}

@Preview(showBackground = true, name = "URL Content")
@Composable
private fun PreviewURLContent() {
    Theme {

        FeedPostRow(
            post = FeedPost.mockList()[5],
            viewModel = PreviewFeedViewModel(),
            onPostDeleted = { _ -> },
            onComment = { },
            singleLine = false
        )
    }
}