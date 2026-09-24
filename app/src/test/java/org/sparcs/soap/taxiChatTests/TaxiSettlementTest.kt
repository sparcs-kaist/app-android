package org.sparcs.soap.taxiChatTests

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.taxiChat.components.validSettlementAmount
import org.sparcs.soap.app.networking.responseDTO.taxi.TaxiChatDTO
import org.sparcs.soap.app.shared.mocks.taxi.mock

class TaxiSettlementTest {
    private val content = """{"total":10000,"perPerson":3333,"participantCount":3}"""

    private fun chat(body: String, meta: String? = null, type: String = "settlement"): TaxiChat {
        val gson = Gson()
        val json = JsonObject().apply {
            addProperty("roomId", "room")
            addProperty("type", type)
            addProperty("content", body)
            addProperty("time", "2026-09-24T09:00:00.000Z")
            addProperty("isValid", true)
            meta?.let { add("settlementMeta", gson.fromJson(it, JsonObject::class.java)) }
        }
        return gson.fromJson(json, TaxiChatDTO::class.java).toModel()
    }

    @Test fun `settlement details support both response formats`() {
        val expected = TaxiChat.SettlementMeta(10000, 3333, 3)
        assertEquals(expected, chat(content).settlementMeta)
        assertEquals(expected, chat("", content).settlementMeta)
        assertEquals(expected, chat("not json", content).settlementMeta)
    }

    @Test fun `plain old messages and invalid metadata remain readable`() {
        listOf("", "I paid", "null", "[]", "{}", "{broken", content.replace("10000", "2147483648"),
            content.replace("10000", "-1"), content.replace("3333", "3.5"),
            content.replace("\"participantCount\":3", "\"participantCount\":0")).forEach {
            assertNull(it, chat(it).settlementMeta)
        }
        assertNull(chat(content, type = "text").settlementMeta)
    }

    @Test fun `amount input requires positive bounded total and participants`() {
        listOf("", "0", "-1", "2147483648", "12.5").forEach { assertNull(validSettlementAmount(it, 3)) }
        assertNull(validSettlementAmount("10000", 0))
        val amount = validSettlementAmount("10000", 3)!!
        assertEquals(3333, amount / 3)
        assertEquals(10000, validSettlementAmount("00010000", 3))
    }

    @Test fun `payment requires departure settlement and current unpaid participant`() {
        val original = TaxiRoom.mock()
        val participant = original.participants.first().copy(isSettlement = TaxiParticipant.SettlementType.PaymentRequired)
        val room = original.copy(isDeparted = true, settlementTotal = 1, participants = listOf(participant))
        assertTrue(room.canCommitPayment(participant.id))
        assertFalse(room.copy(isDeparted = false).canCommitPayment(participant.id))
        assertFalse(room.copy(settlementTotal = null).canCommitPayment(participant.id))
        assertFalse(room.copy(settlementTotal = 0).canCommitPayment(participant.id))
        assertFalse(room.canCommitPayment(null))
        assertFalse(room.canCommitPayment("other"))
        listOf(TaxiParticipant.SettlementType.PaymentSent, TaxiParticipant.SettlementType.RequestedSettlement).forEach {
            assertFalse(room.copy(participants = listOf(participant.copy(isSettlement = it))).canCommitPayment(participant.id))
        }
    }
}
