package org.sparcs.soap.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.Timetable

class MainViewModel(private val watchDataStore: WatchDataStore) : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }

    val timetableState = watchDataStore.timetableJsonFlow.map { value ->
        value?.let { runCatching { json.decodeFromString<Timetable>(it) }.getOrNull() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val viewOption = watchDataStore.viewOptionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectViewOption(option: LectureViewOption) {
        viewModelScope.launch { watchDataStore.saveViewOption(option) }
    }
}
