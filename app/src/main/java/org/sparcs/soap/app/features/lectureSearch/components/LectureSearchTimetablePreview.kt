package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.features.timetable.components.TimetableSilhouetteView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun LectureSearchTimetablePreview(
    viewModel: TimetableViewModelProtocol,
    height: Dp,
    maxHeight: Dp,
    onHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    resizable: Boolean = true,
) {
    val table by viewModel.selectedTimetable.collectAsState()
    val candidate by viewModel.candidateLecture.collectAsState()
    val timetableWithCandidate = table?.let { timetable ->
        candidate?.takeUnless(timetable::contains)?.let { timetable.copy(lectures = timetable.lectures + it) } ?: timetable
    }
    val density = LocalDensity.current
    val description = stringResource(R.string.timetable_preview_height)
    val minimum = minOf(120.dp, maxHeight)
    val displayedHeight = height.coerceIn(minimum, maxHeight)
    val compact = resizable && displayedHeight < 260.dp
    var dragHeight by remember { mutableFloatStateOf(displayedHeight.value) }
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
        Card(
            modifier = Modifier.height(displayedHeight)
                .then(if (compact) Modifier.width(displayedHeight * 0.82f) else Modifier.fillMaxWidth()),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 8.dp else 0.dp),
        ) {
            Column {
                Box(Modifier.weight(1f).padding(8.dp)) {
                    if (compact) TimetableSilhouetteView(timetableWithCandidate, Modifier.fillMaxSize(), candidate?.id)
                    else TimetableGrid(timetable = table, candidateLecture = candidate)
                }
                if (resizable) Box(
                    Modifier.fillMaxWidth().height(32.dp)
                        .semantics {
                            contentDescription = description
                            progressBarRangeInfo = ProgressBarRangeInfo(displayedHeight.value, minimum.value..maxHeight.value)
                            setProgress { value -> onHeightChange(value.dp.coerceIn(minimum, maxHeight)); true }
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            onDragStarted = { dragHeight = displayedHeight.value },
                            state = rememberDraggableState { delta ->
                                dragHeight = (dragHeight + with(density) { delta.toDp().value }).coerceIn(minimum.value, maxHeight.value)
                                onHeightChange(dragHeight.dp)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(36.dp, 4.dp).background(MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetablePreview() {
    Theme { LectureSearchTimetablePreview(PreviewTimetableViewModel(), 240.dp, 640.dp, {}) }
}
