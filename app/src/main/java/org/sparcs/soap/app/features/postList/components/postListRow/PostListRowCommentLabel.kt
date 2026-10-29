package org.sparcs.soap.app.features.postList.components.postListRow

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme


@Composable
fun PostListRowCommentLabel(
    commentCount: Int,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    if(commentCount > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )

            Spacer(Modifier.padding(2.dp))

            Text(
                text = "$commentCount",
                maxLines = 1,
                color = color,
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