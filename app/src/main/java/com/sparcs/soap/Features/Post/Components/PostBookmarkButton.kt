package com.sparcs.soap.Features.Post.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.lightGray0
import kotlinx.coroutines.launch

@Composable
fun PostBookmarkButton(
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
){
    val scope = rememberCoroutineScope()

    Card(
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.lightGray0),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable {
            scope.launch {
                onToggleBookmark()
            }
        }
    ){
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = if(isBookmarked) painterResource(R.drawable.baseline_bookmark_filled) else painterResource(R.drawable.bookmark_border),
                contentDescription = stringResource(R.string.to_bookmark),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { PostBookmarkButton(true, {}) }
}