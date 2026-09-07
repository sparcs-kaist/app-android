package org.sparcs.soap.app.features.courseCompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun TimetablePreviewSection(
    modifier: Modifier = Modifier,
    viewModel: TimetableViewModelProtocol,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        TimetableGrid(
            viewModel = viewModel,
            onLectureSelected = {},
            showDeleteDialog = {}
        )
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