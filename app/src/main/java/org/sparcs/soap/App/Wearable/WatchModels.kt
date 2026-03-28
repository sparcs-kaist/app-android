package org.sparcs.soap.App.Wearable

import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.backgroundColor

@Serializable
data class WatchTimetable(
    val id: String,
    val lectures: List<WatchLecture>
)

@Serializable
data class WatchLecture(
    val id: Int,
    val name: String,
    val classes: List<WatchLectureClass>,
    val color: String? = null
)

@Serializable
data class WatchLectureClass(
    val day: String,
    val begin: Int,
    val end: Int,
    @SerialName("location") val location: String
)

fun Timetable.toWatchModel(): WatchTimetable {
    return WatchTimetable(
        id = id,
        lectures = lectures.map { lecture ->
            WatchLecture(
                id = lecture.id,
                name = lecture.name + lecture.subtitle,
                classes = lecture.classes.map { cl ->
                    WatchLectureClass(
                        day = cl.day.name,
                        begin = cl.begin,
                        end = cl.end,
                        location = "(" + cl.buildingCode + ") " + cl.roomName
                    )
                },
                color = "#" + Integer.toHexString(lecture.backgroundColor.toArgb()).uppercase()
            )
        }
    )
}