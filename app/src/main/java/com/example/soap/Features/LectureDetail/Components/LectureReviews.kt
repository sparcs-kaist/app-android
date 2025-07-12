package com.example.soap.Features.LectureDetail.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Models.TimeTable.Lecture
import com.example.soap.Models.TimeTable.gradeLetter
import com.example.soap.Models.TimeTable.loadLetter
import com.example.soap.Models.TimeTable.speechLetter
import com.example.soap.R
import com.example.soap.Utilities.Mocks.mock
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun LectureReviews(lecture: Lecture){
    Column {
        Row {
            Text(
                text = stringResource(R.string.reviews),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.padding(4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LectureSummaryRow(title = stringResource(R.string.grade), description = lecture.gradeLetter)

            Spacer(Modifier.weight(1f))

            LectureSummaryRow(title = stringResource(R.string.load), description = lecture.loadLetter)

            Spacer(Modifier.weight(1f))

            LectureSummaryRow(title = stringResource(R.string.speech), description = lecture.speechLetter)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {},
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(MaterialTheme.soapColors.background)
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_rate_review),
                    contentDescription = null,
                    tint = MaterialTheme.soapColors.onSurface
                )

                Spacer(Modifier.padding(4.dp))

                Text(
                    text = stringResource(R.string.write_a_review),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.soapColors.onSurface
                )
            }

        }
        Spacer(Modifier.padding(4.dp))

        Column {
            ReviewCard(lecture = lecture)
            ReviewCard(lecture = lecture)
        }
    }
}


@Composable
fun ReviewCard(lecture: Lecture) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        Modifier
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.soapColors.surface, shape = RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = lecture.professors.firstOrNull()?.name?.localized()
                        ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    text = "${lecture.year.toString().takeLast(2)}${lecture.semester.shortCode}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.soapColors.grayBB
                )

                Spacer(modifier = Modifier.weight(1f))

                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        modifier = Modifier.clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.soapColors.gray0Border)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.translate)) },
                            onClick = { /* TODO */ },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_translate),
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.summarise)) },
                            onClick = { /* TODO */ },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_summarize),
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.report)) },
                            onClick = { /* TODO */ },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(4.dp))

            Text(
                text = "재수강할 각오로 기말 던지고 나왔는데 교수님이 B0를 주신 ㅎㅎ...\n수업 잘하시는데, 개인적으로 못 따라가서 좀 아쉽네요\n밑 글처럼 전산쪽 베이스 부족하면 좀 힘들 것 같습니다\n왜 전산을 하고 싶으면 시프를 들으라는지 알 수 있었네요",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                ReviewMetric(title = stringResource(R.string.grade), value = "A+")
                Spacer(modifier = Modifier.padding(8.dp))
                ReviewMetric(title = stringResource(R.string.load), value = "A+")
                Spacer(modifier = Modifier.padding(8.dp))
                ReviewMetric(title = stringResource(R.string.speech), value = "A")

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("20")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.icon_arrowup),
                        contentDescription = "Upvote"
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewMetric(title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.soapColors.grayBB
        )
        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = value,
            color = MaterialTheme.soapColors.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
@Preview
private fun Preview(){
    SoapTheme { LectureReviews(lecture = Lecture.mock()) }
}