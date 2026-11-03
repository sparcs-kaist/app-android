package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme

fun LazyListScope.postCommentsSection(
    comments: List<AraPostComment>,
    onReply: (AraPostComment) -> Unit,
    onCommentDeleted: () -> Unit,
    onEdit: (AraPostComment) -> Unit,
    onUpVote: (AraPostComment) -> Unit,
    onDownVote: (AraPostComment) -> Unit,
    onReport: (Int, AraContentReportType) -> Unit,
    onDeleteComment: (AraPostComment) -> Unit,
    onTranslateComment: (AraPostComment) -> Unit,
    onSetAuthorBlocked: (AraPostAuthor, Boolean) -> Unit,
    showsTopDivider: Boolean = true,
    reservesReplySpace: Boolean = false,
) {
    if (comments.isEmpty()) {
        item(key = "comments-empty") {
            Column(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 32.dp)) {
                if (showsTopDivider) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                UnavailableView(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = stringResource(R.string.no_one_has_commented_yet),
                    description = stringResource(R.string.be_the_first_one_to_share_your_thoughts)
                )
            }
        }
        return
    }
    val rows = commentRows(comments)
    items(rows, key = { (comment, _) -> commentKey(comment.id) }) { (comment, isThreaded) ->
        Box(Modifier.animateItem()) {
            CommentCell(
                comment = comment,
                isThreaded = isThreaded,
                onReply = onReply,
                onCommentDeleted = onCommentDeleted,
                onEdit = onEdit,
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                onReport = onReport,
                onDeleteComment = onDeleteComment,
                onTranslateComment = onTranslateComment,
                onSetAuthorBlocked = onSetAuthorBlocked
            )
        }
    }
    item(key = "comments-bottom") { Spacer(Modifier.height(32.dp)) }
    if (reservesReplySpace) {
        item(key = "comments-reply-space") { Spacer(Modifier.fillParentMaxHeight()) }
    }
}

fun commentIndex(comments: List<AraPostComment>, commentId: Int, totalItemsCount: Int, reservesReplySpace: Boolean): Int? {
    val rows = commentRows(comments)
    val position = rows.indexOfFirst { (comment, _) -> comment.id == commentId }.takeIf { it >= 0 } ?: return null
    val trailingItems = if (reservesReplySpace) 2 else 1
    return (totalItemsCount - trailingItems - (rows.size - position)).takeIf { it >= 0 }
}

private fun commentRows(comments: List<AraPostComment>): List<Pair<AraPostComment, Boolean>> =
    comments.flatMap { comment -> listOf(comment to false) + comment.comments.map { it to true } }

private fun commentKey(commentId: Int) = "comment-$commentId"

@Composable
private fun CommentCell(
    comment: AraPostComment,
    isThreaded: Boolean,
    onReply: (AraPostComment) -> Unit,
    onCommentDeleted: () -> Unit,
    onEdit: (AraPostComment) -> Unit,
    onUpVote: (AraPostComment) -> Unit,
    onDownVote: (AraPostComment) -> Unit,
    onReport: (Int, AraContentReportType) -> Unit,
    onDeleteComment: (AraPostComment) -> Unit,
    onTranslateComment: (AraPostComment) -> Unit,
    onSetAuthorBlocked: (AraPostAuthor, Boolean) -> Unit,
) {
    val isBlockable = comment.isMine != true && comment.author.blockableUserId != null
    PostCommentCell(
        comment = comment,
        isThreaded = isThreaded,
        onComment = { onReply(comment) },
        onDelete = { onCommentDeleted() },
        onEdit = { onEdit(comment) },
        onUpVote = { onUpVote(comment) },
        onDownVote = { onDownVote(comment) },
        onReport = { type -> onReport(comment.id, type) },
        onDeleteComment = { onDeleteComment(comment) },
        onTranslate = { onTranslateComment(comment) },
        onSetAuthorBlocked = if (isBlockable) {
            { blocked -> onSetAuthorBlocked(comment.author, blocked) }
        } else null
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        LazyColumn {
            postCommentsSection(
            comments = listOf(
                AraPostComment.mock(),
                AraPostComment.mock(),
            ),
            onReply = {},
            onCommentDeleted = {},
            onEdit = {},
            onUpVote = {},
            onDownVote = {},
            onReport = { _, _ -> },
            onDeleteComment = {},
            onTranslateComment = {},
            onSetAuthorBlocked = { _, _ -> }
            )
        }
    }
}