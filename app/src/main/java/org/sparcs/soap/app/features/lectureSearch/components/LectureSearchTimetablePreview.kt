package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
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
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.features.timetable.components.TimetableSilhouetteView
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TimetableOnboarding
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
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
    visible: Boolean = true,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(previewSpring(), expandFrom = Alignment.Top) +
            scaleIn(previewSpring(), initialScale = 0.85f, transformOrigin = TransformOrigin(1f, 0f)) +
            fadeIn(),
        exit = shrinkVertically(previewSpring(), shrinkTowards = Alignment.Top) +
            scaleOut(previewSpring(), targetScale = 0.85f, transformOrigin = TransformOrigin(1f, 0f)) +
            fadeOut(),
    ) {
        PreviewCard(viewModel, height, maxHeight, onHeightChange, modifier, resizable)
    }
}

private fun <T> previewSpring() = spring<T>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

@Composable
private fun PreviewCard(
    viewModel: TimetableViewModelProtocol,
    height: Dp,
    maxHeight: Dp,
    onHeightChange: (Dp) -> Unit,
    modifier: Modifier,
    resizable: Boolean,
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
    val tipStore = LocalTipStore.current
    val resize: (Dp) -> Unit = {
        tipStore?.invalidate(BuddyTip.ResizePreview)
        onHeightChange(it)
    }
    LaunchedEffect(resizable) { if (!resizable) tipStore?.completeOnboarding(TimetableOnboarding.Step.ResizePreview) }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // The selected timetable theme's background, as on the timetable screen. Elevated while it
        // floats over the results in its small size.
        val timetableTheme = LocalTimetableTheme.current
        ElevatedCard(
            modifier = Modifier.height(displayedHeight)
                .then(if (compact) Modifier.width(displayedHeight * 0.82f) else Modifier.fillMaxWidth()),
            colors = CardDefaults.elevatedCardColors(
                containerColor = timetableTheme.backgroundColor ?: MaterialTheme.colorScheme.background
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (compact) 6.dp else 0.dp),
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
                            setProgress { value -> resize(value.dp.coerceIn(minimum, maxHeight)); true }
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            onDragStarted = { dragHeight = displayedHeight.value },
                            state = rememberDraggableState { delta ->
                                dragHeight = (dragHeight + with(density) { delta.toDp().value }).coerceIn(minimum.value, maxHeight.value)
                                resize(dragHeight.dp)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Sized like the Material bottom sheet drag handle, in the theme's label colour so it shows on its background.
                    Box(Modifier.size(32.dp, 4.dp).background((timetableTheme.gridLabelColor ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.4f), MaterialTheme.shapes.extraLarge))
                }
            }
        }
        TipView(
            BuddyTip.ResizePreview,
            enabled = resizable,
            modifier = if (compact) Modifier.width(maxOf(displayedHeight * 0.82f, 240.dp)) else Modifier,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetablePreview() {
    Theme { LectureSearchTimetablePreview(PreviewTimetableViewModel(), 240.dp, 640.dp, {}) }
}

// A timetable theme with its own dark background, to check the card follows it.
private val nightTheme = TimetableTheme(
    id = "preview.night",
    name = "Night",
    hexColors = listOf("5E81AC", "BF616A", "A3BE8C", "EBCB8B", "B48EAD"),
    textColorHex = "FFFFFF",
    separatorColorHex = "3B4252",
    backgroundColorHex = "1E222A",
)

@Preview(showBackground = true)
@Composable
private fun ThemedTimetablePreview() {
    Theme {
        CompositionLocalProvider(LocalTimetableTheme provides nightTheme) {
            Column {
                LectureSearchTimetablePreview(PreviewTimetableViewModel(), 400.dp, 640.dp, {})
                LectureSearchTimetablePreview(PreviewTimetableViewModel(), 180.dp, 640.dp, {})
            }
        }
    }
}
