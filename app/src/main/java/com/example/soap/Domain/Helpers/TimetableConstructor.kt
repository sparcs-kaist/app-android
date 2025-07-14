package com.example.soap.Domain.Helpers


import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.TimeTable.LectureItem

object TimetableConstructor {
    // The width of the column representing the time at the leftmost of the timetable
    val hoursWidth: Dp = 24.dp

    // The height of the row representing the day at the top of the timetable
    val daysHeight: Dp = 32.dp

    val topPadding: Dp = 0.dp

    /**
     * getCellHeight: Calculate the height of the lecture cell based on size of the timetable and its status
     * @param item LectureItem
     * @param size Size - size of the timetable (from Modifier.onGloballyPositioned)
     * @param duration Int - total duration of the timetable in minutes
     */
//    fun getCellHeight(item: LectureItem, size: Size, duration: Int): Float {
//
//        val timetableHeight = size.height - daysHeight.value
//        val classDuration = item.lecture.classTimes[item.index].duration
//
//        val totalBlocks = duration / 30
//        val blockHeight = timetableHeight / totalBlocks
//        val cellHeight = blockHeight * (classDuration / 30f)
//
//        return if (cellHeight <= 0f) 0f else cellHeight
//
//    }-
    fun getCellHeight(item: LectureItem, size: Size, duration: Int): Float {
        val pixelsPerMinute = daysHeight.value / 30f
        val classDuration = item.lecture.classTimes[item.index].duration
        return pixelsPerMinute * classDuration - 4f
    }

    fun getCellOffset(item: LectureItem, size: Size, start: Int, duration: Int): Float {
        val beginMinute = item.lecture.classTimes[item.index].begin
        val pixelsPerMinute = daysHeight.value / 30f
        return pixelsPerMinute * (beginMinute - start)
    }



    /**
     * getCellOffset: Calculate the vertical offset of a lecture cell
     * @param item LectureItem
     * @param size Size - size of the timetable
     * @param start Int - start minute of the timetable
     * @param duration Int - total duration of the timetable
     */
//    fun getCellOffset(item: LectureItem, size: Size, start: Int, duration: Int): Float {
//        val beginMinute = item.lecture.classTimes[item.index].begin
//        val timetableHeight = size.height - daysHeight.value - topPadding.value
//        val timeRatio = (beginMinute - start).toFloat() / duration
//        val offset = timetableHeight * timeRatio
//
//        return offset -2f
//    }
}
