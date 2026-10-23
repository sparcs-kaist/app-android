package org.sparcs.soap.app.domain.models.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.helpers.TimetableColorPalette
import kotlin.math.roundToInt

data class ActivityTime(
    val day: Int,
    val begin: Int,
    val end: Int,
) {
    val duration get() = end - begin
    val isValid get() = day in 0..6 && begin >= 0 && end <= 1440 && end - begin >= 15

    fun overlaps(day: Int, begin: Int, end: Int) = this.day == day && this.begin < end && begin < this.end
    fun overlaps(other: ActivityTime) = overlaps(other.day, other.begin, other.end)
    fun overlaps(lecture: Lecture) = lecture.classes.any { overlaps(it.day.value, it.begin, it.end) }

    fun move(minutes: Int, newDay: Int = day): ActivityTime {
        val length = duration.coerceIn(15, 1440)
        val start = minutes.coerceIn(0, 1440 - length)
        return ActivityTime(newDay.coerceIn(0, 6), start, start + length)
    }

    fun resizeStart(minutes: Int) = copy(begin = minutes.coerceIn(0, end - 15))
    fun resizeEnd(minutes: Int) = copy(end = minutes.coerceIn(begin + 15, 1440))
}

data class TimetableActivity(
    val id: Int,
    @SerializedName("block_name") val title: String,
    @SerializedName("place") val location: String,
    val day: Int,
    val begin: Int,
    val end: Int,
    private val times: List<ActivityTime>? = null,
) {
    constructor(id: Int, title: String, location: String, times: List<ActivityTime>) : this(
        id = id,
        title = title,
        location = location,
        day = times.first().day,
        begin = times.first().begin,
        end = times.first().end,
        times = times,
    )

    val ranges: List<ActivityTime>
        get() = times?.takeIf { it.isNotEmpty() } ?: listOf(ActivityTime(day, begin, end))

    val slots: List<TimetableActivity>
        get() = ranges.map { copy(day = it.day, begin = it.begin, end = it.end, times = listOf(it)) }

    val backgroundColor get() = TimetableColorPalette.palettes[0].colors.let { it[Math.floorMod(id, it.size)] }

    fun draft() = ActivityDraft(title, location, ranges)

    fun overlaps(lecture: Lecture) = ranges.any { it.overlaps(lecture) }
    fun overlaps(time: ActivityTime) = ranges.any { it.overlaps(time) }
}

data class ActivityDraft(
    @SerializedName("block_name") val title: String = "",
    @SerializedName("place") val location: String = "",
    val times: List<ActivityTime> = listOf(ActivityTime(0, 540, 600)),
) {
    constructor(title: String = "", location: String = "", day: Int, begin: Int = 540, end: Int = 600) :
        this(title, location, listOf(ActivityTime(day, begin, end)))

    val day get() = times.first().day
    val begin get() = times.first().begin
    val end get() = times.first().end

    val hasOverlappingTimes: Boolean
        get() = times.withIndex().any { (index, time) -> times.drop(index + 1).any(time::overlaps) }

    val isValid get() = title.isNotBlank() && times.isNotEmpty() && times.all { it.isValid } && !hasOverlappingTimes

    fun conflict(table: Timetable, excludingID: Int? = null): Boolean = times.any { conflict(table, it, excludingID) }

    fun conflict(table: Timetable, time: ActivityTime, excludingID: Int? = null): Boolean =
        table.lectures.any(time::overlaps) || table.activities.any { it.id != excludingID && it.overlaps(time) }

    fun replacing(index: Int, time: ActivityTime) = copy(times = times.toMutableList().also { it[index] = time })
    fun adding(time: ActivityTime) = copy(times = times + time)
    fun removing(index: Int) = if (times.size > 1) copy(times = times.filterIndexed { i, _ -> i != index }) else this

    fun move(minutes: Int, newDay: Int = day) = replacing(0, times.first().move(minutes, newDay))
    fun resizeStart(minutes: Int) = replacing(0, times.first().resizeStart(minutes))
    fun resizeEnd(minutes: Int) = replacing(0, times.first().resizeEnd(minutes))

    companion object {
        fun snap(minutes: Float) = (minutes / 15f).roundToInt() * 15

        fun initial(table: Timetable, day: Int): ActivityDraft = ActivityDraft(times = listOf(freeTime(table, day)))

        fun freeTime(table: Timetable, day: Int, taken: List<ActivityTime> = emptyList()): ActivityTime {
            for (start in 540..1380 step 15) {
                val time = ActivityTime(day, start, start + 60)
                if (!ActivityDraft(times = listOf(time)).conflict(table) && taken.none(time::overlaps)) return time
            }
            return ActivityTime(day, 540, 600)
        }
    }
}

val Timetable.activitySlots: List<TimetableActivity> get() = activities.flatMap { it.slots }

fun Timetable?.activityFor(slot: TimetableActivity): TimetableActivity =
    this?.activities?.firstOrNull { it.id == slot.id } ?: slot

fun Timetable.withActivityTimes(activityID: Int?, title: String, times: List<ActivityTime>): Timetable {
    val others = activities.filter { it.id != activityID }
    val siblings = times.takeIf { it.isNotEmpty() }?.let { TimetableActivity(SIBLING_TIMES_ID, title, "", it) }
    return copy(customBlocks = others + listOfNotNull(siblings))
}

private const val SIBLING_TIMES_ID = -1

class ActivityConflictException : Exception()
// The write succeeded; retry only the GET, never repeat a successful POST.
class ActivityRefreshRequiredException(cause: Throwable) : Exception(cause)
