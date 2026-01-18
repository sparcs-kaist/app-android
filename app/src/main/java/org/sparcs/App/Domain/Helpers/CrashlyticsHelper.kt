package org.sparcs.App.Domain.Helpers

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsHelper @Inject constructor() {

    fun recordException(error: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(error)
    }
}
