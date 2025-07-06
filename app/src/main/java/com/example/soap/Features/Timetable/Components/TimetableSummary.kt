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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

//Todo : 로딩창

@Composable
fun TimetableSummary() {

    Card(
        colors = CardDefaults.cardColors(MaterialTheme.soapColors.surface),
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
                SmallSummary(stringResource(R.string.br), 0)
                SmallSummary(stringResource(R.string.be), 0)
            }
            Column {
                SmallSummary(stringResource(R.string.mr), 3)
                SmallSummary(stringResource(R.string.me), 9)
            }
            Column {
                SmallSummary(stringResource(R.string.hse), 0)
                SmallSummary(stringResource(R.string.etc), 3)
            }
            BigSummary(stringResource(R.string.credit), "15")
            BigSummary(stringResource(R.string.au), "0")
            BigSummary(stringResource(R.string.grade), "A")
            BigSummary(stringResource(R.string.load), "A-")
            BigSummary(stringResource(R.string.speech), "A")
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
        val typePadding = if (type.length == 2) 4.dp else 2.dp
        val countType = if (type.length == 2) 16.dp else 20.dp

        Text(
            text = type,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = typePadding)
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
    SoapTheme { TimetableSummary() }
}