package org.sparcs.soap.presentation.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.data.models.Lecture
import org.sparcs.soap.data.models.LectureClass
import org.sparcs.soap.presentation.theme.SoapTheme
import org.sparcs.soap.shared.formatTimeRange


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

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Composable
private fun LectureItemPreview() {
    SoapTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LectureItem(
                lecture = Lecture.mock(),
                cl = LectureClass.mock()
            )
        }
    }
}