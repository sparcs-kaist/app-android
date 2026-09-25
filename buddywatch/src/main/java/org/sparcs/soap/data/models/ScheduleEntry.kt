package org.sparcs.soap.data.models

import java.time.DayOfWeek
import java.time.LocalDateTime

data class ScheduleEntry(
    val id: String,
    val title: String,
    val classTime: LectureClass,
    val color: String?,
    val code: String = ""
) {
    val location: String get() = classTime.location
}

val DayOfWeek.dayCode: String get() = name.take(3)

fun Timetable.scheduleEntries(day: DayOfWeek): List<ScheduleEntry> = (
    lectures.flatMap { lecture ->
        lecture.classes.filter { it.day == day.dayCode }.map { time ->
            ScheduleEntry(
                "lecture-${lecture.id}-${time.day}-${time.begin}-${time.end}",
                lecture.name, time, lecture.color, lecture.code
            )
        }
    } + activities.filter { it.day == day.dayCode }.map { activity ->
        ScheduleEntry(
            "activity-${activity.id}-${activity.day}-${activity.begin}",
            activity.title,
            LectureClass(activity.day, activity.begin, activity.end, activity.location),
            activity.color
        )
    }
).filter { it.classTime.begin >= 0 && it.classTime.end <= 1440 && it.classTime.end > it.classTime.begin }
    .sortedWith(compareBy({ it.classTime.begin }, { it.id }))

val Timetable.visibleDays: List<DayOfWeek>
    get() = DayOfWeek.entries.take(
        when {
            scheduleEntries(DayOfWeek.SUNDAY).isNotEmpty() -> 7
            scheduleEntries(DayOfWeek.SATURDAY).isNotEmpty() -> 6
            else -> 5
        }
    )

fun Timetable.upcomingEntry(now: LocalDateTime): ScheduleEntry? =
    scheduleEntries(now.dayOfWeek).firstOrNull { it.classTime.end > now.hour * 60 + now.minute }

fun defaultSelection(items: List<ScheduleEntry>, now: LocalDateTime): String? =
    items.firstOrNull { it.classTime.end > now.hour * 60 + now.minute }?.id ?: items.lastOrNull()?.id

data class DayCell(val entry: ScheduleEntry, val lane: Int, val laneCount: Int)

fun scheduleCells(items: List<ScheduleEntry>): List<DayCell> {
    val groups = mutableListOf<MutableList<ScheduleEntry>>()
    var groupEnd = -1
    for (entry in items.sortedBy { it.classTime.begin }) {
        if (entry.classTime.begin >= groupEnd) groups.add(mutableListOf(entry))
        else groups.last().add(entry)
        groupEnd = if (groups.last().size == 1) entry.classTime.end else maxOf(groupEnd, entry.classTime.end)
    }
    return groups.flatMap { group ->
        val laneEnds = mutableListOf<Int>()
        val lanes = group.map { entry ->
            val available = laneEnds.indexOfFirst { it <= entry.classTime.begin }
            val lane = if (available == -1) laneEnds.size.also { laneEnds.add(entry.classTime.end) }
                else available.also { laneEnds[it] = entry.classTime.end }
            entry to lane
        }
        lanes.map { (entry, lane) -> DayCell(entry, lane, laneEnds.size) }
    }
}
