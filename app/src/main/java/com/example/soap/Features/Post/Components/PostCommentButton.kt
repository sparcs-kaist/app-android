package com.example.soap.Features.Post.Components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.Theme

@Composable
fun PostCommentButton(
    commentCount: Int,
    onClick: () -> Unit
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_chat),
                contentDescription = "Comments",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.padding(2.dp))

            AnimatedContent(
                targetState = commentCount,
                transitionSpec = {
                    ((slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { +it } + fadeOut()))
                        .using(SizeTransform(clip = false))
                },
                label = "CommentCountTransition"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    var comment by remember { mutableStateOf(1) }
    Theme { PostCommentButton(comment, {comment = comment + 1}) }
}