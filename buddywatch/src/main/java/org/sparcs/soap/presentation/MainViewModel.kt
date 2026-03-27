package org.sparcs.soap.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.sparcs.soap.data.Timetable
import org.sparcs.soap.data.WatchDataStore

class MainViewModel(private val watchDataStore: WatchDataStore) : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }

    val timetableState: StateFlow<Timetable?> = watchDataStore.timetableJsonFlow
        .map { jsonString ->
            jsonString?.let {
                try {
                    json.decodeFromString<Timetable>(it)
                } catch (e: Exception) {
                    null
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}