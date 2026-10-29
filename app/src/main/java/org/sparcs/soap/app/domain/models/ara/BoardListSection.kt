package org.sparcs.soap.app.domain.models.ara

sealed interface BoardListSection {
    val id: String

    data object Trending : BoardListSection {
        override val id = "trending"
    }

    data object Latest : BoardListSection {
        override val id = "latest"
    }

    data class Board(val slug: String) : BoardListSection {
        override val id = "board:$slug"
    }

    companion object {
        val defaults: List<BoardListSection> = listOf(Trending, Latest, Board(AraBoard.MARKET_SLUG))

        fun fromId(id: String): BoardListSection? = when {
            id == Trending.id -> Trending
            id == Latest.id -> Latest
            id.startsWith("board:") -> id.removePrefix("board:").takeIf { it.isNotBlank() }?.let(::Board)
            else -> null
        }
    }
}
