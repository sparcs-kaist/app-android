package org.sparcs.soap.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.presentation.theme.SoapTheme
import org.sparcs.soap.presentation.ui.LectureRootView
import java.time.LocalDateTime

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
    val viewOption by viewModel.viewOption.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val now by produceState(LocalDateTime.now(), lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                value = LocalDateTime.now()
                delay(60_000 - System.currentTimeMillis() % 60_000)
            }
        }
    }
    SoapTheme {
        viewOption?.let { option ->
            LectureRootView(timetable, option, now, viewModel::selectViewOption)
        }
    }
}
