package org.sparcs.soap.app.domain.services

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Whether Buddy is on screen, for what only needs to run while it is. */
interface AppForegroundMonitor {
    val isForeground: StateFlow<Boolean>
}

@Singleton
class ProcessForegroundMonitor @Inject constructor() : AppForegroundMonitor {
    private val _isForeground = MutableStateFlow(false)
    override val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    init {
        // The process's lifecycle can only be observed from the main thread, and this may be
        // created on another, such as by a push.
        val observe = {
            ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { owner, _ ->
                _isForeground.value = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            })
        }
        if (Looper.myLooper() == Looper.getMainLooper()) observe() else Handler(Looper.getMainLooper()).post(observe)
    }
}
