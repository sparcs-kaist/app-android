package org.sparcs.soap.app.features.post.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostCommentsSection(
    comments: List<AraPostComment>,
    onReply: (AraPostComment) -> Unit,
    onCommentDeleted: () -> Unit,
    onEdit: (AraPostComment) -> Unit,
    onUpVote: (AraPostComment) -> Unit,
    onDownVote: (AraPostComment) -> Unit,
    onReport: (Int, AraContentReportType) -> Unit,
    onDeleteComment: (AraPostComment) -> Unit,
    onTranslateComment: (AraPostComment) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        if (comments.isEmpty()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp),
            )

            UnavailableView(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = stringResource(R.string.no_one_has_commented_yet),
                description = stringResource(R.string.be_the_first_one_to_share_your_thoughts)
            )
        } else {
            comments.forEach { comment ->
                Column {
                    CommentCell(
                        comment = comment,
                        isThreaded = false,
                        onReply = onReply,
                        onCommentDeleted = onCommentDeleted,
                        onEdit = onEdit,
                        onUpVote = onUpVote,
                        onDownVote = onDownVote,
                        onReport = onReport,
                        onDeleteComment = onDeleteComment,
                        onTranslateComment = onTranslateComment
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
                            onDeleteComment = onDeleteComment,
                            onTranslateComment = onTranslateComment
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
    onDeleteComment: (AraPostComment) -> Unit,
    onTranslateComment: (AraPostComment) -> Unit,
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
        onDeleteComment = { onDeleteComment(comment) },
        onTranslate = { onTranslateComment(comment) }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        PostCommentsSection(
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
            onTranslateComment = {}
        )
    }
}