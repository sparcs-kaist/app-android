package org.sparcs.soap.app.features.courseCompose.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.features.navigationBar.components.CloseButton
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseComposeTopBar(
    title: String,
    onClose: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            CloseButton(onClick = onClose)
        },
        actions = actions
    )
}

@Preview
@Composable
private fun CourseComposeTopBarPreview() {
    Theme {
        CourseComposeTopBar(
            title = "title",
            onClose = {},
        )
    }
}
