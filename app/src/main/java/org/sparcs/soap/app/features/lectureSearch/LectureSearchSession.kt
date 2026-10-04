package org.sparcs.soap.app.features.lectureSearch

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LectureSearchSession @Inject constructor(private val savedState: SavedStateHandle) : ViewModel() {
    var showPreview by mutableStateOf(savedState["showPreview"] ?: false)
        private set
    var previewHeight by mutableFloatStateOf(savedState["previewHeight"] ?: 300f)
        private set
    val resultsScroll = LazyListState()

    fun showPreview(show: Boolean) {
        showPreview = show
        savedState["showPreview"] = show
    }

    fun resizePreview(height: Float) {
        previewHeight = height
        savedState["previewHeight"] = height
    }
}
