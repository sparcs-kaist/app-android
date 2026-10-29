package org.sparcs.soap.app.features.fullscreenImage

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.min

internal data class SafeInsets(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun areaIn(size: Size): Rect = Rect(left, top, size.width - right, size.height - bottom)

    companion object {
        val Zero = SafeInsets(0f, 0f, 0f, 0f)
    }
}

internal object ImageZoomMath {
    const val MIN_GESTURE_SCALE = 0.5f
    const val MAX_SCALE = 5f
    const val DOUBLE_TAP_SCALE = 2.5f
    private const val ZOOM_EPSILON = 0.01f

    fun isZoomed(scale: Float): Boolean = scale > 1f + ZOOM_EPSILON

    fun gestureScale(scale: Float): Float = scale.coerceIn(MIN_GESTURE_SCALE, MAX_SCALE)

    fun safeFitScale(content: Size, safeArea: Rect): Float {
        if (content.width <= 0f || content.height <= 0f || safeArea.isEmpty) return 1f
        return min(safeArea.width / content.width, safeArea.height / content.height).coerceAtMost(1f)
    }

    fun settledScale(scale: Float, startScale: Float, safeFit: Float): Float = when {
        scale >= 1f -> scale.coerceAtMost(MAX_SCALE)
        safeFit < 1f && scale < startScale -> safeFit
        else -> 1f
    }

    fun safeFitOffset(scale: Float, safeFit: Float, safeArea: Rect, container: Size): Offset {
        if (safeFit >= 1f || scale > safeFit || safeArea.isEmpty) return Offset.Zero
        return safeArea.center - Offset(container.width / 2f, container.height / 2f)
    }

    fun fittedSize(intrinsic: Size, container: Size): Size {
        if (intrinsic == Size.Unspecified || intrinsic.width <= 0f || intrinsic.height <= 0f) return container
        if (container.width <= 0f || container.height <= 0f) return Size.Zero
        val ratio = min(container.width / intrinsic.width, container.height / intrinsic.height)
        return Size(intrinsic.width * ratio, intrinsic.height * ratio)
    }

    fun anchoredOffset(offset: Offset, oldScale: Float, newScale: Float, anchor: Offset): Offset {
        if (oldScale <= 0f) return offset
        return anchor - (anchor - offset) * (newScale / oldScale)
    }

    fun clampOffset(offset: Offset, scale: Float, content: Size, container: Size): Offset {
        val maxX = max(0f, (content.width * scale - container.width) / 2f)
        val maxY = max(0f, (content.height * scale - container.height) / 2f)
        return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }
}
