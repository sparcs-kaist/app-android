package org.sparcs.soap.app

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var userUseCase: UserUseCaseProtocol

    override fun onCreate() {
        super.onCreate()

        setupLogger()
        setupFirebase()

        try {
            val name = runCatching { userUseCase.taxiUser?.name }.getOrNull()
            val email = runCatching { userUseCase.taxiUser?.email }.getOrNull()
            val phone = runCatching { userUseCase.taxiUser?.phoneNumber }.getOrNull()
            val memberId = userUseCase.feedUser?.id
            ChannelManager.initialize(this, BuildConfig.CHANNEL_PLUGIN_KEY, name, email, phone, memberId)
        } catch (e: Exception) {
            Timber.e(e, "ChannelManager init from MyApplication failed")
        }
    }

    private fun setupLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun setupFirebase() {
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)

        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        }
    }
}