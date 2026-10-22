package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun LectureSearchResultsLayout(
    modifier: Modifier = Modifier,
    floatingPreview: Boolean,
    header: @Composable () -> Unit,
    results: @Composable (topInset: Dp) -> Unit,
    preview: (@Composable () -> Unit)?,
) {
    SubcomposeLayout(modifier) { constraints ->
        val loose = constraints.copy(minHeight = 0)
        val headerPlaceable = subcompose("header") { Box { header() } }.first().measure(loose)
        val previewPlaceable = subcompose("preview") { Box { preview?.invoke() } }.first().measure(
            loose.copy(maxHeight = (constraints.maxHeight - headerPlaceable.height).coerceAtLeast(0))
        )
        val reservedHeight = if (floatingPreview) 0 else previewPlaceable.height
        val topInset = if (floatingPreview) previewPlaceable.height.toDp() else 0.dp
        val resultsHeight = (constraints.maxHeight - headerPlaceable.height - reservedHeight).coerceAtLeast(0)
        val resultsPlaceable = subcompose("results") { Box(Modifier.fillMaxSize()) { results(topInset) } }.first()
            .measure(constraints.copy(minHeight = resultsHeight, maxHeight = resultsHeight))
        layout(constraints.maxWidth, constraints.maxHeight) {
            headerPlaceable.placeRelative(0, reservedHeight)
            resultsPlaceable.placeRelative(0, reservedHeight + headerPlaceable.height)
            previewPlaceable.placeRelative(0, if (floatingPreview) headerPlaceable.height else 0, zIndex = 1f)
        }
    }
}
