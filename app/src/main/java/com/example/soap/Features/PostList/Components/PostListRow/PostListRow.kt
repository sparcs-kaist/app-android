package com.example.soap.Features.PostList.Components.PostListRow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Models.Post
import com.example.soap.Utilities.Extensions.timeAgoDisplay
import com.example.soap.Utilities.Mocks.mockList
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun PostListRow(
    post: Post,
    navController: NavController){
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {navController.navigate(Channel.PostView.name)}
    ) {
        Column(Modifier.align(Alignment.CenterVertically)){
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.padding(1.dp))

            Text(
                text = post.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.soapColors.grayBB,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.padding(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                if (post.voteCount != 0 || post.commentCount > 0) {

                    Row(modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.soapColors.grayf8)
                        .padding(horizontal = 2.dp)) {
                        PostListRowVoteLabel(post.voteCount)

                        PostListRowCommentLabel(post.commentCount)
                    }

                    Spacer(Modifier.padding(4.dp))
                }


                Text(
                    text = post.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.soapColors.grayBB
                )

                Spacer(Modifier.padding(4.dp))

                Text(
                    text = post.createdAt.timeAgoDisplay(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.soapColors.grayBB

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
            Post.mockList()[5],
            rememberNavController()
        )
    }
}