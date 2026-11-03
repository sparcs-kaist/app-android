package org.sparcs.soap.app.features.fullscreenImage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import org.sparcs.soap.app.features.fullscreenImage.ImageZoomState
import org.sparcs.soap.app.features.fullscreenImage.SafeInsets
import org.sparcs.soap.app.features.fullscreenImage.zoomGestures
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun ZoomableImagePage(
    url: String,
    zoomState: ImageZoomState,
    onLoadedChange: (Boolean) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var attempt by remember(url) { mutableIntStateOf(0) }
    val safeInsets = rememberSafeInsets()
    SideEffect { zoomState.updateSafeInsets(safeInsets) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { zoomState.updateContainerSize(it.toSize()) }
    ) {
        key(attempt) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) } },
                error = { Box(contentAlignment = Alignment.Center) { ImageLoadError(onRetry = { attempt++ }) } },
                success = {
                    SubcomposeAsyncImageContent(
                        modifier = Modifier
                            .zoomGestures(zoomState)
                            .pointerInput(zoomState) {
                                detectTapGestures(
                                    onDoubleTap = { zoomState.toggle(it) },
                                    onLongPress = { onLongPress() }
                                )
                            }
                            .graphicsLayer {
                                scaleX = zoomState.scale
                                scaleY = zoomState.scale
                                translationX = zoomState.offset.x
                                translationY = zoomState.offset.y
                            }
                    )
                },
                onLoading = { onLoadedChange(false) },
                onError = { onLoadedChange(false) },
                onSuccess = { state ->
                    zoomState.updateImageSize(state.painter.intrinsicSize)
                    onLoadedChange(true)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSafeInsets(): SafeInsets {
    val insets = WindowInsets.safeDrawing
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val topBarPx = with(density) { TopAppBarDefaults.TopAppBarExpandedHeight.toPx() }
    return SafeInsets(
        left = insets.getLeft(density, direction).toFloat(),
        top = insets.getTop(density) + topBarPx,
        right = insets.getRight(density, direction).toFloat(),
        bottom = insets.getBottom(density).toFloat()
    )
}

@Preview
@Composable
private fun ZoomableImagePagePreview() {
    val scope = rememberCoroutineScope()
    Theme {
        ZoomableImagePage(
            url = "https://sparcs.org/img/1.png",
            zoomState = remember { ImageZoomState(scope) },
            onLoadedChange = {},
            onLongPress = {},
            modifier = Modifier.background(Color.Black)
        )
    }
}
