package org.sparcs.soap.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.*
import org.sparcs.soap.R
import org.sparcs.soap.data.Lecture
import org.sparcs.soap.data.LectureClass
import org.sparcs.soap.data.Timetable
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.presentation.theme.SoapTheme
import java.util.*

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
            val dayRes = when (today.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> R.string.monday
                Calendar.TUESDAY -> R.string.tuesday
                Calendar.WEDNESDAY -> R.string.wednesday
                Calendar.THURSDAY -> R.string.thursday
                Calendar.FRIDAY -> R.string.friday
                Calendar.SATURDAY -> R.string.saturday
                Calendar.SUNDAY -> R.string.sunday
                else -> R.string.monday
            }
            Column(
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(dayRes),
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = stringResource(R.string.today_schedule),
                    style = MaterialTheme.typography.title3
                )
            }
        }

        if (timetable == null) {
            item {
                Text(
                    text = stringResource(R.string.no_sync),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else if (todayLectures.isEmpty()) {
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
            lecture.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF4A90E2)
        } catch (e: Exception) {
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
    fun Int.toTime(): String = String.format("%02d:%02d", (this / 60) % 24, this % 60)
    return "${begin.toTime()} - ${end.toTime()}"
}