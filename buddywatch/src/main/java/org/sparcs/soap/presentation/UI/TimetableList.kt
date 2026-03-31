package org.sparcs.soap.presentation.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.data.models.Lecture
import org.sparcs.soap.data.models.LectureClass
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.presentation.theme.SoapTheme
import java.util.Calendar

private fun getTodayLectures(timetable: Timetable?): List<Pair<Lecture, LectureClass>> {
    val dayOfWeekString = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "MON"
        Calendar.TUESDAY -> "TUE"
        Calendar.WEDNESDAY -> "WED"
        Calendar.THURSDAY -> "THU"
        Calendar.FRIDAY -> "FRI"
        else -> ""
    }

    return timetable?.lectures?.flatMap { lecture ->
        lecture.classes
            .filter { it.day == dayOfWeekString }
            .map { lecture to it }
    }?.sortedBy { it.second.begin } ?: emptyList()
}

private fun getDayResource(): Int = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
    Calendar.MONDAY -> R.string.monday
    Calendar.TUESDAY -> R.string.tuesday
    Calendar.WEDNESDAY -> R.string.wednesday
    Calendar.THURSDAY -> R.string.thursday
    Calendar.FRIDAY -> R.string.friday
    Calendar.SATURDAY -> R.string.saturday
    Calendar.SUNDAY -> R.string.sunday
    else -> R.string.monday
}

@Composable
fun TimetableList(timetable: Timetable?) {
    val listState = rememberScalingLazyListState()
    val todayLectures = remember(timetable) { getTodayLectures(timetable) }
    val dayRes = remember { getDayResource() }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            HeaderSection(dayRes)
        }

        when {
            timetable == null -> item { EmptyStateMessage(R.string.no_sync) }
            todayLectures.isEmpty() -> item { EmptyStateMessage(R.string.no_more_classes) }
            else -> items(todayLectures) { (lecture, cl) ->
                LectureItem(lecture, cl)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection(dayRes: Int) {
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

@Composable
private fun EmptyStateMessage(messageRes: Int) {
    Text(
        text = stringResource(messageRes),
        style = MaterialTheme.typography.body2,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        textAlign = TextAlign.Center
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun TimetableListPreview() {
    SoapTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            TimetableList(timetable = Timetable.mock())
        }
    }
}