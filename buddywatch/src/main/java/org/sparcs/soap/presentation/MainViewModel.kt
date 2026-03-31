package org.sparcs.soap.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.Timetable
import java.util.Calendar

class MainViewModel(watchDataStore: WatchDataStore) : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }

    private val dateTicker = flow {
        while (true) {
            emit(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            delay(60_000)
        }
    }.distinctUntilChanged()

    val timetableState: StateFlow<Timetable?> = combine(
        watchDataStore.timetableJsonFlow,
        dateTicker
    ) { jsonString, _ ->
        jsonString?.let {
            try {
                json.decodeFromString<Timetable>(it)
            } catch (e: Exception) {
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}