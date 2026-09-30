package org.sparcs.soap.app.features.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.repositories.settings.SettingsRepositoryProtocol
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.logScreen
import javax.inject.Inject

@AndroidEntryPoint
class GatewayActivity : ComponentActivity() {

    @Inject lateinit var analyticsService: AnalyticsServiceProtocol

    @Inject
    lateinit var settingsRepository: SettingsRepositoryProtocol

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var themeModeState by mutableStateOf<String?>(null)

        splashScreen.setKeepOnScreenCondition { themeModeState == null }

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            themeModeState = settingsRepository.themeMode.first()
        }

        analyticsService.logScreen("Gateway")

        setContent {
            val themeMode = themeModeState

            if (themeMode != null) {
                val isDarkMode = when (themeMode) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }

                LaunchedEffect(Unit) {
                    delay(1000)
                    startActivity(Intent(this@GatewayActivity, MainActivity::class.java))
                    finish()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDarkMode) colorResource(R.color.splash_background_night)
                            else colorResource(R.color.splash_background)
                        )
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_buddy_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(130.dp, 120.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}