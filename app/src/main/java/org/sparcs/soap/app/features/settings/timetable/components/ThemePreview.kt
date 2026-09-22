package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.settings.timetable.rememberThemeSample
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
internal fun ThemePreview(theme: TimetableTheme, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.theme_preview)
    val items = rememberThemeSample()
    val viewModel = remember(items) {
        val timetable = Timetable(
            id = "theme-preview",
            lectures = items.map { it.lecture }.distinctBy { it.courseID }
        )
        object : TimetableViewModelProtocol by PreviewTimetableViewModel(timetable) {
            override val isEditable = MutableStateFlow(false).asStateFlow()
        }
    }
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    CompositionLocalProvider(LocalTimetableTheme provides theme) {
        Surface(
            modifier = modifier
                .glassBorder(shape = MaterialTheme.shapes.large)
                .semantics { contentDescription = description },
            shape = MaterialTheme.shapes.large,
            color = theme.backgroundColor ?: MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp * fontScale)
                    .padding(8.dp)
            ) {
                TimetableGrid(viewModel = viewModel, showDeleteDialog = {})
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, locale = "en")
@Preview(showBackground = true, widthDp = 320, locale = "ko", fontScale = 1.5f)
@Preview(showBackground = true, widthDp = 420, locale = "en")
@Preview(showBackground = true, widthDp = 420, locale = "ko")
@Composable
private fun ThemePreviewSample() {
    Theme {
        ThemePreview(TimetableTheme.Default)
    }
}
