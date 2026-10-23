package org.sparcs.soap.networkingTests

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.ActivityTime
import org.sparcs.soap.app.domain.models.otl.TimetableItemChange
import org.sparcs.soap.app.networking.requestDTO.otl.TimetableItemsRequestDTO
import org.sparcs.soap.app.networking.requestDTO.otl.TimetableItemsResponseDTO

class TimetableItemsRequestDTOTest {
    private val gson = Gson()

    @Test
    fun `changes match the items endpoint`() {
        val draft = ActivityDraft(" Gym ", " Hall ", listOf(ActivityTime(0, 540, 600), ActivityTime(2, 1200, 1260)))
        val json = gson.toJsonTree(
            TimetableItemsRequestDTO.of(
                listOf(
                    TimetableItemChange.RemoveActivity(4),
                    TimetableItemChange.RemoveLecture(7),
                    TimetableItemChange.AddLecture(9),
                    TimetableItemChange.UpdateActivity(4, draft),
                    TimetableItemChange.AddActivity(draft),
                )
            )
        ).asJsonObject["changes"].asJsonArray

        assertEquals("""{"op":"remove","kind":"custom","id":4}""", json[0].toString())
        assertEquals("""{"op":"remove","kind":"lecture","id":7}""", json[1].toString())
        assertEquals("""{"op":"add","kind":"lecture","lectureId":9}""", json[2].toString())
        val update = json[3].asJsonObject
        assertEquals("update", update["op"].asString)
        assertEquals(4, update["id"].asInt)
        val data = update["data"].asJsonObject
        assertEquals("Gym", data["block_name"].asString)
        assertEquals("Hall", data["place"].asString)
        assertEquals("""[{"day":0,"begin":540,"end":600},{"day":2,"begin":1200,"end":1260}]""", data["times"].toString())
        assertFalse(json[4].asJsonObject.has("id"))
    }

    @Test
    fun `results come back in request order`() {
        val response = gson.fromJson("""{"results":[{"index":1,"id":20},{"index":0,"id":10}]}""", TimetableItemsResponseDTO::class.java)
        assertEquals(listOf(10, 20), response.results.sortedBy { it.index }.map { it.id })
    }
}
