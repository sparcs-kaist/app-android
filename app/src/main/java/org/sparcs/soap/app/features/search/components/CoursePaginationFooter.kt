package org.sparcs.soap.app.features.search.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun CoursePaginationFooter(hasMoreCourses: Boolean, isLoadingMoreCourses: Boolean, error: Exception?, courseCount: Int, onLoadMore: () -> Unit) {
    LaunchedEffect(courseCount, hasMoreCourses) {
        if (hasMoreCourses && !isLoadingMoreCourses && error == null) onLoadMore()
    }
    if (isLoadingMoreCourses) CircularProgressIndicator()
    else if (hasMoreCourses) TextButton(onClick = onLoadMore) {
        Text(stringResource(if (error == null) R.string.lecture_load_more else R.string.retry))
    }
}

@Preview(showBackground = true)
@Composable
private fun CoursePaginationFooterPreview() {
    Theme { CoursePaginationFooter(true, false, null, 150, {}) }
}
