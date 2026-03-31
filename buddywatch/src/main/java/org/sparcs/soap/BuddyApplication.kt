package org.sparcs.soap

import android.app.Application
import timber.log.Timber

class BuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}