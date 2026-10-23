package org.sparcs.soap.app.domain.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class ReviewSummarySettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("timetable", Context.MODE_PRIVATE)

    var summarisesAutomatically: Boolean
        get() = preferences.getBoolean(KEY, true)
        set(value) = preferences.edit().putBoolean(KEY, value).apply()

    fun observe(onChange: () -> Unit): () -> Unit {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> if (key == KEY) onChange() }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        return { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object { const val KEY = "timetable.summarisesReviewsAutomatically" }
}

@Composable
fun rememberSummarisesReviewsAutomatically(): State<Boolean> {
    val context = LocalContext.current
    val store = remember(context) { ReviewSummarySettingsStore(context) }
    val state = remember(store) { mutableStateOf(store.summarisesAutomatically) }
    DisposableEffect(store) {
        val stop = store.observe { state.value = store.summarisesAutomatically }
        onDispose(stop)
    }
    return state
}
