package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

//Todo : 로딩창

@Composable
fun TimetableSummary() {

    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                SmallSummary("BR", 0)
                SmallSummary("BE", 0)
            }
            Column {
                SmallSummary("MR", 3)
                SmallSummary("ME", 9)
            }
            Column {
                SmallSummary("HSE", 0)
                SmallSummary("ETC", 3)
            }
            BigSummary("Credit", "15")
            BigSummary("AU", "0")
            BigSummary("Grade", "A")
            BigSummary("Load", "A-")
            BigSummary("Speech", "A")
        }
    }
}

@Composable
fun SmallSummary(
    type : String,
    count : Int
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val typeWidth = if (type.length == 2) 24.dp else 30.dp
        val countType = if (type.length == 2) 16.dp else 20.dp

        Text(
            text = type,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(typeWidth)
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(countType)
        )
    }
}

@Composable
fun BigSummary(
    label : String,
    grade : String
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Text(
            text = grade,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
@Preview
private fun Preview(){
    TimetableSummary()
}