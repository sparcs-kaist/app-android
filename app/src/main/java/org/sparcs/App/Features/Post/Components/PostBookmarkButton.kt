package org.sparcs.App.Features.Post.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.lightGray0
import org.sparcs.R

@Composable
fun PostBookmarkButton(
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
){
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    Card(
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.lightGray0),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable {
            scope.launch {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onToggleBookmark()
            }
        }
    ){
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if(isBookmarked) Icons.Default.Bookmark else Icons.Outlined.Bookmark,
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