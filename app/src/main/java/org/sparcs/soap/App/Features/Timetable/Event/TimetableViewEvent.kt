package org.sparcs.soap.App.Features.Timetable.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class TimetableViewEvent : Event {
    data object SemesterChanged : TimetableViewEvent()
    data object TimetableSelected : TimetableViewEvent()
    data object LectureAdded : TimetableViewEvent()
    data object LectureDeleted : TimetableViewEvent()
    data object TableRenamed : TimetableViewEvent()
    data object TableDeleted : TimetableViewEvent()
    data object TableCreated : TimetableViewEvent()

    override val source: String
        get() = "TimetableView"

    override val name: String
        get() = when (this) {
            is SemesterChanged -> "semester_changed"
            is TimetableSelected -> "timetable_selected"
            is LectureAdded -> "lecture_added"
            is LectureDeleted -> "lecture_deleted"
            is TableRenamed -> "table_renamed"
            is TableDeleted -> "table_deleted"
            is TableCreated -> "table_created"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}