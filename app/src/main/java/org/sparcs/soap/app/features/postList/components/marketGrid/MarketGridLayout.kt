package org.sparcs.soap.app.features.postList.components.marketGrid

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

object MarketGridLayout {
    const val MARGIN = 16f
    const val SPACING = 12f
    const val ROW_SPACING = 20f
    private const val IDEAL_WIDTH = 180f
    private const val TEXT_HEIGHT = 60f

    fun columnCount(width: Float): Int =
        max(2, ((available(width) + SPACING) / (IDEAL_WIDTH + SPACING)).roundToInt())

    fun columnWidth(width: Float): Float {
        val count = columnCount(width)
        return (available(width) - (count - 1) * SPACING) / count
    }

    fun placeholderCount(width: Float, height: Float): Int {
        val rowHeight = columnWidth(width) + TEXT_HEIGHT + ROW_SPACING
        val rows = max(1, ceil(height / rowHeight).toInt())
        return rows * columnCount(width)
    }

    private fun available(width: Float) = max(width - MARGIN * 2, 1f)
}
