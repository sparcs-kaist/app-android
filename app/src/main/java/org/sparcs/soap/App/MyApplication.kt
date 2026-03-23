package org.sparcs.soap.App

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.FCMUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.BuildConfig
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var authUseCase: AuthUseCaseProtocol

    @Inject
    lateinit var userUseCase: UserUseCaseProtocol

    @Inject
    lateinit var fcmUseCase: FCMUseCaseProtocol

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        setupLogger()
        setupFirebase()

//        applicationScope.launch {
//            try {
//                authUseCase.refreshAccessToken(force = true)
//                userUseCase.fetchUsers()
//                FirebaseMessaging.getInstance().token
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val token = task.result
//                            applicationScope.launch {
//                                try {
//                                    fcmUseCase.register(token)
//                                } catch (e: Exception) {
//                                    Timber.e(e, "FCM registration failed")
//                                }
//                            }
//                        }
//                    }
//            } catch (e: Exception) {
//                Timber.e(e, "Initial data fetch failed")
//            }
//        }
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