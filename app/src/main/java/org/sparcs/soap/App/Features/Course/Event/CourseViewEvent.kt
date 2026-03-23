package org.sparcs.soap.App.Features.Course.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class CourseViewEvent : Event {
    data object CourseLoaded : CourseViewEvent()
    data object ReviewsLoaded : CourseViewEvent()
    data object LikeReview : CourseViewEvent()


    override val source: String
        get() = "CourseView"

    override val name: String
        get() = when (this) {
            is CourseLoaded -> "course_loaded"
            is ReviewsLoaded -> "reviews_loaded"
            is LikeReview -> "like_review"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}