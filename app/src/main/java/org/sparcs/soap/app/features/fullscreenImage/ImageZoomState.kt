package org.sparcs.soap.app.features.fullscreenImage

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Stable
internal class ImageZoomState(private val scope: CoroutineScope) {
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    val isZoomed: Boolean get() = ImageZoomMath.isZoomed(scale)

    private var containerSize = Size.Zero
    private var imageSize = Size.Unspecified
    private var safeInsets = SafeInsets.Zero
    private var gestureStartScale = 1f
    private var animation: Job? = null

    private val contentSize: Size get() = ImageZoomMath.fittedSize(imageSize, containerSize)
    private val safeArea: Rect get() = safeInsets.areaIn(containerSize)
    private val center: Offset get() = Offset(containerSize.width / 2f, containerSize.height / 2f)

    fun updateContainerSize(size: Size) {
        if (size == containerSize) return
        val resized = containerSize != Size.Zero
        containerSize = size
        if (resized) reset()
    }

    fun updateImageSize(size: Size) {
        imageSize = size
    }

    fun updateSafeInsets(insets: SafeInsets) {
        safeInsets = insets
    }

    fun beginGesture() {
        gestureStartScale = scale
    }

    fun transform(centroid: Offset, pan: Offset, zoom: Float) {
        animation?.cancel()
        val newScale = ImageZoomMath.gestureScale(scale * zoom)
        val anchored = ImageZoomMath.anchoredOffset(offset, scale, newScale, centroid - center)
        offset = clamp(anchored + pan, newScale)
        scale = newScale
    }

    fun pan(delta: Offset) {
        animation?.cancel()
        offset = clamp(offset + delta, scale)
    }

    fun settle() {
        val safeFit = ImageZoomMath.safeFitScale(contentSize, safeArea)
        val target = ImageZoomMath.settledScale(scale, gestureStartScale, safeFit)
        val targetOffset = if (target < 1f) {
            ImageZoomMath.safeFitOffset(target, safeFit, safeArea, containerSize)
        } else {
            clamp(offset, target)
        }
        animateTo(target, targetOffset)
    }

    fun toggle(tap: Offset) {
        if (scale != 1f) {
            animateTo(1f, Offset.Zero)
            return
        }
        val target = ImageZoomMath.DOUBLE_TAP_SCALE
        animateTo(target, clamp(ImageZoomMath.anchoredOffset(Offset.Zero, 1f, target, tap - center), target))
    }

    fun reset() {
        animation?.cancel()
        scale = 1f
        offset = Offset.Zero
    }

    private fun clamp(value: Offset, scale: Float): Offset =
        ImageZoomMath.clampOffset(value, scale, contentSize, containerSize)

    private fun animateTo(targetScale: Float, targetOffset: Offset) {
        animation?.cancel()
        val startScale = scale
        val startOffset = offset
        animation = scope.launch {
            animate(0f, 1f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { fraction, _ ->
                scale = startScale + (targetScale - startScale) * fraction
                offset = startOffset + (targetOffset - startOffset) * fraction
            }
        }
    }
}

internal fun Modifier.zoomGestures(state: ImageZoomState): Modifier = pointerInput(state) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        state.beginGesture()
        var transformed = false
        do {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.size >= 2) {
                state.transform(event.calculateCentroid(), event.calculatePan(), event.calculateZoom())
                event.changes.forEach { it.consume() }
                transformed = true
            } else if (pressed.size == 1 && state.isZoomed) {
                val change = pressed.first()
                val delta = change.positionChange()
                if (delta != Offset.Zero) {
                    state.pan(delta)
                    change.consume()
                    transformed = true
                }
            }
        } while (event.changes.any { it.pressed })
        if (transformed) state.settle()
    }
}
