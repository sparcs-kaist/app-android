package org.sparcs.soap.App.Features.FeedPost.Components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.DeepLink
import org.sparcs.soap.App.Domain.Enums.DeepLinkEventBus
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.FeedPost.FeedPostViewModel
import org.sparcs.soap.App.Features.FeedPost.FeedPostViewModelProtocol
import org.sparcs.soap.App.Features.Post.Components.PostCommentButton
import org.sparcs.soap.App.Features.Post.Components.PostVoteButton
import org.sparcs.soap.App.Features.Settings.Components.InfoTooltip
import org.sparcs.soap.App.Shared.Extensions.timeAgoDisplay
import org.sparcs.soap.App.Shared.Extensions.toDetectedAnnotatedString
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.ViewModelMocks.Feed.MockFeedPostViewModel
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.grayF8
import org.sparcs.soap.Features.Post.PostCommentActionsMenu
import org.sparcs.soap.R

@Composable
fun FeedCommentRow(
    comment: FeedComment,
    isReply: Boolean,
    onReply: () -> Unit,
    viewModel: FeedPostViewModelProtocol
) {
    val coroutineScope = rememberCoroutineScope()


    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (isReply) {
            Icon(
                imageVector = Icons.Rounded.SubdirectoryArrowRight,
                contentDescription = null,
                modifier = Modifier.padding(top = 8.dp, end = 4.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(vertical = 4.dp)
            ) //TODO - 추가할지 말지 고민
            Header(
                comment = comment,
                onDelete = {
                    coroutineScope.launch {
                        viewModel.deleteComment(comment)
                    }
                },
                onReport = { reason ->
                    coroutineScope.launch {
                        viewModel.reportComment(comment.id, reason)
                    }
                }
            )

            Content(comment)

            Footer(
                comment = comment,
                onReply = onReply,
                onUpVote = {
                    coroutineScope.launch {
                        val newType = if (comment.myVote == FeedVoteType.UP) null else FeedVoteType.UP
                        viewModel.voteComment(comment, newType)
                    }
                },
                onDownVote = {
                    coroutineScope.launch {
                        val newType = if (comment.myVote == FeedVoteType.DOWN) null else FeedVoteType.DOWN
                        viewModel.voteComment(comment, newType)
                    }
                }
            )
        }
    }
}

@Composable
private fun Header(
    comment: FeedComment,
    onDelete: () -> Unit,
    onReport: (FeedReportType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val author =
        if (comment.isAnonymous) {
            val number = comment.authorName.substringAfter("Anonymous", "").trim()
            "${stringResource(R.string.anonymous)} $number"
        } else {
            comment.authorName
        }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileImage(comment)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (comment.isAuthor) "$author (${stringResource(R.string.author)})" else author,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = if (comment.isAuthor) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
        Spacer(Modifier.width(8.dp))
        if (comment.isKaistIP) {
            InfoTooltip(
                tooltipText = stringResource(R.string.kaist_ip_verified),
                icon = painterResource(R.drawable.checkmark_seal_fill),
                tint = MaterialTheme.colorScheme.primary,
                iconSize = 15.dp
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = comment.createdAt.timeAgoDisplay(),
            color = MaterialTheme.colorScheme.grayBB,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!comment.isDeleted) {
            PostCommentActionsMenu(
                enumClass = FeedReportType::class,
                isMine = comment.isMyComment,
                onEdit = {/*Todo - edit*/ },
                onDelete = {
                    expanded = false
                    onDelete()
                },
                onReport = onReport,
                onTranslate = {/*Todo - translate*/ },
                isComment = true
            )
        }
    }
}

@Composable
private fun ProfileImage(comment: FeedComment) {
    if (comment.profileImageURL != null) {
        AsyncImage(
            model = comment.profileImageURL,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.grayF8),
            contentAlignment = Alignment.Center
        ) {
            Text("😀", fontSize = 12.sp)
        }
    }
}

@Composable
private fun Content(comment: FeedComment) {
    val context = LocalContext.current
    val text = if (comment.isDeleted) stringResource(R.string.this_comment_has_been_deleted) else comment.content
    val color = if (comment.isDeleted) MaterialTheme.colorScheme.grayBB.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }
    var hasMeasured by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val moreText = stringResource(R.string.more)
    val moreColor = MaterialTheme.colorScheme.grayBB
    val linkColor = MaterialTheme.colorScheme.primary

    val displayText = remember(text, expanded, isOverflowing, textLayoutResult) {
        if (comment.isDeleted) {
            return@remember AnnotatedString(text)
        }

        val annotatedContent = text.toDetectedAnnotatedString(linkColor)

        if (expanded || !isOverflowing || textLayoutResult == null) {
            annotatedContent
        } else {
            val visibleEnd = textLayoutResult!!.getLineEnd(2, visibleEnd = true)
            buildAnnotatedString {
                append(annotatedContent.subSequence(0, visibleEnd.coerceAtMost(annotatedContent.length)))
                pushStringAnnotation("MORE", "expand")
                append("… ")
                withStyle(SpanStyle(color = moreColor, fontWeight = FontWeight.SemiBold)) {
                    append(moreText)
                }
                pop()
            }
        }
    }

    ClickableText(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium.copy(color = color),
        maxLines = if (!expanded) 4 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layoutResult ->
            if (!hasMeasured && !expanded) {
                hasMeasured = true
                isOverflowing = layoutResult.hasVisualOverflow
                textLayoutResult = layoutResult
            }
        },
        onClick = { offset ->
            if (comment.isDeleted) return@ClickableText

            displayText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { annotation ->
                handleURL(context, annotation.item, scope)
                return@ClickableText
            }

            displayText.getStringAnnotations("MORE", offset, offset).firstOrNull()?.let {
                expanded = true
            }
        },
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun Footer(
    comment: FeedComment,
    onReply: () -> Unit,
    onUpVote: () -> Unit,
    onDownVote: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1f))

        if (comment.parentCommentID == null) {
            PostCommentButton(
                commentCount = comment.replyCount,
                onClick = onReply
            )
            Spacer(modifier = Modifier.padding(4.dp))
        }

        if (!comment.isDeleted) {
            PostVoteButton(
                myVote = when (comment.myVote) {
                    FeedVoteType.UP -> true
                    FeedVoteType.DOWN -> false
                    else -> null
                },
                votes = comment.upVotes - comment.downVotes,
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                enabled = true
            )
        }
    }
}

private fun handleURL(
    context: Context,
    urlString: String,
    scope: CoroutineScope
) {
    val uri = Uri.parse(if (!urlString.startsWith("http")) "http://$urlString" else urlString)
    val deepLink = DeepLink.fromUri(uri)

    if (deepLink != null) {
        scope.launch {
            DeepLinkEventBus.post(deepLink)
        }
    } else {
        try {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(context, uri)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}

/* ____________________________________________________________________*/
@Preview(showBackground = true, name = "Root Comment")
@Composable
private fun RootCommentPreview() {
    val mockViewModel = MockFeedPostViewModel(initialState = FeedPostViewModel.ViewState.Loaded(
        FeedPost.mock()))
    Theme {
        FeedCommentRow(
            comment = FeedComment.mock().copy(
                content = "This is a root comment with enough content to test layout.",
                replyCount = 3
            ),
            isReply = false,
            onReply = {},
            viewModel = mockViewModel
        )
    }
}

@Preview(showBackground = true, name = "Reply Comment")
@Composable
private fun ReplyCommentPreview() {
    val mockViewModel = MockFeedPostViewModel(initialState = FeedPostViewModel.ViewState.Loaded(
        FeedPost.mock()))
    Theme {
        FeedCommentRow(
            comment = FeedComment.mock().copy(
                content = "This is a reply comment.",
                parentCommentID = "parent_id",
                myVote = FeedVoteType.UP
            ),
            isReply = true,
            onReply = {},
            viewModel = mockViewModel
        )
    }
}

@Preview(showBackground = true, name = "Deleted Comment")
@Composable
private fun DeletedCommentPreview() {
    val mockViewModel = MockFeedPostViewModel(initialState = FeedPostViewModel.ViewState.Loaded(
        FeedPost.mock()))
    Theme {
        FeedCommentRow(
            comment = FeedComment.mock().copy(
                content = "Deleted content",
                isDeleted = true
            ),
            isReply = false,
            onReply = {},
            viewModel = mockViewModel
        )
    }
}