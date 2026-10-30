package org.sparcs.soap.taxiChatTests

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.ridersWithOwnStatus
import org.sparcs.soap.app.features.taxiChat.components.SerialStatusUpdates
import org.sparcs.soap.app.shared.mocks.taxi.mock

class TaxiRideStatusTest {

    @Test fun `updates run one after another and settle only after the latest`() = runTest {
        val updates = SerialStatusUpdates()
        val first = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()
        var settled = 0

        updates.enqueue(this, onSettled = { settled++ }) {
            first.await()
            order += "first"
        }
        updates.enqueue(this, onSettled = { settled++ }) { order += "second" }
        testScheduler.advanceUntilIdle()

        assertTrue(updates.isPending)
        assertTrue(order.isEmpty())

        first.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("first", "second"), order)
        assertEquals(1, settled)
        assertFalse(updates.isPending)
    }

    @Test fun `own status overrides only your own rider`() {
        val room = TaxiRoom.mock()
        val me = room.participants[0]
        val riders = room.ridersWithOwnStatus(me.id, isArrived = true, hasCarrier = true)

        assertTrue(riders[0].isArrived && riders[0].hasCarrier)
        assertEquals(room.participants[1], riders[1])
        assertEquals(room.participants, room.ridersWithOwnStatus(null, isArrived = true, hasCarrier = true))
    }
}
