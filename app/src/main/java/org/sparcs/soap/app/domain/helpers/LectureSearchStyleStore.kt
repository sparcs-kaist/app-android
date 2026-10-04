package org.sparcs.soap.app.domain.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.sparcs.soap.app.domain.models.otl.LectureSearchStyle

class LectureSearchStyleStore(context: Context) {
    private val defaultStyle = if (context.resources.configuration.smallestScreenWidthDp >= 600)
        LectureSearchStyle.Flexible else LectureSearchStyle.Fixed
    private val preferences = context.getSharedPreferences("timetable", Context.MODE_PRIVATE)
    var lectureSearchStyle: LectureSearchStyle
        get() = LectureSearchStyle.entries.firstOrNull { it.rawValue == preferences.getString(KEY, null) }
            ?: defaultStyle
        set(value) { preferences.edit().putString(KEY, value.rawValue).apply() }

    fun observe(onChange: () -> Unit): () -> Unit {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> if (key == KEY) onChange() }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        return { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object { const val KEY = "timetable.lectureSearchStyle" }
}

@Composable
fun rememberLectureSearchStyle(): MutableState<LectureSearchStyle> {
    val context = LocalContext.current
    val store = remember(context) { LectureSearchStyleStore(context) }
    val state = remember(store) { mutableStateOf(store.lectureSearchStyle) }
    DisposableEffect(store) {
        val stop = store.observe { state.value = store.lectureSearchStyle }
        onDispose(stop)
    }
    return state
}
