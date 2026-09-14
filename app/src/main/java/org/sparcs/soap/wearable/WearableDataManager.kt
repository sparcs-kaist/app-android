package org.sparcs.soap.wearable

import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableDataManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TIMETABLE_PATH = "/timetable/current"
        private const val TIMETABLE_KEY = "timetable_json"
        private const val SEMESTER_KEY = "semester_json"
        private const val LAST_SENT_PREFS = "wearable_last_sent"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val lastSent = context.getSharedPreferences(LAST_SENT_PREFS, Context.MODE_PRIVATE)

    fun sendTimetableToWatch(timetable: Timetable, semester: Semester? = null) {
        val watchModel = timetable.toWatchModel(TimetableThemeStore.selectedTheme(context))
        val semesterJson = semester?.let { json.encodeToString(it.toWatchModel(context)) }
        send(json.encodeToString(watchModel), semesterJson)
    }

    /**
     * Re-sends the last payload recolored with the selected theme. The watch cannot read the phone's
     * theme, so a theme change has to be pushed; refetching the timetable is not needed for that.
     */
    fun resendWithCurrentTheme() {
        val stored = lastSent.getString(TIMETABLE_KEY, null) ?: return
        val recolored = runCatching {
            json.decodeFromString<WatchTimetable>(stored).themed(TimetableThemeStore.selectedTheme(context))
        }.getOrElse {
            Timber.e(it, "Failed to recolor the stored watch timetable")
            return
        }
        send(json.encodeToString(recolored), lastSent.getString(SEMESTER_KEY, null))
    }

    private fun send(timetableJson: String, semesterJson: String?) {
        lastSent.edit {
            putString(TIMETABLE_KEY, timetableJson)
            if (semesterJson != null) putString(SEMESTER_KEY, semesterJson)
        }

        val putDataMapReq = PutDataMapRequest.create(TIMETABLE_PATH)
        putDataMapReq.dataMap.putString(TIMETABLE_KEY, timetableJson)
        if (semesterJson != null) putDataMapReq.dataMap.putString(SEMESTER_KEY, semesterJson)
        putDataMapReq.dataMap.putLong("timestamp", System.currentTimeMillis())

        val putDataReq = putDataMapReq.asPutDataRequest()
        putDataReq.setUrgent()

        val dataClient = Wearable.getDataClient(context)
        dataClient.putDataItem(putDataReq)
            .addOnSuccessListener {
                Timber.d("Successfully sent timetable to watch")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Failed to send timetable to watch")
            }
    }
}

private fun Semester.toWatchModel(context: Context): WatchSemester {
    val label = runCatching {
        "$year ${context.getString(semesterType.rawValue)}"
    }.getOrElse {
        "$year ${semesterType.name}"
    }
    return WatchSemester(
        name = label,
        beginDateMillis = beginDate.time,
        endDateMillis = endDate.time
    )
}
