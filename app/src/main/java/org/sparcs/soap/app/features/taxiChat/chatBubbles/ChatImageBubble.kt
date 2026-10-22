package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.sparcs.soap.app.domain.helpers.Constants

private val MAX_IMAGE_HEIGHT = 240.dp

@Composable
fun ChatImageBubble(
    id: String,
    onClick: (String) -> Unit
) {
    val context = LocalContext.current
    val maxDecodePx = with(LocalDensity.current) { MAX_IMAGE_HEIGHT.roundToPx() } * 2
    val request = remember(id, maxDecodePx) {
        ImageRequest.Builder(context)
            .data(Constants.taxiChatImageURL + id)
            .size(maxDecodePx)
            .crossfade(true)
            .build()
    }
    val painter = rememberAsyncImagePainter(model = request)
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
                        .heightIn(max = MAX_IMAGE_HEIGHT)
                        .clickable { onClick(id) }
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 300.dp)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(24.dp))
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ChatImageBubble(id = "688714fb95fce20ddc8f19da") {}
}
