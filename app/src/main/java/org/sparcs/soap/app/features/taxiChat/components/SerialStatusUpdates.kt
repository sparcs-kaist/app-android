package org.sparcs.soap.app.features.taxiChat.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SerialStatusUpdates {
    private var latest: Job? = null
    private var generation = 0

    val isPending: Boolean get() = latest != null

    fun enqueue(scope: CoroutineScope, onSettled: () -> Unit, update: suspend () -> Unit) {
        val previous = latest
        val current = ++generation
        val job = scope.launch(start = CoroutineStart.LAZY) {
            previous?.join()
            update()
            if (current == generation) {
                latest = null
                onSettled()
            }
        }
        latest = job
        job.start()
    }
}
