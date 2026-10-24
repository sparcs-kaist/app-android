package org.sparcs.soap.app.features.course.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.features.lectureSearch.components.LectureSearchNavigationBar
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.theme.ui.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseNavigationBar(
    navController: NavController,
    text: String,
    isSearchContext: Boolean = false,
    navigationActions: @Composable RowScope.() -> Unit = {},
) {
    if (isSearchContext) {
        LectureSearchNavigationBar(text, { navController.popBackStack() }, navigationActions)
        return
    }

    TopAppBar(
        navigationIcon = {
            BackButton { navController.popBackStack() }
        },
        title = {
            Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { CourseNavigationBar(rememberNavController(), "title") }
}
