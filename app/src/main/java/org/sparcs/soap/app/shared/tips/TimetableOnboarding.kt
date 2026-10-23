package org.sparcs.soap.app.shared.tips

import android.content.SharedPreferences
import androidx.core.content.edit

class TimetableOnboarding(private val preferences: SharedPreferences) {
    enum class Step { StartEditing, AddButton, ResizePreview, TogglePreview, AddLecture, Done }

    val current: Step
        get() = Step.entries.getOrNull(preferences.getInt(KEY_STEP, 0)) ?: Step.Done

    val hasCheckedForExistingTables: Boolean
        get() = preferences.getBoolean(KEY_CHECKED, false)

    fun complete(finished: Step) {
        if (current != finished) return
        val next = when (finished) {
            Step.StartEditing -> Step.AddButton
            Step.AddButton -> Step.ResizePreview
            Step.ResizePreview -> Step.TogglePreview
            Step.TogglePreview -> if (preferences.getBoolean(KEY_SKIPS_ADD_LECTURE, false)) Step.Done else Step.AddLecture
            Step.AddLecture, Step.Done -> Step.Done
        }
        preferences.edit { putInt(KEY_STEP, next.ordinal) }
    }

    fun skipPreviewSteps() {
        complete(Step.ResizePreview)
        complete(Step.TogglePreview)
    }

    fun checkForExistingTables(hasTables: Boolean) {
        if (hasCheckedForExistingTables) return
        val replays = preferences.getBoolean(KEY_REPLAY, false)
        preferences.edit {
            remove(KEY_REPLAY)
            if (hasTables && !replays && current < Step.ResizePreview) {
                putBoolean(KEY_SKIPS_ADD_LECTURE, true)
                putInt(KEY_STEP, Step.ResizePreview.ordinal)
            }
            putBoolean(KEY_CHECKED, true)
        }
    }

    fun lectureAdded() = complete(Step.AddLecture)

    internal fun requestReplay() {
        preferences.edit { putBoolean(KEY_REPLAY, true) }
    }

    internal companion object {
        const val PREFIX = "tips.onboarding."
        private const val KEY_STEP = PREFIX + "timetable.step"
        private const val KEY_CHECKED = PREFIX + "timetable.hasCheckedForExistingTables"
        private const val KEY_SKIPS_ADD_LECTURE = PREFIX + "timetable.skipsAddLecture"
        private const val KEY_REPLAY = "tips.debug.replaysOnboarding"
    }
}
