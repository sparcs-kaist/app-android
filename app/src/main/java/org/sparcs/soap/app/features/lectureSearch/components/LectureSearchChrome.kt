package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.theme.ui.Theme

object LectureSearchChrome {
    val background: Color
        @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF101012) else Color(0xFFF2F2F7)
    val card: Color
        @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF1C1C1E) else Color.White
    val secondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
    val separator: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureSearchNavigationBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        navigationIcon = { BackButton { onBack() } },
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LectureSearchChrome.background
        )
    )
}
@Preview(showBackground = true)
@Composable
private fun NavigationPreview() { Theme { LectureSearchNavigationBar("Timetable", {}) } }
