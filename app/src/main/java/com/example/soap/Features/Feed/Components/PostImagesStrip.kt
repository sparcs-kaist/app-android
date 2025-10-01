package com.example.soap.Features.Feed.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
    val spacing = 12.dp
    val minW = 100.dp

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val parentWidth = maxWidth
        //TODO - 이미지 가로 폭 줄이기
        val maxW = parentWidth - hPadding * 2
        val height = maxW * 3 / 4

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(horizontal = hPadding),
            modifier = Modifier.height(height)
        ) {
            items(images) { item ->
                SubcomposeAsyncImage(
                    model = item.url,
                    contentDescription = null
                ) {
                    when (val state = painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            Placeholder(width = minW, height = height)
                        }

                        is AsyncImagePainter.State.Error -> {
                            Placeholder(
                                width = minW,
                                height = height,
                                systemImage = Icons.Default.Warning
                            )
                        }

                        is AsyncImagePainter.State.Success -> {
                            val size = state.painter.intrinsicSize
                            val aspect = if (size.height > 0) size.width / size.height else 16f / 9f
                            val fitWidth = height * aspect
                            val clampedWidth = fitWidth.coerceIn(minW, maxW)

                            SubcomposeAsyncImageContent(
                                modifier = Modifier
                                    .height(height)
                                    .width(clampedWidth)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = if (fitWidth in minW..maxW) ContentScale.Fit else ContentScale.Crop
                            )
                        }

                        else -> {}
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
