package org.sparcs.soap.app.features.postList.components.postListRow

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraHiddenReason
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote

@Composable
fun PostListRow(
    post: AraPost,
    modifier: Modifier = Modifier,
) {
    if (post.isHidden) {
        HiddenPostListRow(post, modifier)
    } else {
        VisiblePostListRow(post, modifier)
    }
}

@Composable
private fun HiddenPostListRow(
    post: AraPost,
    modifier: Modifier = Modifier,
) {
    val metaColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = post.hiddenIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(post.hiddenReasonRes),
                style = MaterialTheme.typography.titleMedium,
                color = metaColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.views, post.views),
                style = MaterialTheme.typography.bodySmall,
                color = metaColor
            )

            Text(
                text = post.createdAt.timeAgoDisplay(),
                style = MaterialTheme.typography.bodySmall,
                color = metaColor
            )
        }
    }
}

@Composable
private fun VisiblePostListRow(
    post: AraPost,
    modifier: Modifier = Modifier,
) {
    val metaColor = post.metaColor()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (post.isUpdatedSinceRead) {
                UpdatedMarker(Modifier.padding(end = 6.dp))
            }

            post.topic?.let { topic ->
                Text(
                    text = "[" + topic.name.localized() + "]",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = post.accentColor()
                )

                Spacer(Modifier.width(4.dp))
            }

            Text(
                text = title(post) { id -> stringResource(id) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
                color = post.titleColor()
            )

            Spacer(modifier = Modifier.width(4.dp))

            if (post.attachmentType == AraPost.AttachmentType.IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = metaColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (post.attachmentType == AraPost.AttachmentType.NON_IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = null,
                    tint = metaColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        post.marketListing?.let { listing ->
            MarketListingLabel(listing, Modifier.padding(top = 2.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val voteCount = post.upVotes - post.downVotes

            if (voteCount != 0 || post.commentCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PostListRowVoteLabel(voteCount, post.accentColor(upvote), post.accentColor(downvote))
                    PostListRowCommentLabel(post.commentCount, metaColor)
                }
            }

            Text(
                text = post.author.profile.nickname,
                style = MaterialTheme.typography.bodySmall,
                color = metaColor
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.views, post.views),
                style = MaterialTheme.typography.bodySmall,
                color = metaColor
            )

            Text(
                text = post.createdAt.timeAgoDisplay(),
                style = MaterialTheme.typography.bodySmall,
                color = metaColor
            )
        }
    }
}

private val skeletonTitleWidths = listOf(0.7f, 0.45f, 0.85f, 0.6f, 0.5f)

@Composable
fun PostListSkeletonRow(index: Int = 0, modifier: Modifier = Modifier) {
    val titleStyle = MaterialTheme.typography.titleMedium
    val metaStyle = MaterialTheme.typography.bodySmall
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SkeletonLine(titleStyle, Modifier.fillMaxWidth(skeletonTitleWidths[index % skeletonTitleWidths.size]))

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonLine(metaStyle, Modifier.width(60.dp))
            Spacer(modifier = Modifier.weight(1f))
            SkeletonLine(metaStyle, Modifier.width(40.dp))
            SkeletonLine(metaStyle, Modifier.width(40.dp))
        }
    }
}

@Composable
fun title(post: AraPost, stringProvider: @Composable (Int) -> String): String =
    if (post.isHidden) stringProvider(post.hiddenReasonRes)
    else post.title ?: stringProvider(R.string.untitled)

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column {
            AraPost.mockList().forEach {
                PostListRow(it, Modifier)
            }
        }
    }
}

@Composable
private fun ReadStatePreviewContent() {
    val post = AraPost.mock().copy(upVotes = 12, downVotes = 2, commentCount = 4)
    Column {
        PostListRow(post.copy(readStatus = AraPost.ReadStatus.UNREAD))
        PostListRow(post.copy(readStatus = AraPost.ReadStatus.READ))
    }
}

@Composable
@Preview(showBackground = true, name = "Read vs unread")
private fun PreviewReadState() {
    Theme { ReadStatePreviewContent() }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF1F1F1F, name = "Read vs unread dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PreviewReadStateDark() {
    Theme { ReadStatePreviewContent() }
}

@Composable
@Preview(showBackground = true)
private fun PreviewReadAndHidden() {
    val post = AraPost.mock()
    Theme {
        Column {
            PostListRow(post.copy(readStatus = AraPost.ReadStatus.UNREAD))
            PostListRow(post.copy(readStatus = AraPost.ReadStatus.READ))
            PostListRow(post.copy(readStatus = AraPost.ReadStatus.UPDATED))
            PostListRow(post.copy(isHidden = true, hiddenReason = listOf(AraHiddenReason.BLOCKED_USER_CONTENT)))
            PostListRow(post.copy(isHidden = true, isNSFW = true))
            PostListRow(post.copy(isHidden = true, isPolitical = true))
            PostListRow(post.copy(isHidden = true))
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewSkeleton() {
    Theme {
        Column {
            repeat(15) { PostListSkeletonRow(it) }
        }
    }
}
