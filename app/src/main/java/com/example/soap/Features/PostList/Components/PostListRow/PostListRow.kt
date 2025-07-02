package com.example.soap.Features.PostList.Components.PostListRow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.soap.Models.Post
import com.example.soap.Utilities.Extensions.timeAgoDisplay
import com.example.soap.Utilities.Mocks.mockList
import com.example.soap.ui.theme.SoapTheme

@Composable
fun PostListRow(post: Post){

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.align(Alignment.CenterVertically)){
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.padding(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                if (post.voteCount != 0 || post.commentCount > 0) {

                    Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                        PostListRowVoteLabel(post.voteCount)

                        PostListRowCommentLabel(post.commentCount)
                    }

                    Spacer(Modifier.padding(4.dp))
                }


                Text(
                    text = post.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.padding(4.dp))

                Text(
                    text = post.createdAt.timeAgoDisplay(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant

                )

            }
        }

        Spacer(Modifier.weight(1f))

        if (post.thumbnailURL != null) {
            AsyncImage(
                model = post.thumbnailURL.toString(),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme {
        PostListRow(
            Post.mockList()[5]
        )
    }
}