package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun LectureSearchPagination(state: LectureSearchViewModel.PaginationState, courseCount: Int, onLoadMore: () -> Unit) {
    LaunchedEffect(courseCount, state) {
        if (state is LectureSearchViewModel.PaginationState.Idle && state.hasMore) onLoadMore()
    }
    when (state) {
        LectureSearchViewModel.PaginationState.Loading -> LectureSearchSkeletonCard()
        is LectureSearchViewModel.PaginationState.Error -> TextButton(onClick = onLoadMore) {
            Text(stringResource(R.string.lecture_load_more))
        }
        is LectureSearchViewModel.PaginationState.Idle -> if (state.hasMore) {
            TextButton(onClick = onLoadMore) { Text(stringResource(R.string.lecture_load_more)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureSearchPaginationPreview() {
    Theme { LectureSearchPagination(LectureSearchViewModel.PaginationState.Idle(hasMore = true), 1, {}) }
}
