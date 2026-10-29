package org.sparcs.soap.boardListTests

import org.junit.Assert.assertEquals
import org.junit.Test
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.settings.ara.BoardSectionsOrder
import org.sparcs.soap.app.shared.mocks.ara.mock

class BoardSectionsOrderTest {

    private val market = BoardListSection.Board(AraBoard.MARKET_SLUG)

    @Test
    fun offersOnlyTheHighlightsNotShown() {
        assertEquals(listOf(BoardListSection.Latest), BoardSectionsOrder.addableHighlights(listOf(market, BoardListSection.Trending)))
        assertEquals(emptyList<BoardListSection>(), BoardSectionsOrder.addableHighlights(BoardListSection.defaults))
    }

    @Test
    fun offersReadableBoardsOfTheGroupNotShown() {
        val group = AraBoardGroup.Empty
        val other = group.copy(id = 1)
        val boards = listOf(
            AraBoard.mock().copy(id = 1, slug = "talk", group = group),
            AraBoard.mock().copy(id = 2, slug = AraBoard.MARKET_SLUG, group = group),
            AraBoard.mock().copy(id = 3, slug = "secret", group = group, userReadable = false),
            AraBoard.mock().copy(id = 4, slug = "club", group = other),
        )
        assertEquals(listOf("talk"), BoardSectionsOrder.addableBoards(listOf(market), boards, group).map { it.slug })
    }

    @Test
    fun movesASectionAndIgnoresMovesPastTheEnds() {
        val sections = listOf(BoardListSection.Trending, BoardListSection.Latest, market)
        assertEquals(listOf(BoardListSection.Latest, BoardListSection.Trending, market), BoardSectionsOrder.move(sections, 1, -1))
        assertEquals(listOf(BoardListSection.Trending, market, BoardListSection.Latest), BoardSectionsOrder.move(sections, 1, 1))
        assertEquals(sections, BoardSectionsOrder.move(sections, 0, -1))
        assertEquals(sections, BoardSectionsOrder.move(sections, 2, 1))
        assertEquals(sections, BoardSectionsOrder.move(sections, -1, 1))
    }
}
