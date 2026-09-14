package org.sparcs.soap.app.domain.models.otl

/**
 * The outcome of copying a timetable. The server has no copy endpoint, so a duplicate is replayed
 * item by item and individual rejections are reported instead of failing the whole copy.
 */
data class TableDuplication(
    val id: Int,
    val skippedLectureCount: Int = 0,
    val skippedActivityCount: Int = 0,
) {
    val isComplete get() = skippedLectureCount == 0 && skippedActivityCount == 0
}
