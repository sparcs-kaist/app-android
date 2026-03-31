package org.sparcs.soap.data

import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sparcs.soap.tile.MainTileService
import timber.log.Timber

class WearableDataListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var watchDataStore: WatchDataStore

    override fun onCreate() {
        super.onCreate()
        watchDataStore = WatchDataStore(applicationContext)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            dataEvents.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val path = event.dataItem.uri.path
                    if (path == "/timetable/current") {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val json = dataMap.getString("timetable_json")
                        if (json != null) {
                            scope.launch {
                                watchDataStore.saveTimetableJson(json)
                                TileService.getUpdater(applicationContext)
                                    .requestUpdate(MainTileService::class.java)
                                Timber.d("Received timetable from phone and requested tile update")
                            }
                        }
                    }
                }
            }
        } finally {
            dataEvents.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}