package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme

@Composable
fun CompactTimetableSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {},
            modifier = Modifier
                .width(24.dp)
                .padding(end = 4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_ios),
                contentDescription = "Select Previous Semester",
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Text(
            text = "Autumn 2024",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = {},
            modifier = Modifier
                .width(24.dp)
                .padding(end = 4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_forward_ios),
                contentDescription = "Select Next Semester",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "My Table",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = {}
        ) {
            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "Select Next Semester",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { CompactTimetableSelector() }
}