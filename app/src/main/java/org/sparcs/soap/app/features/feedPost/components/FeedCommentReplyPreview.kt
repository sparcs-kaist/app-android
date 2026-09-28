package org.sparcs.soap.app.features.feedPost.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.shared.mocks.feed.mockList
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
    Row(modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .padding(vertical = 8.dp)) {
        Spacer(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Column(Modifier
            .weight(1f)
            .padding(horizontal = 8.dp)) {
            Text(
                stringResource(R.string.feed_replying_to, authorName),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (comment.isDeleted) stringResource(R.string.this_comment_has_been_deleted) else comment.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel))
        }
    }
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
