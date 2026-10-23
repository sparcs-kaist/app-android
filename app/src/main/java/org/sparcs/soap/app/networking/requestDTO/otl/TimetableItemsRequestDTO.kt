package org.sparcs.soap.app.networking.requestDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.TimetableItemChange

data class TimetableItemsRequestDTO(val changes: List<ItemChangeDTO>) {
    data class ItemChangeDTO(
        val op: String,
        val kind: String,
        val lectureId: Int? = null,
        val id: Int? = null,
        val data: ActivityDataDTO? = null,
    )

    data class ActivityDataDTO(
        @SerializedName("block_name") val title: String,
        @SerializedName("place") val location: String,
        val times: List<TimeDTO>,
    )

    data class TimeDTO(val day: Int, val begin: Int, val end: Int)

    companion object {
        fun of(changes: List<TimetableItemChange>) = TimetableItemsRequestDTO(changes.map(::change))

        private fun change(change: TimetableItemChange) = when (change) {
            is TimetableItemChange.AddLecture -> ItemChangeDTO("add", "lecture", lectureId = change.lectureID)
            is TimetableItemChange.RemoveLecture -> ItemChangeDTO("remove", "lecture", id = change.lectureID)
            is TimetableItemChange.AddActivity -> ItemChangeDTO("add", "custom", data = data(change.draft))
            is TimetableItemChange.UpdateActivity -> ItemChangeDTO("update", "custom", id = change.activityID, data = data(change.draft))
            is TimetableItemChange.RemoveActivity -> ItemChangeDTO("remove", "custom", id = change.activityID)
        }

        private fun data(draft: ActivityDraft) = ActivityDataDTO(
            title = draft.title.trim(),
            location = draft.location.trim(),
            times = draft.times.map { TimeDTO(it.day, it.begin, it.end) },
        )
    }
}

data class TimetableItemsResponseDTO(val results: List<Result> = emptyList()) {
    data class Result(val index: Int, val id: Int)
}
