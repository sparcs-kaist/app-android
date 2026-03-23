package org.sparcs.soap.App.Features.LectureDetail.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class LectureDetailViewEvent : Event {
    data object CourseLoaded : LectureDetailViewEvent()
    data object ReviewsLoaded : LectureDetailViewEvent()
    data object ReviewLiked : LectureDetailViewEvent()


    override val source: String
        get() = "LectureDetailView"

    override val name: String
        get() = when (this) {
            is CourseLoaded -> "course_loaded"
            is ReviewsLoaded -> "reviews_loaded"
            is ReviewLiked -> "review_liked"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}