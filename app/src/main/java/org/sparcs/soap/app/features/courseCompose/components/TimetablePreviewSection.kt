package org.sparcs.soap.app.features.courseCompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableGrid

@Composable
fun TimetablePreviewSection(
    modifier: Modifier = Modifier,
    viewModel: TimetableViewModelProtocol,
    candidateLecture: Lecture?,
    isOverlapping: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        TimetableGrid(
            viewModel = viewModel,
            onLectureSelected = { /* Preview 모드에서는 선택 비활성화 또는 정보 조회 */ },
            showDeleteDialog = { /* Preview 모드에서는 삭제 비활성화 */ }
        )
    }
}
