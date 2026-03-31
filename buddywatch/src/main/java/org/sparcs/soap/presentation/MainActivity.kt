package org.sparcs.soap.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.presentation.UI.TimetableList
import org.sparcs.soap.presentation.theme.SoapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        val watchDataStore = WatchDataStore(applicationContext)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(watchDataStore) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
        val mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            WearApp(mainViewModel)
        }
    }
}

@Composable
fun WearApp(viewModel: MainViewModel) {
    val timetable by viewModel.timetableState.collectAsState()

    SoapTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            TimeText()
            TimetableList(timetable)
        }
    }
}