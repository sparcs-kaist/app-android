package org.sparcs.soap.app.features.fullscreenImage

internal object ViewerPaging {
    fun clampedIndex(index: Int, count: Int): Int = if (count <= 0) 0 else index.coerceIn(0, count - 1)

    fun showsCounter(count: Int): Boolean = count > 1
}
