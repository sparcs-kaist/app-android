package org.sparcs.soap.app.features.feed

import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.sparcs.soap.app.features.navigationBar.usesNavigationRail

data class FeedSplit(
    val feedWidth: Dp?,
    val gutter: Dp,
    val panelWidth: Dp,
)

fun feedSplit(
    width: Dp,
    verticalFold: ClosedRange<Dp>? = null,
    hasHorizontalFold: Boolean = false,
): FeedSplit? {
    if (verticalFold != null) return foldSplit(width, verticalFold)
    if (hasHorizontalFold) return null

    val availableWidth = width - FEED_MIN_WIDTH
    if (availableWidth < PANEL_MIN_WIDTH) return null
    return FeedSplit(feedWidth = null, gutter = 0.dp, panelWidth = min(PANEL_MAX_WIDTH, availableWidth))
}

private fun foldSplit(width: Dp, fold: ClosedRange<Dp>): FeedSplit? {
    val leadingWidth = fold.start
    val trailingWidth = width - fold.endInclusive
    if (leadingWidth < FOLD_MIN_COLUMN_WIDTH || trailingWidth < FOLD_MIN_COLUMN_WIDTH) return null
    return FeedSplit(
        feedWidth = leadingWidth,
        gutter = fold.endInclusive - fold.start,
        panelWidth = trailingWidth,
    )
}

@Composable
fun rememberFeedSplit(): FeedSplit? {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val windowSize = currentWindowSize()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val railWidth = if (usesNavigationRail(adaptiveInfo)) NAVIGATION_RAIL_WIDTH else 0.dp
    val feedAreaWidth = with(density) { windowSize.width.toDp() } - railWidth
    val hinges = adaptiveInfo.windowPosture.hingeList

    return feedSplit(
        width = feedAreaWidth,
        verticalFold = hinges.firstOrNull { it.isVertical }
            ?.startRelativeSpan(density, layoutDirection, railWidth, feedAreaWidth),
        hasHorizontalFold = hinges.any { !it.isVertical },
    )
}

private fun HingeInfo.startRelativeSpan(
    density: Density,
    layoutDirection: LayoutDirection,
    railWidth: Dp,
    feedAreaWidth: Dp,
): ClosedRange<Dp> = with(density) {
    val left = bounds.left.toDp()
    val right = bounds.right.toDp()
    when (layoutDirection) {
        LayoutDirection.Ltr -> (left - railWidth)..(right - railWidth)
        LayoutDirection.Rtl -> (feedAreaWidth - right)..(feedAreaWidth - left)
    }
}

private val FEED_MIN_WIDTH = 360.dp
private val PANEL_MIN_WIDTH = 320.dp
private val PANEL_MAX_WIDTH = 400.dp
private val FOLD_MIN_COLUMN_WIDTH = 240.dp
private val NAVIGATION_RAIL_WIDTH = 80.dp
