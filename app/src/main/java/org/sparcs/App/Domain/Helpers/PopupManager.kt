package org.sparcs.App.Domain.Helpers

import android.content.Context

object PopupManager {
    private const val PREF_NAME = "popup_prefs"
    private const val KEY_LAST_CLOSED_TIME = "last_closed_time"

    fun saveIgnoreTimestamp(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_CLOSED_TIME, System.currentTimeMillis()).apply()
    }

    fun shouldShowPopup(context: Context, days: Int): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastClosedTime = prefs.getLong(KEY_LAST_CLOSED_TIME, 0L)

        if (lastClosedTime == 0L) return true

        val diff = System.currentTimeMillis() - lastClosedTime
        val daysInMillis = days * 24 * 60 * 60 * 1000L

        return diff > daysInMillis
    }
}