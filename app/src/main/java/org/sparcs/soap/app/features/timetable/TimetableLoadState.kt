package org.sparcs.soap.app.features.timetable

import androidx.annotation.StringRes
import java.util.Date

data class TimetableLoadState(
    val isOffline: Boolean = false,
    val isShowingSavedData: Boolean = false,
    val refreshFailed: Boolean = false,
    val isRefreshing: Boolean = false,
    val lastUpdated: Date? = null,
    @param:StringRes val loadError: Int? = null,
) {
    val isReadOnly: Boolean get() = isOffline || isShowingSavedData || refreshFailed
}
