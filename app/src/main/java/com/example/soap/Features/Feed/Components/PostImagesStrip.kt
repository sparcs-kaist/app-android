package com.example.soap.Features.Feed.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.soap.Domain.Models.Feed.FeedImage

@Composable
fun PostImagesStrip(images: List<FeedImage>) {
    val hPadding = 16.dp

    val imageHeight = 150.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(imageHeight)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = hPadding)
        ) {
            items(images) { item ->
                SubcomposeAsyncImage(
                    model = item.url.toString(),
                    contentDescription = null,
                    modifier = Modifier
                        .height(imageHeight)
                        .width(imageHeight * 16 / 9)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                ) {
                    val state = painter.state
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            Placeholder(width = (imageHeight * 16 / 9), height = imageHeight)
                        }
                        is AsyncImagePainter.State.Error -> {
                            Placeholder(width = (imageHeight * 16 / 9), height = imageHeight, systemImage = Icons.Default.Warning)
                        }
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }
        }
    }
}


@Composable
fun Placeholder(width: Dp, height: Dp, systemImage: ImageVector? = null) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        systemImage?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
