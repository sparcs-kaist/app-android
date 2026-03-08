package org.sparcs.soap.App.Features.FullscreenImage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen

@Composable
fun FullscreenImageView(
    id: String?,
    onDismiss: () -> Unit,
) {
    val imageUrl = Constants.taxiChatImageURL + id
    if (id != null) {
        BackHandler {
            onDismiss()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
                .analyticsScreen("Fullscreen Image"),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}