package org.sparcs.soap.app.domain.helpers

import android.content.Context
import org.sparcs.soap.app.domain.models.otl.Semester

/** The app's last selection, independent of widget and watch preferences. */
class TimetableSelectionStore(context: Context) {
    private val preferences = context.getSharedPreferences("timetable_selection", Context.MODE_PRIVATE)

    data class Selection(val semesterID: String, val timetableID: Int?)

    val selection: Selection?
        get() = runCatching {
            val semesterID = preferences.getString("semester_id", null) ?: return null
            val timetableID = preferences.getInt("timetable_id", -1).takeIf { it >= 0 }
            Selection(semesterID, timetableID)
        }.getOrNull()

    fun save(semester: Semester, timetableID: Int?) {
        preferences.edit()
            .putString("semester_id", semester.id)
            .putInt("timetable_id", timetableID ?: -1)
            .apply()
    }

    fun clear() = preferences.edit().clear().apply()
}
