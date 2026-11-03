package org.sparcs.soap.buddyTestSupport.helper

import org.sparcs.soap.app.domain.helpers.BoardListSectionStoreProtocol
import org.sparcs.soap.app.domain.helpers.BoardSeenPostsStoreProtocol
import org.sparcs.soap.app.domain.models.ara.BoardListSection

class TestBoardListSectionStore(var sections: List<BoardListSection> = BoardListSection.defaults) : BoardListSectionStoreProtocol {
    override fun load(): List<BoardListSection> = sections
    override fun save(sections: List<BoardListSection>) {
        this.sections = sections
    }
}

class TestBoardSeenPostsStore(private val seen: MutableMap<Int, Int> = mutableMapOf()) : BoardSeenPostsStoreProtocol {
    override fun newestSeen(boardId: Int): Int? = seen[boardId]
    override fun markSeen(postId: Int, boardId: Int) {
        if (postId > (seen[boardId] ?: Int.MIN_VALUE)) seen[boardId] = postId
    }

    override fun reset() {
        seen.clear()
    }
}
