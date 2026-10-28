package org.sparcs.soap.timetableTests

import com.google.gson.Gson
import org.junit.Assert.*
import org.sparcs.soap.app.domain.models.otl.*
import org.junit.Test
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
        val time = ActivityTime(0, 600, 690)
        assertEquals(ActivityTime(0, 1350, 1440), time.move(1430))
        assertEquals(ActivityTime(0, 0, 90), time.move(-60))
        assertEquals(5, time.move(600, 5).day)
    }
    @Test fun resizeMaintainsMinimumDurationAndMidnightBoundary() {
        val draft = activity.draft()
        assertEquals(645, draft.resizeStart(1000).begin)
        assertEquals(615, draft.resizeEnd(500).end)
        assertEquals(1440, draft.resizeEnd(1500).end)
        assertFalse(draft.replacing(0, ActivityTime(0, 600, 1441)).isValid)
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
        assertEquals(setOf("block_name", "place", "times"), json.keySet())
        assertEquals("Study", json["block_name"].asString)
        assertEquals(0, json["times"].asJsonArray[0].asJsonObject["day"].asInt)
    }

    @Test fun everyTimeOfAnActivityCountsForConflicts() {
        val twice = TimetableActivity(5, "Gym", "", listOf(ActivityTime(0, 540, 600), ActivityTime(2, 540, 600)))
        val busy = Timetable("12", emptyList(), listOf(twice))
        assertTrue(ActivityDraft("New", day = 2, begin = 570, end = 630).conflict(busy))
        assertFalse(ActivityDraft("New", day = 1, begin = 540, end = 600).conflict(busy))
        assertEquals(2, busy.activitySlots.size)
        assertEquals(listOf(DayType.MON, DayType.TUE, DayType.WED, DayType.THU, DayType.FRI), busy.visibleDays)
    }

    @Test fun overlappingTimesWithinOneActivityAreInvalid() {
        val draft = ActivityDraft("Gym", times = listOf(ActivityTime(0, 540, 600), ActivityTime(0, 570, 630)))
        assertTrue(draft.hasOverlappingTimes)
        assertFalse(draft.isValid)
        assertTrue(ActivityDraft("Gym", times = listOf(ActivityTime(0, 540, 600), ActivityTime(0, 600, 660))).isValid)
    }

    @Test fun removingKeepsAtLeastOneTime() {
        val draft = ActivityDraft("Gym", day = 0)
        assertEquals(draft, draft.removing(0))
        assertEquals(1, draft.adding(ActivityTime(1, 540, 600)).removing(0).times.size)
    }

    @Test fun serverTimesAreReadAndLegacyBlocksStillLoad() {
        val gson = Gson()
        val multi = gson.fromJson(
            """{"id":3,"block_name":"Gym","place":"","day":0,"begin":540,"end":600,"times":[{"day":0,"begin":540,"end":600},{"day":3,"begin":1200,"end":1260}]}""",
            TimetableActivity::class.java,
        )
        assertEquals(listOf(ActivityTime(0, 540, 600), ActivityTime(3, 1200, 1260)), multi.ranges)
        val legacy = gson.fromJson("""{"id":3,"block_name":"Gym","place":"","day":1,"begin":540,"end":600}""", TimetableActivity::class.java)
        assertEquals(listOf(ActivityTime(1, 540, 600)), legacy.ranges)
        val empty = gson.fromJson("""{"id":3,"block_name":"Gym","place":"","day":1,"begin":540,"end":600,"times":[]}""", TimetableActivity::class.java)
        assertEquals(listOf(ActivityTime(1, 540, 600)), empty.ranges)
        assertEquals(multi.ranges, gson.fromJson(gson.toJson(multi), TimetableActivity::class.java).ranges)
    }

    @Test fun editingContextShowsOtherTimesAsConflicts() {
        val context = table.withActivityTimes(17, "Study", listOf(ActivityTime(3, 600, 660)))
        assertTrue(context.activities.none { it.id == 17 })
        assertTrue(ActivityDraft("Study", day = 3, begin = 630, end = 690).conflict(context, excludingID = 17))
    }
}
