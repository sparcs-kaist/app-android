package com.example.soap.Features.Post.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.gray64

@Composable
fun PostCommentButton(){
    Row(
        modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.chat_bubble_outline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.gray64,
            modifier = Modifier.size(15.dp)
        )

        Spacer(Modifier.padding(2.dp))

        Text(
            text = "23",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.gray64
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { PostCommentButton() }
}