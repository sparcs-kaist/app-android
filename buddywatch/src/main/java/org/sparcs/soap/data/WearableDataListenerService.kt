package org.sparcs.soap.data

import android.content.ComponentName
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.sparcs.soap.complication.CreditsComplicationService
import org.sparcs.soap.complication.DDayComplicationService
import org.sparcs.soap.complication.UpcomingClassComplicationService
import org.sparcs.soap.tile.DDayTileService
import org.sparcs.soap.tile.MainTileService
import timber.log.Timber

class WearableDataListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var watchDataStore: WatchDataStore
    private val creditUpdates = Channel<String?>(Channel.UNLIMITED)

    override fun onCreate() {
        super.onCreate()
        watchDataStore = WatchDataStore(applicationContext)
        scope.launch {
            // Apply in delivery order: parallel launches can resurrect a value after a clear.
            for (summary in creditUpdates) {
                watchDataStore.saveCreditSummaryJson(summary)
                ComplicationDataSourceUpdateRequester.create(
                    this@WearableDataListenerService,
                    ComponentName(this@WearableDataListenerService, CreditsComplicationService::class.java),
                ).requestUpdateAll()
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            dataEvents.forEach { event ->
                if (event.dataItem.uri.path == "/semester/current") {
                    val semester = if (event.type == DataEvent.TYPE_CHANGED) {
                        DataMapItem.fromDataItem(event.dataItem).dataMap.getString("semester_json")
                    } else null
                    scope.launch {
                        watchDataStore.saveCurrentSemesterJson(semester)
                        refreshSurfaces()
                    }
                    return@forEach
                }
                if (event.dataItem.uri.path == "/credits/summary") {
                    val summary = if (event.type == DataEvent.TYPE_CHANGED) {
                        DataMapItem.fromDataItem(event.dataItem).dataMap.getString("credit_summary_json")
                    } else null
                    creditUpdates.trySend(summary)
                    return@forEach
                }
                if (event.type == DataEvent.TYPE_DELETED && event.dataItem.uri.path == "/timetable/current") {
                    scope.launch {
                        watchDataStore.clearTimetable()
                        refreshSurfaces()
                    }
                } else if (event.type == DataEvent.TYPE_CHANGED) {
                    val path = event.dataItem.uri.path
                    if (path == "/timetable/current") {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val timetableJson = dataMap.getString("timetable_json")
                        val semesterJson = dataMap.getString("semester_json")

                        scope.launch {
                            timetableJson?.let {
                                watchDataStore.saveTimetableJson(it)
                            }

                            semesterJson?.let {
                                watchDataStore.saveSemesterJson(it)
                            }

                            if (timetableJson != null || semesterJson != null) {
                                refreshSurfaces()
                                Timber.d("Data updated. Timetable: ${timetableJson != null}, Semester: ${semesterJson != null}")
                            }
                        }
                    }
                }
            }
        } finally {
            dataEvents.release()
        }
    }

    private fun refreshSurfaces() {
        TileService.getUpdater(this).requestUpdate(MainTileService::class.java)
        TileService.getUpdater(this).requestUpdate(DDayTileService::class.java)
        listOf(UpcomingClassComplicationService::class.java, DDayComplicationService::class.java).forEach { service ->
            ComplicationDataSourceUpdateRequester.create(this, ComponentName(this, service)).requestUpdateAll()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        creditUpdates.close()
        scope.cancel()
    }
}
