package com.example.soap.Features.PostList.Components.PostListRow

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.R
import com.example.soap.Shared.Extensions.timeAgoDisplay
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.gray64
import com.example.soap.ui.theme.grayBB

@Composable
fun PostListRow(
    post: AraPost,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            post.topic?.let { topic ->
                Text(
                    text = "[" + topic.name.localized()+ "]",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(4.dp))
            }


            Text(
                text = title(post),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (post.isHidden) MaterialTheme.colorScheme.gray64 else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(4.dp))

            if (post.attachmentType == AraPost.AttachmentType.IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                Icon(
                    painter = painterResource(R.drawable.round_image),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.grayBB,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (post.attachmentType == AraPost.AttachmentType.NON_IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                Icon(
                    painter = painterResource(R.drawable.attach_file),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.grayBB,
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
                color = MaterialTheme.colorScheme.grayBB
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${post.views} views",
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

@Composable
fun PostListSkeletonRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .width(listOf(0.dp, 0.dp, 20.dp, 40.dp).random())
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .width((10..200).random().dp)
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(12.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(12.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

private fun title(post: AraPost): String {
    return when {
        post.isHidden && post.isNSFW -> "This post contains NSFW content"
        post.isHidden && post.isPolitical -> "This post contains political content"
        post.isHidden -> "This post is hidden"
        else -> post.title ?: "Untitled"
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(){
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
private fun PreviewSkeleton(){
    Theme {
        Column {
            repeat(15){ PostListSkeletonRow() }
        }
    }
}


