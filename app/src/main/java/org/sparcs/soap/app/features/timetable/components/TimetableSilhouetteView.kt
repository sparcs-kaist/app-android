package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TimetableSilhouetteView(timetable: Timetable?, modifier: Modifier = Modifier) {
    val theme = LocalTimetableTheme.current
    val track = theme.gridLabelColor?.copy(alpha = 0.15f)
        ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    val days = timetable?.visibleDays ?: DayType.weekdays()
    val classes = timetable?.lectures.orEmpty().flatMap { it.classes }
    val activities = timetable?.activities.orEmpty()
    val begin = (classes.map { it.begin } + activities.map { it.begin }).minOrNull() ?: 540
    val end = (classes.map { it.end } + activities.map { it.end }).maxOrNull() ?: 1080
    val duration = (end - begin).coerceAtLeast(1)
    Canvas(modifier) {
        val gap = 4.dp.toPx()
        val blockGap = 2.dp.toPx()
        val width = ((size.width - gap * (days.size - 1)) / days.size).coerceAtLeast(0f)
        val radius = CornerRadius(4.dp.toPx())
        days.forEachIndexed { index, day ->
            val x = index * (width + gap)
            drawRoundRect(track, Offset(x, 0f), Size(width, size.height), radius)
            timetable?.getLectures(day, null).orEmpty().forEach { item ->
                val top = (item.lectureClass.begin - begin).toFloat() / duration * size.height
                val rawHeight = (item.lectureClass.end - item.lectureClass.begin).toFloat() / duration * size.height
                val height = (rawHeight - blockGap).coerceAtLeast(1f)
                if (rawHeight > 0) drawRoundRect(theme.colorFor(item.lecture.courseID), Offset(x, top), Size(width, height), radius)
            }
            activities.filter { it.day == day.value }.forEach { activity ->
                val top = (activity.begin - begin).toFloat() / duration * size.height
                val rawHeight = (activity.end - activity.begin).toFloat() / duration * size.height
                val height = (rawHeight - blockGap).coerceAtLeast(1f)
                if (rawHeight > 0) drawRoundRect(theme.colorFor(activity.id), Offset(x, top), Size(width, height), radius)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableSilhouettePreview() {
    Theme { TimetableSilhouetteView(Timetable.mock(), Modifier.size(160.dp)) }
}
