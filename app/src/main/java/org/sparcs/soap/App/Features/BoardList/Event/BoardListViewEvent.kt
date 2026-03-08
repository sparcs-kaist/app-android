package org.sparcs.soap.App.Features.BoardList.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class BoardListViewEvent : Event {
    data object BoardsLoaded : BoardListViewEvent()
    data class BoardSelected(val boardName: String) : BoardListViewEvent()

    override val source: String = "BoardListView"

    override val name: String
        get() = when (this) {
            is BoardsLoaded -> "boards_loaded"
            is BoardSelected -> "board_selected"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is BoardSelected -> mapOf(
                "source" to source,
                "boardName" to boardName
            )
            else -> mapOf("source" to source)
        }
}