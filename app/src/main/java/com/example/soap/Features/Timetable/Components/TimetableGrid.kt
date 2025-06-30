package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Models.Types.DayType
import com.example.soap.ui.theme.SoapTheme


@Composable
fun TimetableGrid(){
    val scrollState = rememberScrollState()

    Box(Modifier.padding(vertical = 16.dp)) {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                //           .verticalScroll(scrollState)
            ) {
                DaysColumnHeader()
                TimesRowHeader()
            }

        }
    }
}

@Composable
fun DaysColumnHeader(){
    val defaultVisibleDays : List<DayType> = listOf(DayType.MON, DayType.TUE, DayType.WED, DayType.THU, DayType.FRI)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        defaultVisibleDays.forEach { day ->
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TimesRowHeader(){
    val defaultMinMinutes = 540 //9:00AM
    val defaultMaxMinutes = 1080 //6:00PM

    val minHour = defaultMinMinutes / 60
    val maxHour = defaultMaxMinutes / 60

    Column(
//        modifier = Modifier.fillMaxHeight(),
//        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        (minHour..maxHour).forEach { time ->
            Text(
                text = time.toString(),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { TimetableGrid() }
}