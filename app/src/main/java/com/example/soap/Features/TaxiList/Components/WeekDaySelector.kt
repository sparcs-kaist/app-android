package com.example.soap.Features.TaxiList.Components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current
    val itemBounds = remember { mutableStateListOf<Pair<Int, Int>>() }

    val selectedIndex = week.indexOf(selectedDate).coerceAtLeast(0)
    val selectedBounds = itemBounds.getOrNull(selectedIndex)

    val animatedOffsetX by animateDpAsState(
        targetValue = with(density) { selectedBounds?.first?.toDp() ?: 0.dp },
        label = "indicator offset"
    )

    val animatedWidth by animateDpAsState(
        targetValue = with(density) { selectedBounds?.second?.toDp() ?: 0.dp },
        label = "indicator width"
    )

    Box(
        modifier = Modifier
            .shadow(
                4.dp,
                RoundedCornerShape(28.dp),
                spotColor = MaterialTheme.soapColors.gray64Button
            )
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.soapColors.surface)
                .height(60.dp)
        ) {
            if (selectedBounds != null) {
                Box(
                    modifier = Modifier
                        .offset(x = animatedOffsetX)
                        .width(animatedWidth)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.soapColors.onSurface)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEachIndexed { index, day ->
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
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .onGloballyPositioned { coords ->
                                val x = coords.positionInParent().x.toInt()
                                val width = coords.size.width
                                if (itemBounds.size <= index) {
                                    itemBounds.add(x to width)
                                } else {
                                    itemBounds[index] = x to width
                                }
                            }
                            .clickable { onSelect(day) },
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
}

@Preview
@Composable
private fun WeekDaySelectorPreview() {
    SoapTheme {
        val today = LocalDate.now()
        val week = (0..6).map { today.plusDays(it.toLong()) }
        var selectedDate by remember { mutableStateOf(today) }

        WeekDaySelector(
            selectedDate = selectedDate,
            week = week,
            onSelect = { selectedDate = it }
        )
    }
}
