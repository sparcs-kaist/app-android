package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureClass
import org.sparcs.soap.app.domain.models.otl.LectureItem
import org.sparcs.soap.app.features.timetable.components.TimetableGridCell
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun ThemePreview(theme: TimetableTheme) {
    val label = theme.gridLabelColor ?: MaterialTheme.colorScheme.onSurface
    val line = theme.separatorColor ?: MaterialTheme.colorScheme.outlineVariant
    val description = stringResource(R.string.theme_preview)
    val titles = listOf(
        R.string.theme_sample_1,
        R.string.theme_sample_2,
        R.string.theme_sample_3,
        R.string.theme_sample_4
    ).map { stringResource(it) }
    val lecture = remember { Lecture.mock() }
    CompositionLocalProvider(LocalTimetableTheme provides theme) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = theme.backgroundColor ?: MaterialTheme.colorScheme.background,
            modifier = Modifier.glassBorder(shape = MaterialTheme.shapes.large)
        ) {
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(12.dp)
                    .semantics { contentDescription = description }) {
                val gutter = 24.dp
                val header = 24.dp
                val column = (maxWidth - gutter) / 5
                val hour = (maxHeight - header) / 4
                Row(Modifier.padding(start = gutter)) {
                    DayType.weekdays().forEach { day ->
                        Text(
                            stringResource(day.stringValue),
                            Modifier.weight(1f),
                            color = label,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                for (i in 0..3) Text(
                    "${9 + i}",
                    Modifier
                        .offset(y = header + hour * i - 8.dp)
                        .width(20.dp),
                    color = label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
                Canvas(Modifier
                    .padding(start = gutter, top = header)
                    .fillMaxSize()) {
                    for (i in 0..7) {
                        val y = size.height * i / 8
                        drawLine(
                            line,
                            Offset(0f, y),
                            Offset(size.width, y),
                            1.dp.toPx(),
                            pathEffect = if (i % 2 == 1) PathEffect.dashPathEffect(
                                floatArrayOf(
                                    4.dp.toPx(),
                                    4.dp.toPx()
                                )
                            ) else null
                        )
                    }
                }
                val samples = listOf(
                    Triple(0, 0f, 1.5f),
                    Triple(1, 1f, 2f),
                    Triple(2, 0f, 1.5f),
                    Triple(3, 2f, 1.5f),
                    Triple(4, 1f, 2f)
                )
                samples.forEachIndexed { i, (day, start, duration) ->
                    val sample =
                        lecture.copy(courseID = i, name = titles[i % titles.size], subtitle = "")
                    val item = LectureItem(
                        lecture = sample,
                        lectureClass = LectureClass(
                            DayType.weekdays()[day],
                            540,
                            630,
                            "E11",
                            "",
                            "101"
                        ),
                        index = 0
                    )
                    TimetableGridCell(
                        item,
                        false,
                        hour * duration - 4.dp,
                        Modifier
                            .offset(x = gutter + column * day + 2.dp, y = header + hour * start)
                            .width(column - 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemePreviewSample() {
    Theme {
        ThemePreview(TimetableTheme.Default)
    }
}
