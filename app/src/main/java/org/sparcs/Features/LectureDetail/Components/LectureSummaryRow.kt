package org.sparcs.Features.LectureDetail.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.ui.theme.Theme
import org.sparcs.ui.theme.darkGray
import org.sparcs.ui.theme.grayBB

@Composable
fun LectureSummaryRow(
    title: String,
    description: String
){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.grayBB
        )
        Text(
            text = description,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.darkGray
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { LectureSummaryRow(title = "Language", description = "EN") }
}