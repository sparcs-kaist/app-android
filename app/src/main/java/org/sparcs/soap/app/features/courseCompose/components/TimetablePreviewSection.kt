package org.sparcs.soap.app.features.courseCompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.helpers.TimetableConstructor
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun TimetablePreviewSection(
    modifier: Modifier = Modifier,
    viewModel: TimetableViewModelProtocol,
    isScrollable: Boolean = false
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val timetable by viewModel.selectedTimetable.collectAsState()
    val candidateLecture by viewModel.candidateLecture.collectAsState()
    
    LaunchedEffect(candidateLecture) {
        if (isScrollable && candidateLecture != null) {
            val beginTime = candidateLecture?.classes?.minOfOrNull { it.begin } ?: return@LaunchedEffect
            
            val times = buildList {
                timetable?.lectures?.forEach { addAll(it.classes) }
                candidateLecture?.let { addAll(it.classes) }
            }

            val minMinutes = times.minOfOrNull { it.begin }?.let { (it / 60) * 60 } ?: 540
            val maxMinutes = times.maxOfOrNull { it.end }?.let { ((it / 60) + 1) * 60 } ?: 1080
            val duration = maxMinutes - minMinutes

            if (duration > 0) {
                val containerHeightDp = 1200.dp
                val containerHeightPx = with(density) { containerHeightDp.toPx() }
                val daysHeightPx = with(density) { TimetableConstructor.daysHeight.toPx() }
                
                val timetableHeight = containerHeightPx - (daysHeightPx + 24f) - 14f
                val difference = (timetableHeight / duration.toFloat()) * (beginTime - minMinutes).toFloat()
                
                val targetOffsetPx = daysHeightPx + 14f + difference
                val targetScrollPx = targetOffsetPx - with(density) { 80.dp.toPx() }
                
                scrollState.animateScrollTo(targetScrollPx.coerceAtLeast(0f).toInt())
            }
        }
    }

    val background = LocalTimetableTheme.current.backgroundColor
    Column(
        modifier = modifier
            .then(if (background != null) Modifier.background(background) else Modifier)
            .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isScrollable) 1200.dp else 400.dp)
        ) {
            TimetableGrid(
                viewModel = viewModel,
                onLectureSelected = {},
                showDeleteDialog = {}
            )
        }
    }
}

@Composable
@Preview
private fun TimetablePreviewSectionPreview() {
    Theme {
        TimetablePreviewSection(
            viewModel = PreviewTimetableViewModel()
        )
    }
}
