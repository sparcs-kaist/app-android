package org.sparcs.soap.App.Features.PostList.Components.PostListRow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.downvote
import org.sparcs.soap.App.theme.ui.upvote
import org.sparcs.soap.R

@Composable
fun PostListRowVoteLabel(voteCount: Int){
    if(voteCount > 0){
        //up voted

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.baseline_arrow_up_bold),
                contentDescription = null,
                colorFilter = ColorFilter.tint(upvote),
                modifier = Modifier.size(15.dp)
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
            Image(
                painter = painterResource(R.drawable.baseline_arrow_down_bold),
                contentDescription = null,
                colorFilter = ColorFilter.tint(downvote),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.padding(2.dp))

            Text(
                text = "$voteCount",
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