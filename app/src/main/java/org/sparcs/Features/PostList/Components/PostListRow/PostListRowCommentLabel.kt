package org.sparcs.Features.PostList.Components.PostListRow

import androidx.compose.foundation.Image
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
import org.sparcs.R
import org.sparcs.ui.theme.Theme
import org.sparcs.ui.theme.darkGray


@Composable
fun PostListRowCommentLabel(commentCount: Int){
    if(commentCount > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.chat_bubble_outline),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.darkGray),
                modifier = Modifier.size(15.dp)
            )

            Spacer(Modifier.padding(2.dp))

            Text(
                text = "$commentCount",
                maxLines = 1,
                color = MaterialTheme.colorScheme.darkGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
@Preview
private fun Preview(){
    Theme {
        PostListRowCommentLabel(20)
    }
}