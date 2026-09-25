package org.sparcs.soap.wearable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.Timetable

@Serializable
data class WatchTimetable(
    val id: String,
    val lectures: List<WatchLecture>,
    val activities: List<WatchActivity> = emptyList(),
    val textColor: String = "#FFFFFF"
)

@Serializable
data class WatchActivity(
    val id: Int,
    val title: String,
    val day: String,
    val begin: Int,
    val end: Int,
    val location: String,
    val color: String? = null
)

@Serializable
data class WatchSemester(
    val name: String,
    val beginDateMillis: Long,
    val endDateMillis: Long
)

@Serializable
data class WatchLecture(
    val id: Int,
    val name: String,
    val code: String,
    val classes: List<WatchLectureClass>,
    val color: String? = null,
    /** Palette slot this lecture occupies, so the payload can be recolored without refetching it. */
    val colorID: Int? = null
)

@Serializable
data class WatchLectureClass(
    val day: String,
    val begin: Int,
    val end: Int,
    @SerialName("location") val location: String
)

fun Timetable.toWatchModel(theme: TimetableTheme): WatchTimetable {
    return WatchTimetable(
        id = id,
        lectures = lectures.map { lecture ->
            WatchLecture(
                id = lecture.id,
                name = lecture.name + lecture.subtitle,
                code = lecture.code,
                classes = lecture.classes.map { cl ->
                    WatchLectureClass(
                        day = cl.day.name,
                        begin = cl.begin,
                        end = cl.end,
                        location = "(" + cl.buildingCode + ") " + cl.roomName
                    )
                },
                color = theme.colorFor(lecture.courseID).toWatchHex(),
                colorID = lecture.courseID
            )
        },
        activities = activities.mapNotNull { activity ->
            val day = DayType.fromValue(activity.day) ?: return@mapNotNull null
            WatchActivity(
                id = activity.id,
                title = activity.title,
                day = day.name,
                begin = activity.begin,
                end = activity.end,
                location = activity.location,
                color = theme.colorFor(activity.id).toWatchHex()
            )
        },
        textColor = theme.textColor.toWatchHex()
    )
}

/** Recolors a payload with [theme] so a theme change reaches the watch without refetching the table. */
fun WatchTimetable.themed(theme: TimetableTheme): WatchTimetable = copy(
    lectures = lectures.map { lecture ->
        lecture.colorID?.let { lecture.copy(color = theme.colorFor(it).toWatchHex()) } ?: lecture
    },
    activities = activities.map { it.copy(color = theme.colorFor(it.id).toWatchHex()) },
    textColor = theme.textColor.toWatchHex()
)

private fun Color.toWatchHex(): String =
    String.format("#%06X", 0xFFFFFF and toArgb())
