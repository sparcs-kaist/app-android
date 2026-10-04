package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

@Composable
internal fun LectureSearchResultsLayout(
    modifier: Modifier = Modifier,
    floatingPreview: Boolean,
    header: @Composable () -> Unit,
    results: @Composable () -> Unit,
    preview: (@Composable () -> Unit)?,
) {
    Layout(
        modifier = modifier,
        content = {
            Box { header() }
            Box(Modifier.fillMaxSize()) { results() }
            Box { preview?.invoke() }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0)
        val headerPlaceable = measurables[0].measure(loose)
        val previewPlaceable = measurables[2].measure(
            loose.copy(maxHeight = (constraints.maxHeight - headerPlaceable.height).coerceAtLeast(0))
        )
        val reservedHeight = if (floatingPreview) 0 else previewPlaceable.height
        val resultsHeight = (constraints.maxHeight - headerPlaceable.height - reservedHeight).coerceAtLeast(0)
        val resultsPlaceable = measurables[1].measure(
            constraints.copy(minHeight = resultsHeight, maxHeight = resultsHeight)
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            headerPlaceable.placeRelative(0, reservedHeight)
            resultsPlaceable.placeRelative(0, reservedHeight + headerPlaceable.height)
            previewPlaceable.placeRelative(0, if (floatingPreview) headerPlaceable.height else 0, zIndex = 1f)
        }
    }
}
