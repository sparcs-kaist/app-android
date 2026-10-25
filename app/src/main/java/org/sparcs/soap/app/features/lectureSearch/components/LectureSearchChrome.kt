package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureSearchNavigationBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        navigationIcon = { BackButton { onBack() } },
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        actions = actions
    )
}
@Preview(showBackground = true)
@Composable
private fun NavigationPreview() { Theme { LectureSearchNavigationBar("Timetable", {}) } }
