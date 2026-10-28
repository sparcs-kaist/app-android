package org.sparcs.soap.app.features.post.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.CommentReplyPreview
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostCommentReplyPreview(
    comment: AraPostComment,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rawName = comment.author.profile.nickname
    val authorName = if (rawName.contains("Anonymous")) {
        rawName.replace("Anonymous", stringResource(R.string.anonymous))
    } else rawName
    CommentReplyPreview(
        authorName = authorName,
        content = if (comment.isHidden == true) stringResource(R.string.this_comment_has_been_deleted)
        else comment.content.orEmpty(),
        onCancel = onCancel,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun PostReplyPreview() {
    Theme { PostCommentReplyPreview(AraPostComment.mockList().first(), onCancel = {}) }
}
