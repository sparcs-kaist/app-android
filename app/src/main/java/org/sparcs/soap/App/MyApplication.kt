package org.sparcs.soap.App

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        setupLogger()

        applicationScope.launch {
            try {
                authUseCase.refreshAccessToken(force = true)
                userUseCase.fetchUsers()
            } catch (e: Exception) {
                Timber.e(e, "Initial data fetch failed")
            }
        }
    }

    private fun setupLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}