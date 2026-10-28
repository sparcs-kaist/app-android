package org.sparcs.soap.app.features.postList.components.postListRow

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
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostListRow(
    post: AraPost,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            post.topic?.let { topic ->
                Text(
                    text = "[" + topic.name.localized() + "]",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
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
                color = if (post.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(4.dp))

            if (post.attachmentType == AraPost.AttachmentType.IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (post.attachmentType == AraPost.AttachmentType.NON_IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
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
                    PostListRowVoteLabel(voteCount)
                    PostListRowCommentLabel(post.commentCount)
                }
            }

            Text(
                text = post.author.profile.nickname,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.views, post.views),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = post.createdAt.timeAgoDisplay(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Title widths cycle through these so placeholders look like titles without changing between
// recompositions.
private val skeletonTitleWidths = listOf(0.7f, 0.45f, 0.85f, 0.6f, 0.5f)

/**
 * Placeholder with the same height as [PostListRow]: each bar sits in a box one text line tall
 * for the style it stands in for, so swapping to loaded rows doesn't shift the list.
 */
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
fun title(post: AraPost, stringProvider: @Composable (Int) -> String): String {
    return when {
        post.isHidden && post.isNSFW -> stringProvider(R.string.this_post_contains_nsfw_content)
        post.isHidden && post.isPolitical -> stringProvider(R.string.this_post_contains_political_content)
        post.isHidden -> stringProvider(R.string.this_post_is_hidden)
        else -> post.title ?: stringProvider(R.string.untitled)
    }
}

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
@Preview(showBackground = true)
private fun PreviewSkeleton() {
    Theme {
        Column {
            repeat(15) { PostListSkeletonRow(it) }
        }
    }
}


