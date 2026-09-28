package org.sparcs.soap.notificationTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.features.feedPost.toCommentListItems
import org.sparcs.soap.app.shared.mocks.feed.mockList

class FeedCommentListTest {
    @Test
    fun repliesHaveTheirOwnPositionsInTheDisplayedOrder() {
        val base = FeedComment.mockList().first()
        val reply = base.copy(id = "reply", parentCommentID = "parent", replies = emptyList())
        val parent = base.copy(id = "parent", parentCommentID = null, replies = listOf(reply))
        val next = base.copy(id = "next", parentCommentID = null, replies = emptyList())
        val rows = listOf(parent, next).toCommentListItems()
        assertEquals(listOf("parent", "reply", "next"), rows.map { it.comment.id })
        assertEquals(1, rows.indexOfFirst { it.comment.id == "reply" })
        assertFalse(rows[0].isReply)
        assertTrue(rows[1].isReply)
        assertFalse(rows[0].endsThread)
        assertTrue(rows[1].endsThread)
        assertTrue(rows[2].endsThread)
    }

    @Test
    fun emptyCommentsProduceNoScrollTargets() {
        assertTrue(emptyList<FeedComment>().toCommentListItems().isEmpty())
    }
}
