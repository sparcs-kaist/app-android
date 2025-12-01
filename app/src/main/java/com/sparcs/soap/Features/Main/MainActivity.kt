package com.sparcs.soap.Features.Main

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sparcs.soap.Domain.Services.AuthenticationCallbackHandler
import com.sparcs.soap.Features.NavigationBar.MainTabBar
import com.sparcs.soap.Features.Settings.SettingsViewModel
import com.sparcs.soap.Features.SignIn.SignInView
import com.sparcs.soap.InAppUpdateHelper
import com.sparcs.soap.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint


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

                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                when {
                    isLoading -> {/*TODO 아이콘 로딩 화면*/}
                    isAuthenticated == true -> {
                        MainTabBar()
                    }
                    else -> {
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
    }
}