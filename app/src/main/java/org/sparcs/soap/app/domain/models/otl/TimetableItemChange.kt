package org.sparcs.soap.app.domain.models.otl

sealed interface TimetableItemChange {
    data class AddLecture(val lectureID: Int) : TimetableItemChange
    data class RemoveLecture(val lectureID: Int) : TimetableItemChange
    data class AddActivity(val draft: ActivityDraft) : TimetableItemChange
    data class UpdateActivity(val activityID: Int, val draft: ActivityDraft) : TimetableItemChange
    data class RemoveActivity(val activityID: Int) : TimetableItemChange
}
