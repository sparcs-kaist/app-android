package org.sparcs.soap.timetableTests

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.shared.mocks.otl.mockList

class ActivityDraftTest {
    private val activity = TimetableActivity(17, "Study", "Library", 0, 600, 660)
    private val table = Timetable("12", emptyList(), listOf(activity))

    @Test fun touchingActivitiesDoNotConflictButPartialOverlapsDo() {
        assertFalse(ActivityDraft("New", day = 0, begin = 540, end = 600).conflict(table))
        assertTrue(ActivityDraft("New", day = 0, begin = 585, end = 615).conflict(table))
        assertFalse(ActivityDraft("New", day = 1, begin = 600, end = 660).conflict(table))
        assertFalse(activity.draft().conflict(table, excludingID = 17))
    }
    @Test fun lectureConflictsAreDetected() {
        val lecture = Lecture.mockList().first().copy(classes = listOf(LectureClass(DayType.MON, 600, 660, "", "", "")))
        assertTrue(activity.draft().conflict(Timetable("12", listOf(lecture))))
        assertTrue(table.hasCollision(lecture))
    }
    @Test fun movementPreservesDurationAndStaysInOneDay() {
        val draft = ActivityDraft("Study", begin = 600, end = 690)
        assertEquals(draft.copy(begin = 1350, end = 1440), draft.move(1430))
        assertEquals(draft.copy(begin = 0, end = 90), draft.move(-60))
        assertEquals(5, draft.move(600, 5).day)
    }
    @Test fun resizeMaintainsMinimumDurationAndMidnightBoundary() {
        val draft = activity.draft()
        assertEquals(645, draft.resizeStart(1000).begin)
        assertEquals(615, draft.resizeEnd(500).end)
        assertEquals(1440, draft.resizeEnd(1500).end)
        assertFalse(draft.copy(end = 1441).isValid)
        assertFalse(draft.copy(title = " ").isValid)
    }
    @Test fun snappingChoosesNearestQuarterHour() {
        assertEquals(600, ActivityDraft.snap(607f))
        assertEquals(615, ActivityDraft.snap(608f))
    }
    @Test fun olderCacheWithoutActivitiesStillLoads() {
        val old = Gson().fromJson("""{"id":"12","lectures":[]}""", Timetable::class.java)
        assertTrue(old.activities.isEmpty())
        val decoded = Gson().fromJson(Gson().toJson(table), Timetable::class.java)
        assertEquals(table.activities, decoded.activities)
    }
    @Test fun payloadMatchesCustomBlockEndpoint() {
        val json = Gson().toJsonTree(activity.draft()).asJsonObject
        assertEquals(setOf("block_name", "place", "day", "begin", "end"), json.keySet())
        assertEquals("Study", json["block_name"].asString)
        assertEquals(0, json["day"].asInt)
    }
}
