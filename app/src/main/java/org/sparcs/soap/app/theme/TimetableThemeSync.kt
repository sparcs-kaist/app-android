package org.sparcs.soap.app.theme

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore
import org.sparcs.soap.wearable.WearableDataManager
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableWidget
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.BuddyUpcomingClassWidget
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the surfaces outside the app's composition in step with the stored themes: the watch, which
 * follows the app's selected theme through the payload the phone sends it, and the home screen
 * widgets, which each pick their own theme but still have to redraw when its colors are edited.
 */
@Singleton
class TimetableThemeSync @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val wearableDataManager: WearableDataManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false
    // Holds the preference listener alive: SharedPreferences only keeps a weak reference to it.
    private var observer: (() -> Unit)? = null

    /** Reading the stored themes touches disk, so registration stays off the startup thread. */
    fun start() {
        if (started) return
        started = true
        scope.launch {
            var current: TimetableThemeStore.State? = null
            observer = TimetableThemeStore(context).observe { state ->
                val previous = current
                current = state
                // observe() reports the current state on registration; only later changes need a push.
                if (previous != null && previous != state) push()
            }
        }
    }

    private fun push() {
        scope.launch {
            runCatching { TimetableWidget().updateAll(context) }
                .onFailure { Timber.e(it, "Timetable widget theme update failed") }
            runCatching { BuddyUpcomingClassWidget().updateAll(context) }
                .onFailure { Timber.e(it, "Upcoming class widget theme update failed") }
            runCatching { wearableDataManager.resendWithCurrentTheme() }
                .onFailure { Timber.e(it, "Watch theme update failed") }
        }
    }
}
