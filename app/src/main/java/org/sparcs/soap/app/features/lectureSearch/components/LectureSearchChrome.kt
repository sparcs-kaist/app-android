package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.features.navigationBar.components.DismissButton
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

@Composable
fun LectureSearchNavigationBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        DismissButton { onBack() }
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        actions()
    }
}
@Preview(showBackground = true)
@Composable
private fun NavigationPreview() { Theme { LectureSearchNavigationBar("Timetable", {}) } }
