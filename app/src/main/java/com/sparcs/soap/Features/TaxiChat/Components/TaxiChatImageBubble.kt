package com.sparcs.soap.Features.TaxiChat.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.sparcs.soap.Domain.Helpers.Constants
import java.util.Date

@Composable
fun TaxiChatImageBubble(
    id: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageUrl = Constants.taxiChatImageURL + id
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(coil.size.Size.ORIGINAL)
            .crossfade(true)
            .build()
    )
    val state = painter.state

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
    ) {
        when (state) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = "Chat Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .fillMaxWidth()
                        .clickable { onClick() }
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TaxiChatUserWrapper(
        authorID = null,
        authorName = null,
        authorProfileImageURL = null,
        date = Date(),
        isMe = false,
        isGeneral = false,
        isWithdrawn = false
    ) {
        TaxiChatImageBubble(id = "688714fb95fce20ddc8f19da", {})
    }
}
