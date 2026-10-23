package org.sparcs.soap.tipsTests

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.TimetableOnboarding.Step
import org.sparcs.soap.app.shared.tips.TipEvent
import org.sparcs.soap.app.shared.tips.TipStore
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class TipStoreTest {
    private val preferences: SharedPreferences
        get() = RuntimeEnvironment.getApplication().getSharedPreferences("tips_test", Context.MODE_PRIVATE)
    private var now = Instant.parse("2026-10-20T03:00:00Z")
    private val clock get() = Clock.fixed(now, ZoneId.of("Asia/Seoul"))

    private fun store() = TipStore(preferences, clock)

    @Before fun reset() { preferences.edit().clear().commit() }

    @Test fun `new user walks every onboarding step in order`() {
        val store = store()
        store.checkForExistingTables(hasTables = false)
        assertEquals(Step.StartEditing, store.onboarding.current)
        listOf(Step.StartEditing, Step.AddButton, Step.ResizePreview, Step.TogglePreview).forEach(store::completeOnboarding)
        assertEquals(Step.AddLecture, store.onboarding.current)
        store.lectureAdded()
        assertEquals(Step.Done, store.onboarding.current)
    }

    @Test fun `completing a step that is not current does nothing`() {
        val store = store()
        store.completeOnboarding(Step.TogglePreview)
        assertEquals(Step.StartEditing, store.onboarding.current)
    }

    @Test fun `existing user skips adding steps and ends after the preview tips`() {
        val store = store()
        store.checkForExistingTables(hasTables = true)
        assertEquals(Step.ResizePreview, store.onboarding.current)
        store.skipPreviewSteps()
        assertEquals(Step.Done, store.onboarding.current)
    }

    @Test fun `timetable tips wait for the existing tables check`() {
        val store = store()
        assertFalse(store.isEligible(BuddyTip.StartEditing))
        store.checkForExistingTables(hasTables = false)
        assertTrue(store.isEligible(BuddyTip.StartEditing))
        assertFalse(store.isEligible(BuddyTip.AddEvent))
    }

    @Test fun `closing an onboarding tip completes its step`() {
        val store = store()
        store.checkForExistingTables(hasTables = false)
        store.invalidate(BuddyTip.StartEditing)
        assertFalse(store.isEligible(BuddyTip.StartEditing))
        assertEquals(Step.AddButton, store.onboarding.current)
        assertTrue(store.isEligible(BuddyTip.AddEvent))
    }

    @Test fun `feature tips wait for their events and the end of onboarding`() {
        val store = store()
        store.donate(TipEvent.ActivityCreationOpened)
        assertFalse(store.isEligible(BuddyTip.QuickFill))
        store.donate(TipEvent.ActivityCreationOpened)
        assertTrue(store.isEligible(BuddyTip.QuickFill))
        assertFalse(store.isEligible(BuddyTip.ShareTimetable))
    }

    @Test fun `only one new feature tip shows per day`() {
        val store = store()
        repeat(2) { store.donate(TipEvent.ActivityCreationOpened) }
        repeat(3) { store.donate(TipEvent.PostOpened) }
        store.markShown(BuddyTip.QuickFill)
        assertTrue(store.isEligible(BuddyTip.QuickFill))
        assertFalse(store.isEligible(BuddyTip.PostTools))
        now = now.plusSeconds(24 * 60 * 60)
        assertTrue(store().isEligible(BuddyTip.PostTools))
    }

    @Test fun `feature tips stop after their maximum display count`() {
        repeat(3) { day ->
            now = Instant.parse("2026-10-20T03:00:00Z").plusSeconds(day * 24L * 60 * 60)
            val store = store()
            assertTrue(store.isEligible(BuddyTip.SummariseReview))
            store.markShown(BuddyTip.SummariseReview)
        }
        now = Instant.parse("2026-10-24T03:00:00Z")
        assertFalse(store().isEligible(BuddyTip.SummariseReview))
    }

    @Test fun `show immediately lifts the daily limit`() {
        val store = store()
        store.showsTipsImmediately = true
        store.markShown(BuddyTip.SummariseReview)
        repeat(3) { store.donate(TipEvent.PostOpened) }
        assertTrue(store.isEligible(BuddyTip.PostTools))
    }

    @Test fun `reset on next launch shows tips again and replays onboarding for existing users`() {
        val store = store()
        store.checkForExistingTables(hasTables = true)
        store.invalidate(BuddyTip.SummariseReview)
        store.requestReset()
        assertTrue(store.isResetPending)

        val relaunched = store()
        assertFalse(relaunched.isResetPending)
        assertTrue(relaunched.isEligible(BuddyTip.SummariseReview))
        relaunched.checkForExistingTables(hasTables = true)
        assertEquals(Step.StartEditing, relaunched.onboarding.current)
    }
}
