package org.sparcs.soap.app.features.fullscreenImage

import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

internal object DismissDrag {
    const val DISTANCE_DP = 120f
    const val FLING_VELOCITY_DP = 1000f
    private const val SHRINK_DISTANCE_DP = 1500f
    private const val MAX_SHRINK = 0.25f
    private const val FADE_DISTANCE_DP = 400f
    private const val MAX_FADE = 0.8f

    fun isDismissDirection(delta: Offset): Boolean = delta.y > 0f && delta.y > abs(delta.x)

    fun shouldDismiss(dragYDp: Float, velocityYDp: Float): Boolean =
        dragYDp > DISTANCE_DP || (dragYDp > 0f && velocityYDp > FLING_VELOCITY_DP)

    fun contentScale(dragYDp: Float): Float =
        1f - (dragYDp.coerceAtLeast(0f) / SHRINK_DISTANCE_DP).coerceAtMost(MAX_SHRINK)

    fun backdropAlpha(dragYDp: Float): Float =
        1f - (dragYDp.coerceAtLeast(0f) / FADE_DISTANCE_DP).coerceAtMost(MAX_FADE)
}

@Stable
internal class DismissDragState(private val scope: CoroutineScope) {
    var offset by mutableStateOf(Offset.Zero)
        private set

    val isDragging: Boolean get() = offset != Offset.Zero

    private var springBack: Job? = null

    fun drag(delta: Offset) {
        springBack?.cancel()
        offset += delta
    }

    fun release() {
        springBack?.cancel()
        springBack = scope.launch {
            animate(Offset.VectorConverter, offset, Offset.Zero, animationSpec = spring()) { value, _ ->
                offset = value
            }
        }
    }
}

internal fun Modifier.dismissDrag(
    state: DismissDragState,
    canStart: () -> Boolean,
    onDismiss: () -> Unit,
): Modifier = pointerInput(state) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (!canStart() || !awaitDismissSlop(down.id)) return@awaitEachGesture
        val tracker = VelocityTracker()
        val completed = trackDrag(down.id) { change ->
            state.drag(change.positionChange())
            tracker.addPointerInputChange(change)
        }
        val dragYDp = state.offset.y.toDp().value
        val velocityYDp = tracker.calculateVelocity().y.toDp().value
        if (completed && DismissDrag.shouldDismiss(dragYDp, velocityYDp)) onDismiss() else state.release()
    }
}

private suspend fun AwaitPointerEventScope.awaitDismissSlop(pointerId: PointerId): Boolean {
    var total = Offset.Zero
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == pointerId } ?: return false
        if (!change.pressed || change.isConsumed || event.changes.count { it.pressed } > 1) return false
        total += change.positionChange()
        if (total.getDistance() > viewConfiguration.touchSlop) {
            if (!DismissDrag.isDismissDirection(total)) return false
            change.consume()
            return true
        }
    }
}

private suspend fun AwaitPointerEventScope.trackDrag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): Boolean {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        val change = event.changes.firstOrNull { it.id == pointerId } ?: return false
        if (!change.pressed) return true
        onDrag(change)
        event.changes.forEach { it.consume() }
    }
}
