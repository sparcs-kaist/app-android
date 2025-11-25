package com.sparcs.soap.Domain.Helpers

import androidx.compose.ui.unit.dp
import com.sparcs.soap.Domain.Models.OTL.LectureItem

object TimetableConstructor {
    val hoursWidth = 16.dp
    val daysHeight = 16.dp

    private const val TOP_EXTRA_PX = 14f
    private const val CELL_OVERLAP_ADJUST_PX = 4f

    fun getCellHeightPx(
        item: LectureItem,
        containerHeightPx: Float,
        durationMinutes: Int,
        daysHeightPx: Float
    ): Float {
        val timetableHeight = containerHeightPx - daysHeightPx - TOP_EXTRA_PX
        val cellDuration = item.lecture.classTimes[item.index].duration.toFloat()
        val cellHeight = (timetableHeight / durationMinutes.toFloat()) * cellDuration
        return if (cellHeight <= 0f) 0f else cellHeight - CELL_OVERLAP_ADJUST_PX
    }

    fun getCellOffsetPx(
        item: LectureItem,
        containerHeightPx: Float,
        startMinutes: Int,
        durationMinutes: Int,
        daysHeightPx: Float
    ): Float {
        val timetableHeight = containerHeightPx - daysHeightPx - TOP_EXTRA_PX
        val begin = item.lecture.classTimes[item.index].begin
        val difference = (timetableHeight / durationMinutes.toFloat()) * (begin - startMinutes).toFloat()
        return daysHeightPx + TOP_EXTRA_PX + difference
    }
}