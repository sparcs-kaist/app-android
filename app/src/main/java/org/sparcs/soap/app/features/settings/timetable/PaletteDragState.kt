package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

internal class PaletteDragState(
    private val listState: LazyListState,
    private val colorIds: () -> List<String>,
    private val onMove: (String, String) -> Unit,
) {
    var draggedId by mutableStateOf<String?>(null)
        private set
    private var top by mutableFloatStateOf(0f)
    private var height = 0
    private var expectedIndex: Int? = null

    fun start(id: String) {
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id } ?: return
        draggedId = id
        top = item.offset.toFloat()
        height = item.size
        expectedIndex = null
    }

    fun dragBy(distance: Float) {
        top += distance
        moveIfNeeded()
    }

    fun stop() {
        draggedId = null
        expectedIndex = null
    }

    fun translation(id: String): Float {
        if (draggedId != id) return 0f
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id } ?: return 0f
        return top - item.offset
    }

    suspend fun scrollAtEdge(edgeSize: Float) {
        val ids = colorIds()
        val index = ids.indexOf(draggedId)
        if (index < 0) return
        val layout = listState.layoutInfo
        val center = top + height / 2f
        val scroll = when {
            center < layout.viewportStartOffset + edgeSize ->
                -((layout.viewportStartOffset + edgeSize - center) / edgeSize).coerceIn(0f, 1f)
            center > layout.viewportEndOffset - edgeSize ->
                ((center - layout.viewportEndOffset + edgeSize) / edgeSize).coerceIn(0f, 1f)
            else -> 0f
        }
        val canMove = if (scroll < 0f) index > 0 else index < ids.lastIndex
        if (scroll != 0f && canMove) {
            listState.scrollBy(scroll * edgeSize / 6f)
            moveIfNeeded()
        }
    }

    private fun moveIfNeeded() {
        val id = draggedId ?: return
        val items = listState.layoutInfo.visibleItemsInfo
        val dragged = items.firstOrNull { it.key == id } ?: return
        if (expectedIndex != null && dragged.index != expectedIndex) return
        expectedIndex = null
        val center = top + height / 2f
        val target = items.firstOrNull {
            it.key in colorIds() && it.key != id && center >= it.offset && center <= it.offset + it.size
        } ?: return
        expectedIndex = target.index
        onMove(id, target.key as String)
    }
}

@Composable
internal fun rememberPaletteDragState(
    listState: LazyListState,
    colorIds: List<String>,
    onMove: (String, String) -> Unit,
): PaletteDragState {
    val move by rememberUpdatedState(onMove)
    val ids by rememberUpdatedState(colorIds)
    val state = remember(listState) {
        PaletteDragState(listState, { ids }) { source, target -> move(source, target) }
    }
    val edgeSize = with(LocalDensity.current) { 64.dp.toPx() }
    LaunchedEffect(state.draggedId) {
        while (state.draggedId != null) {
            withFrameNanos { }
            state.scrollAtEdge(edgeSize)
        }
    }
    return state
}
