package org.sparcs.soap.app.shared.tips

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipStore internal constructor(
    private val preferences: SharedPreferences,
    private val clock: Clock,
) {
    @Inject constructor(@ApplicationContext context: Context) :
        this(context.getSharedPreferences(FILE, Context.MODE_PRIVATE), Clock.system(ZoneId.of("Asia/Seoul")))

    val onboarding = TimetableOnboarding(preferences)

    private val mutableRevision = MutableStateFlow(0)
    val revision: StateFlow<Int> = mutableRevision.asStateFlow()

    private val shownThisLaunch = mutableSetOf<BuddyTip>()

    init {
        if (preferences.getBoolean(KEY_RESET_ON_LAUNCH, false)) resetNow()
    }

    fun isEligible(tip: BuddyTip): Boolean {
        if (isInvalidated(tip)) return false
        tip.maxDisplayCount?.let { if (displayCount(tip) >= it && tip !in shownThisLaunch) return false }
        if (!rulesAllow(tip)) return false
        return tip.isOnboarding || tip in shownThisLaunch || showsTipsImmediately || !hasShownNewTipToday()
    }

    fun markShown(tip: BuddyTip) {
        if (!shownThisLaunch.add(tip)) return
        preferences.edit {
            putInt(KEY_DISPLAY_COUNT + tip.id, displayCount(tip) + 1)
            if (!tip.isOnboarding) putString(KEY_LAST_NEW_TIP_DAY, today().toString())
        }
    }

    fun invalidate(tip: BuddyTip) {
        preferences.edit { putBoolean(KEY_INVALIDATED + tip.id, true) }
        tip.onboardingStep?.let(onboarding::complete)
        changed()
    }

    fun donate(event: TipEvent) {
        preferences.edit { putInt(KEY_EVENT + event.id, eventCount(event) + 1) }
        changed()
    }

    fun completeOnboarding(step: TimetableOnboarding.Step) = notifying { onboarding.complete(step) }

    fun skipPreviewSteps() = notifying { onboarding.skipPreviewSteps() }

    fun checkForExistingTables(hasTables: Boolean) = notifying { onboarding.checkForExistingTables(hasTables) }

    fun lectureAdded() = notifying { onboarding.lectureAdded() }

    var showsTipsImmediately: Boolean
        get() = preferences.getBoolean(KEY_SHOWS_IMMEDIATELY, false)
        set(value) = preferences.edit { putBoolean(KEY_SHOWS_IMMEDIATELY, value) }

    val isResetPending: Boolean get() = preferences.getBoolean(KEY_RESET_ON_LAUNCH, false)

    fun requestReset() {
        preferences.edit { putBoolean(KEY_RESET_ON_LAUNCH, true) }
        onboarding.requestReplay()
        changed()
    }

    private fun rulesAllow(tip: BuddyTip): Boolean {
        val step = onboarding.current
        return when (tip) {
            BuddyTip.StartEditing, BuddyTip.AddEvent ->
                step == tip.onboardingStep && onboarding.hasCheckedForExistingTables
            BuddyTip.ResizePreview, BuddyTip.TogglePreview -> step == tip.onboardingStep
            BuddyTip.AddFromDetail -> step >= TimetableOnboarding.Step.AddLecture
            BuddyTip.PeekLecture -> step == TimetableOnboarding.Step.Done && eventCount(TipEvent.LectureOpened) >= 2
            BuddyTip.ShareTimetable -> step == TimetableOnboarding.Step.Done
            BuddyTip.QuickFill -> eventCount(TipEvent.ActivityCreationOpened) >= 2
            BuddyTip.PostTools -> eventCount(TipEvent.PostOpened) >= 3
            BuddyTip.ThemeActions -> eventCount(TipEvent.ThemeSettingsOpened) >= 2
            BuddyTip.SummariseReview, BuddyTip.RideInfo -> true
        }
    }

    private fun resetNow() {
        val keep = setOf(KEY_REPLAY_ONBOARDING, KEY_SHOWS_IMMEDIATELY)
        preferences.edit { preferences.all.keys.filterNot { it in keep }.forEach(::remove) }
    }

    private fun isInvalidated(tip: BuddyTip) = preferences.getBoolean(KEY_INVALIDATED + tip.id, false)

    private fun displayCount(tip: BuddyTip) = preferences.getInt(KEY_DISPLAY_COUNT + tip.id, 0)

    private fun eventCount(event: TipEvent) = preferences.getInt(KEY_EVENT + event.id, 0)

    private fun hasShownNewTipToday() = preferences.getString(KEY_LAST_NEW_TIP_DAY, null) == today().toString()

    private fun today(): LocalDate = LocalDate.now(clock)

    private inline fun notifying(block: () -> Unit) {
        block()
        changed()
    }

    private fun changed() {
        mutableRevision.value++
    }

    private companion object {
        const val FILE = "buddy_tips"
        const val KEY_INVALIDATED = "tips.invalidated."
        const val KEY_DISPLAY_COUNT = "tips.displayCount."
        const val KEY_EVENT = "tips.event."
        const val KEY_LAST_NEW_TIP_DAY = "tips.lastNewTipDay"
        const val KEY_RESET_ON_LAUNCH = "tips.debug.resetOnLaunch"
        const val KEY_REPLAY_ONBOARDING = "tips.debug.replaysOnboarding"
        const val KEY_SHOWS_IMMEDIATELY = "tips.debug.showsTipsImmediately"
    }
}
