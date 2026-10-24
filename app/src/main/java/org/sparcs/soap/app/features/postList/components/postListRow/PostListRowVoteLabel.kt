package org.sparcs.soap.app.features.postList.components.postListRow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote
import kotlin.math.abs

@Composable
fun PostListRowVoteLabel(voteCount: Int){
    if(voteCount > 0){
        //up voted
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Same filled Material arrow as the vote pill.
            Icon(
                imageVector = Icons.Filled.Forward,
                contentDescription = null,
                tint = upvote,
                modifier = Modifier
                    .size(13.dp)
                    .rotate(-90f)
            )
            Spacer(Modifier.padding(2.dp))

            Text(
                text = "$voteCount",
                color = upvote,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
    else if (voteCount < 0){
        //down voted
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Same filled Material arrow as the vote pill.
            Icon(
                imageVector = Icons.Filled.Forward,
                contentDescription = null,
                tint = downvote,
                modifier = Modifier
                    .size(13.dp)
                    .rotate(90f)
            )
            Spacer(Modifier.padding(2.dp))

            Text(
                text = "−${abs(voteCount)}",
                color = downvote,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        Column{
            PostListRowVoteLabel(20)
            PostListRowVoteLabel(-20)
        }
    }
}