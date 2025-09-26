package com.example.soap.Features.Post.Components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.Theme

@Composable
fun PostVoteButton(
    myVote: Boolean?,
    votes: Int,
    onDownVote: () -> Unit,
    onUpVote: () -> Unit
) {
    val upvoteImage = if (myVote == true) R.drawable.icon_arrowup else R.drawable.icon_arrowup //filled, outlined
    val downvoteImage = if (myVote == false) R.drawable.icon_arrowdown else R.drawable.icon_arrowdown //filled, outlined

    val upVoteColor = Color(0xFF4CAF50)
    val downVoteColor = Color(0xFFF44336)

    val tintColor = when (myVote) {
        true -> upVoteColor
        false -> downVoteColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(upvoteImage),
                contentDescription = "UpVote",
                tint = if (myVote == true) upVoteColor else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onUpVote() }
            )
            AnimatedContent(
                targetState = votes,
                label = "VotesTransition"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    color = if (myVote != null) tintColor else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }


        VerticalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(20.dp)
        )
        Icon(
            painter = painterResource(downvoteImage),
            contentDescription = "DownVote",
            tint = if (myVote == false) downVoteColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp).clickable { onDownVote() }
        )

    }
}


@Composable
@Preview
private fun Preview(){
    Theme { PostVoteButton(true, 2, {}, {}) }
}