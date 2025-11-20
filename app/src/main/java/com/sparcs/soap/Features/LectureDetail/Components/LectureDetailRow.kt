package com.sparcs.soap.Features.LectureDetail.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB

@Composable
fun LectureDetailRow(
    title: String,
    description: String
){
    Column{
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.grayBB,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        LectureDetailRow(
            title = "Code",
            description = "12345"
        )
    }
}