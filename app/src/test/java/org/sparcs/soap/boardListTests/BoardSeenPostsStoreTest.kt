package org.sparcs.soap.boardListTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.sparcs.soap.app.domain.helpers.BoardSeenPostsStore

@RunWith(RobolectricTestRunner::class)
class BoardSeenPostsStoreTest {

    private val store = BoardSeenPostsStore(RuntimeEnvironment.getApplication())

    @Test
    fun seenPostsOnlyMoveForward() {
        store.markSeen(200, 2)
        store.markSeen(150, 2)

        assertEquals(200, store.newestSeen(2))
        assertNull(store.newestSeen(3))
    }

    @Test
    fun resetForgetsEveryBoard() {
        store.markSeen(200, 2)
        store.markSeen(10, 3)

        store.reset()

        assertNull(store.newestSeen(2))
        assertNull(store.newestSeen(3))
    }
}
