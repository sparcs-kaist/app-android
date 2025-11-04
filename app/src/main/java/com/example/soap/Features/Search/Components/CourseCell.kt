package com.example.soap.Features.Search.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.OTL.Course
import com.example.soap.Shared.Mocks.mock
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB

@Composable
fun CourseCell(
    course: Course,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = course.title.localized(),
                maxLines = 2,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = course.code,
                    color = MaterialTheme.colorScheme.grayBB,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = course.type.localized(),
                    color = MaterialTheme.colorScheme.grayBB,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }

        if (course.summary.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = course.summary,
                maxLines = 3,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.grayBB
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        CourseCell(Course.mock(), {})
    }
}
