package org.sparcs.soap.app.domain.helpers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import javax.inject.Inject

interface BoardListSectionStoreProtocol {
    fun load(): List<BoardListSection>
    fun save(sections: List<BoardListSection>)
}

interface BoardSeenPostsStoreProtocol {
    fun newestSeen(boardId: Int): Int?
    fun markSeen(postId: Int, boardId: Int)
    fun reset()
}

class BoardListSectionStore @Inject constructor(
    @param:ApplicationContext context: Context,
) : BoardListSectionStoreProtocol {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    override fun load(): List<BoardListSection> {
        val stored = preferences.getString(KEY, null) ?: return BoardListSection.defaults
        if (stored.isEmpty()) return emptyList()
        return stored.split(SEPARATOR).mapNotNull(BoardListSection::fromId).distinct()
    }

    override fun save(sections: List<BoardListSection>) {
        preferences.edit().putString(KEY, sections.distinct().joinToString(SEPARATOR) { it.id }).apply()
    }

    private companion object {
        const val PREFERENCES = "ara"
        const val KEY = "ara.boardListSections"
        const val SEPARATOR = "\n"
    }
}

class BoardSeenPostsStore @Inject constructor(
    @param:ApplicationContext context: Context,
) : BoardSeenPostsStoreProtocol {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    override fun newestSeen(boardId: Int): Int? =
        preferences.getInt(key(boardId), Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE }

    override fun markSeen(postId: Int, boardId: Int) {
        if (postId <= (newestSeen(boardId) ?: Int.MIN_VALUE)) return
        preferences.edit().putInt(key(boardId), postId).apply()
    }

    override fun reset() {
        preferences.edit().clear().apply()
    }

    private fun key(boardId: Int) = "board.$boardId"

    private companion object {
        const val PREFERENCES = "ara.seenPosts"
    }
}
