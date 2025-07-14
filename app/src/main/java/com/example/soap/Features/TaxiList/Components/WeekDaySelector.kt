package com.example.soap.Features.TaxiList.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun WeekDaySelector(
    selectedDate: LocalDate,
    week: List<LocalDate>,
    onSelect: (LocalDate) -> Unit
) {
    Box(Modifier.shadow(4.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.soapColors.gray64Button)){
        Row(
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.soapColors.surface)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            week.forEach { day ->
                val isSelected = day == selectedDate
                val dayOfWeek = day.dayOfWeek

                val textColor = when (dayOfWeek) {
                    DayOfWeek.SUNDAY -> Color.Red
                    DayOfWeek.SATURDAY -> Color.Blue
                    else -> MaterialTheme.soapColors.onSurface
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clickable {
                            onSelect(day)
                        }
                        .background(
                            color = if (isSelected) MaterialTheme.soapColors.primary else Color.Transparent,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSelected) MaterialTheme.soapColors.surface else MaterialTheme.soapColors.onSurface
                    )

                    Text(
                        text = day.dayOfWeek.name.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isSelected) MaterialTheme.soapColors.surface else textColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview(){
    SoapTheme {
        WeekDaySelector(
            selectedDate = LocalDate.now(),
            week = (0..6).map { LocalDate.now().plusDays(it.toLong()) },
            onSelect = {}
        )
    }
}