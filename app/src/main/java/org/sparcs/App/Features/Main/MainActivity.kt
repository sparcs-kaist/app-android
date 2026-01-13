package org.sparcs.App.Features.Main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import org.sparcs.App.Domain.Services.AuthenticationCallbackHandler
import org.sparcs.App.Features.NavigationBar.MainTabBar
import org.sparcs.App.Features.Settings.SettingsViewModel
import org.sparcs.App.Features.SignIn.SignInView
import org.sparcs.App.InAppUpdateHelper
import org.sparcs.App.theme.ui.Theme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var helper: InAppUpdateHelper

    private val launcher = registerForActivityResult(
       ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_CANCELED) {
            Log.d("MainActivity", "CANCELED")
        }
        if (result.resultCode != RESULT_OK) {
            Log.w("MainActivity", "FAILED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        helper = InAppUpdateHelper(this, launcher, 4, true)
        helper.check()

        intent?.data?.let { uri ->
            AuthenticationCallbackHandler.handleUri(uri)
        }

        setContent {
            val darkMode by settingsViewModel.darkModeSetting.collectAsState(initial = null)
            val useDarkTheme = darkMode ?: isSystemInDarkTheme()

            Theme(darkTheme = useDarkTheme) {
                val mustUpdate by viewModel.mustUpdate.collectAsState()
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                LaunchedEffect(Unit) {
                    val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
                    viewModel.checkVersion(currentVersion)
                }

                LaunchedEffect(mustUpdate) {
                    if (mustUpdate) {
                        helper.forceStart()
                    }
                }

                if (isLoading || isAuthenticated == null) {
                    // MARK: THIS PLAYS CRUCIAL ROLE HIDING SIGN IN VIEW ON LOADING
                } else {
                    if (isAuthenticated == true) {
                        MainTabBar()
                    } else {
                        SignInView()
                    }
                }
            }
        }

    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            AuthenticationCallbackHandler.handleUri(uri)
        }
    }
    override fun onResume() {
        super.onResume()
        helper.resumeCheck()
        viewModel.checkAuthOnResume()
    }
}