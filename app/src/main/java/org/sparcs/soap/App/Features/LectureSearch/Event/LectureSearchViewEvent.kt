package org.sparcs.soap.App.Features.LectureSearch.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class LectureSearchViewEvent : Event {
    data object LecturesSearched : LectureSearchViewEvent()

    override val source: String
        get() = "LectureSearchView"

    override val name: String
        get() = when (this) {
            is LecturesSearched -> "lectures_searched"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}