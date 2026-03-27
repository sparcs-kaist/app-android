package org.sparcs.soap.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import org.sparcs.soap.R
import org.sparcs.soap.data.Lecture
import org.sparcs.soap.data.LectureClass
import org.sparcs.soap.data.Timetable
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.presentation.theme.SoapTheme
import java.util.Calendar
import java.util.Locale

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

@Composable
fun TimetableList(timetable: Timetable?) {
    val listState = rememberScalingLazyListState()
    
    val todayLectures = remember(timetable) {
        val now = Calendar.getInstance()
        val dayOfWeekString = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            else -> ""
        }

        timetable?.lectures?.flatMap { lecture ->
            lecture.classes
                .filter { it.day == dayOfWeekString }
                .map { lecture to it }
        }?.sortedBy { it.second.begin } ?: emptyList()
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            val today = Calendar.getInstance()
            val dayName = when (today.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> stringResource(R.string.monday)
                Calendar.TUESDAY -> stringResource(R.string.tuesday)
                Calendar.WEDNESDAY -> stringResource(R.string.wednesday)
                Calendar.THURSDAY -> stringResource(R.string.thursday)
                Calendar.FRIDAY -> stringResource(R.string.friday)
                Calendar.SATURDAY -> stringResource(R.string.saturday)
                Calendar.SUNDAY -> stringResource(R.string.sunday)
                else -> ""
            }
            Column(
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = stringResource(R.string.today_schedule),
                    style = MaterialTheme.typography.title3
                )
            }
        }

        if (todayLectures.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_more_classes),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(todayLectures) { (lecture, cl) ->
                LectureItem(lecture, cl)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LectureItem(lecture: Lecture, cl: LectureClass) {
    val accentColor = remember(lecture.color) {
        try {
            lecture.color?.let { Color(it.toColorInt()) } ?: Color(0xFF4A90E2)
        } catch (_: IllegalArgumentException) {
            Color(0xFF4A90E2)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .background(accentColor, shape = MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = lecture.name,
                style = MaterialTheme.typography.button.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = formatTimeRange(cl.begin, cl.end),
                style = MaterialTheme.typography.caption1,
                color = accentColor
            )
            Text(
                text = cl.location,
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }
    }
}

private fun formatTimeRange(begin: Int, end: Int): String {
    fun Int.toTime(): String = String.format(Locale.getDefault(), "%02d:%02d", (this / 60) % 24, this % 60)
    return "${begin.toTime()} - ${end.toTime()}"
}

@Composable
private fun Box(modifier: Modifier, contentAlignment: Alignment = Alignment.TopStart, content: @Composable () -> Unit = {}) {
    androidx.compose.foundation.layout.Box(modifier, contentAlignment) {
        content()
    }
}