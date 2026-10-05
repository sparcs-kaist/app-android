package org.sparcs.soap.app.shared.sharing

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ShareState(
    val preparing: Boolean = false,
    val failed: Boolean = false,
    val request: ShareRequest? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class ShareViewModel @Inject constructor(private val imageStore: ShareImageStore) : ViewModel() {
    private val mutableState = MutableStateFlow(ShareState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    fun prepare(target: ShareTarget, content: ShareContent, capture: suspend () -> Bitmap) {
        val current = mutableState.value
        if (current.preparing || current.request != null || (target == ShareTarget.Save && current.saved)) return
        mutableState.value = ShareState(preparing = true, saved = current.saved)
        job = viewModelScope.launch {
            try {
                val copyingText =
                    target == ShareTarget.Copy && (content.copyText != null || content.link != null)
                val uri = when {
                    copyingText -> null
                    target == ShareTarget.Save -> imageStore.saveToGallery(capture())
                    else -> imageStore.save(capture())
                }
                mutableState.value =
                    ShareState(request = ShareRequest(target, content, uri), saved = current.saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Unable to prepare share image")
                mutableState.value = ShareState(failed = true, saved = current.saved)
            }
        }
    }

    fun consumeRequest(launched: Boolean) {
        val state = mutableState.value
        mutableState.value =
            ShareState(saved = state.saved || (launched && state.request?.target == ShareTarget.Save))
    }

    fun reset() {
        job?.cancel()
        mutableState.value = ShareState()
    }
}
