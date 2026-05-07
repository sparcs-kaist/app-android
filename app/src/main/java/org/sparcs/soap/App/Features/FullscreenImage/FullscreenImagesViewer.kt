package org.sparcs.soap.App.Features.FullscreenImage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.sparcs.soap.App.Domain.Models.Feed.FeedImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullscreenImagesViewer(
    images: List<FeedImage>,
    startIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
        pageCount = { images.size }
    )

    val scales = remember(images) { mutableStateListOf<Float>().apply { images.forEach { _ -> add(1f) } } }
    val offsets = remember(images) { mutableStateListOf<Offset>().apply { images.forEach { _ -> add(Offset.Zero) } } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val currentScale = scales.getOrNull(pagerState.currentPage) ?: 1f

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = currentScale <= 1f,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val image = images[page]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(page) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)

                            while (true) {
                                val event = awaitPointerEvent()
                                val pressedCount = event.changes.count { it.pressed }

                                when {
                                    pressedCount >= 2 -> {
                                        val zoomChange = event.calculateZoom()
                                        val panChange = event.calculatePan()
                                        val oldScale = scales[page]
                                        val newScale = (oldScale * zoomChange).coerceIn(1f, 5f)
                                        scales[page] = newScale

                                        offsets[page] = if (newScale <= 1f) {
                                            Offset.Zero
                                        } else {
                                            offsets[page] + panChange
                                        }

                                        event.changes.forEach { it.consumeAllChanges() }
                                    }

                                    scales[page] > 1f && pressedCount == 1 -> {
                                        val change = event.changes.first()
                                        val panChange = change.positionChange()
                                        if (panChange != Offset.Zero) {
                                            offsets[page] = offsets[page] + panChange
                                            change.consumeAllChanges()
                                        }
                                    }
                                }

                                if (event.changes.all { it.changedToUpIgnoreConsumed() }) break
                            }
                        }
                    }
                    .pointerInput(page) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scales[page] > 1f) {
                                    scales[page] = 1f
                                    offsets[page] = Offset.Zero
                                } else {
                                    scales[page] = 2f
                                }
                            },
                            onTap = {}
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = image.url,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scales[page]
                            scaleY = scales[page]
                            translationX = offsets[page].x
                            translationY = offsets[page].y
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "${pagerState.currentPage + 1}/${images.size}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, end = 12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}







