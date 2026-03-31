package org.sparcs.soap.Wearable

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TIMETABLE_PATH = "/timetable/current"
        private const val TIMETABLE_KEY = "timetable_json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun sendTimetableToWatch(timetable: Timetable) {
        val watchModel = timetable.toWatchModel()
        val jsonString = json.encodeToString(watchModel)
        
        val putDataMapReq = PutDataMapRequest.create(TIMETABLE_PATH)
        putDataMapReq.dataMap.putString(TIMETABLE_KEY, jsonString)
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