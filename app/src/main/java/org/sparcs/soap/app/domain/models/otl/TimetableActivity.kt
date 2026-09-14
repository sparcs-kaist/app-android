package org.sparcs.soap.app.domain.models.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableColorPalette
import kotlin.math.roundToInt

data class TimetableActivity(
    val id: Int,
    @SerializedName("block_name") val title: String,
    @SerializedName("place") val location: String,
    val day: Int,
    val begin: Int,
    val end: Int,
) {
    val backgroundColor get() = TimetableColorPalette.palettes[0].colors.let { it[Math.floorMod(id, it.size)] }
    fun draft() = ActivityDraft(title, location, day, begin, end)
}

data class ActivityDraft(
    @SerializedName("block_name") val title: String = "",
    @SerializedName("place") val location: String = "",
    val day: Int = 0,
    val begin: Int = 540,
    val end: Int = 600,
) {
    val isValid get() = title.isNotBlank() && day in 0..6 && begin >= 0 && end <= 1440 && end - begin >= 15
    fun conflict(table: Timetable, excludingID: Int? = null): Boolean =
        table.lectures.any { lecture -> lecture.classes.any { it.day.value == day && begin < it.end && end > it.begin } } ||
            table.activities.any { it.id != excludingID && it.day == day && begin < it.end && end > it.begin }

    fun move(minutes: Int, newDay: Int = day): ActivityDraft {
        val duration = (end - begin).coerceIn(15, 1440)
        val start = minutes.coerceIn(0, 1440 - duration)
        return copy(day = newDay.coerceIn(0, 6), begin = start, end = start + duration)
    }
    fun resizeStart(minutes: Int) = copy(begin = minutes.coerceIn(0, end - 15))
    fun resizeEnd(minutes: Int) = copy(end = minutes.coerceIn(begin + 15, 1440))

    companion object {
        fun snap(minutes: Float) = (minutes / 15f).roundToInt() * 15
        fun initial(table: Timetable, day: Int): ActivityDraft {
            for (start in 540..1380 step 15) {
                val draft = ActivityDraft(day = day, begin = start, end = start + 60)
                if (!draft.conflict(table)) return draft
            }
            return ActivityDraft(day = day)
        }
    }
}

class ActivityConflictException : Exception()
// The write succeeded; retry only the GET, never repeat a successful POST.
class ActivityRefreshRequiredException(cause: Throwable) : Exception(cause)
