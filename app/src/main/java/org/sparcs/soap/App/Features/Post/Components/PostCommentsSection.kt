package org.sparcs.soap.App.Features.Post.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Enums.Ara.AraContentReportType
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.Features.Post.PostCommentCell
import org.sparcs.soap.R

@Composable
fun PostCommentsSection(
    comments: List<AraPostComment>,
    onReply: (AraPostComment) -> Unit,
    onCommentDeleted: () -> Unit,
    onEdit: (AraPostComment) -> Unit,
    onUpVote: (AraPostComment) -> Unit,
    onDownVote: (AraPostComment) -> Unit,
    onReport: (Int, AraContentReportType) -> Unit,
    onDeleteComment: (AraPostComment) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (comments.isEmpty()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.lightGray0,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            UnavailableView(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = stringResource(R.string.no_one_has_commented_yet),
                description = stringResource(R.string.be_the_first_one_to_share_your_thoughts)
            )
        } else {
            comments.forEach { comment ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CommentCell(
                        comment = comment,
                        isThreaded = false,
                        onReply = onReply,
                        onCommentDeleted = onCommentDeleted,
                        onEdit = onEdit,
                        onUpVote = onUpVote,
                        onDownVote = onDownVote,
                        onReport = onReport,
                        onDeleteComment = onDeleteComment
                    )

                    comment.comments.forEach { thread ->
                        CommentCell(
                            comment = thread,
                            isThreaded = true,
                            onReply = onReply,
                            onCommentDeleted = onCommentDeleted,
                            onEdit = onEdit,
                            onUpVote = onUpVote,
                            onDownVote = onDownVote,
                            onReport = onReport,
                            onDeleteComment = onDeleteComment
                        )
                    }
                }
            }
        }
    }
}

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
    onDeleteComment: (AraPostComment) -> Unit
) {
    PostCommentCell(
        comment = comment,
        isThreaded = isThreaded,
        onComment = { onReply(comment) },
        onDelete = { onCommentDeleted() },
        onEdit = { onEdit(comment) },
        onUpVote = { onUpVote(comment) },
        onDownVote = { onDownVote(comment) },
        onReport = { type -> onReport(comment.id, type) },
        onDeleteComment = { onDeleteComment(comment) }
    )
}