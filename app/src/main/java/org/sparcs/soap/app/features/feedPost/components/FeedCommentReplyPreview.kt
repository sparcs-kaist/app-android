package org.sparcs.soap.app.features.feedPost.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.shared.mocks.feed.mockList
import org.sparcs.soap.app.shared.views.contentViews.CommentReplyPreview
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun FeedCommentReplyPreview(
    comment: FeedComment,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authorName = if (comment.isAnonymous) {
        comment.authorName.replace("Anonymous", stringResource(R.string.anonymous))
    } else comment.authorName
    CommentReplyPreview(
        authorName = authorName,
        content = if (comment.isDeleted) stringResource(R.string.this_comment_has_been_deleted) else comment.content,
        onCancel = onCancel,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun ReplyPreview() {
    Theme { FeedCommentReplyPreview(FeedComment.mockList().first(), onCancel = {}) }
}

@Preview(showBackground = true)
@Composable
private fun DeletedReplyPreview() {
    Theme {
        FeedCommentReplyPreview(
            FeedComment.mockList().first().copy(isDeleted = true),
            onCancel = {})
    }
}
