package org.sparcs.App.Features.Main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.sparcs.App.Domain.Repositories.Settings.SettingsRepository
import org.sparcs.R
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var themeModeState by mutableStateOf<String?>(null)

        splashScreen.setKeepOnScreenCondition { themeModeState == null }

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            themeModeState = settingsRepository.themeMode.first()
        }

        setContent {
            val themeMode = themeModeState

            if (themeMode != null) {
                val isDarkMode = when (themeMode) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }

                LaunchedEffect(Unit) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
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

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.sponsored_by_splash_screen),
                            color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Image(
                            painter = painterResource(
                                if (isDarkMode) R.drawable.ic_mobis_logo_night else R.drawable.ic_mobis_logo
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp, 45.dp)
                        )
                    }
                }
            }
        }
    }
}