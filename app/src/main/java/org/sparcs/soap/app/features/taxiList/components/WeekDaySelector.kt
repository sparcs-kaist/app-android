package org.sparcs.soap.app.features.taxiList.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.repositories.taxi.FakeTaxiRoomRepository
import org.sparcs.soap.app.domain.usecases.taxi.MockTaxiLocationUseCase
import org.sparcs.soap.app.features.taxiList.TaxiListViewModel
import org.sparcs.soap.app.shared.extensions.toDate
import org.sparcs.soap.app.shared.extensions.toLocalDate
import org.sparcs.soap.app.shared.extensions.toShortString
import org.sparcs.soap.app.theme.ui.Theme
import java.time.DayOfWeek
import java.util.Date


private val Saturday = Color(0xFF45A7DA)
private val Sunday = Color(0xFFDA4A45)

/**
 * "All", then each day of the week, with the chosen one in a solid pill that slides to it. Tap a
 * day, or swipe along the week to drag the pill to one.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeekDaySelector(
    selectedDate: Date?,
    week: List<Date>,
    onSelect: (Date?) -> Unit,
) {
    val localWeek = remember(week) { week.map { it.toLocalDate() } }
    // Index 0 is "All"; the days follow.
    val count = localWeek.size + 1
    val selectedIndex = selectedDate?.toLocalDate()?.let { localWeek.indexOf(it) + 1 }?.coerceAtLeast(0) ?: 0
    val currentOnSelect by rememberUpdatedState(onSelect)
    val select: (Int) -> Unit = { index ->
        currentOnSelect(if (index == 0) null else localWeek[index - 1].toDate())
    }
    val currentSelect by rememberUpdatedState(select)
    val selectedDay = localWeek.getOrNull(selectedIndex - 1)
    val pillColor = when (selectedDay?.dayOfWeek) {
        DayOfWeek.SATURDAY -> Saturday
        DayOfWeek.SUNDAY -> Sunday
        else -> MaterialTheme.colorScheme.onSurface
    }
    val animatedPillColor by animateColorAsState(pillColor, label = "pill colour")
    val pillShape = RoundedCornerShape(24.dp)

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val itemWidth = maxWidth / count
        val offset by animateDpAsState(
            itemWidth * selectedIndex,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
            label = "pill offset",
        )
        val itemWidthPx = with(LocalDensity.current) { itemWidth.toPx() }
        val currentIndex by rememberUpdatedState(selectedIndex)

        Box(
            Modifier
                .offset(x = offset)
                .width(itemWidth)
                .fillMaxHeight()
                .background(animatedPillColor, pillShape)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .selectableGroup()
                // Dragging along the week moves the selection to the day under the finger.
                .pointerInput(count, itemWidthPx) {
                    detectHorizontalDragGestures { change, _ ->
                        val index = (change.position.x / itemWidthPx).toInt().coerceIn(0, count - 1)
                        if (index != currentIndex) currentSelect(index)
                    }
                },
        ) {
            DayItem(
                isSelected = selectedIndex == 0,
                onClick = { select(0) },
                shape = pillShape,
            ) { contentColor ->
                Text(
                    text = stringResource(R.string.all),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = contentColor,
                )
            }

            localWeek.forEachIndexed { index, day ->
                val isSelected = selectedIndex == index + 1
                val weekdayColor = when (day.dayOfWeek) {
                    DayOfWeek.SUNDAY -> Sunday
                    DayOfWeek.SATURDAY -> Saturday
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                DayItem(
                    isSelected = isSelected,
                    onClick = { select(index + 1) },
                    shape = pillShape,
                    isWeekend = day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY,
                ) { contentColor ->
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = contentColor,
                    )
                    Text(
                        text = day.dayOfWeek.toShortString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) contentColor else weekdayColor,
                    )
                }
            }
        }
    }
}

/** One choice in the selector; [content] is given the colour to draw in, on the pill or off it. */
@Composable
private fun RowScope.DayItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    isWeekend: Boolean = false,
    content: @Composable ColumnScope.(Color) -> Unit,
) {
    // White on the weekend's red and blue; on a weekday's pill, the surface colour.
    val onPill = if (isWeekend) Color.White else MaterialTheme.colorScheme.surface
    val contentColor by animateColorAsState(
        if (isSelected) onPill else MaterialTheme.colorScheme.onSurface,
        label = "day colour",
    )
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(shape)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
    ) {
        content(contentColor)
    }
}

@Preview
@Composable
private fun WeekDaySelectorPreview() {
    Theme {
        val fakeRepository = remember { FakeTaxiRoomRepository() }
        val savedStateHandle = remember { SavedStateHandle() }
        val viewModel = remember {
            TaxiListViewModel(
                savedStateHandle = savedStateHandle,
                fakeRepository,
                MockTaxiLocationUseCase()
            )
        }

        WeekDaySelector(
            selectedDate = viewModel.selectedDate,
            week = viewModel.week,
            onSelect = { newDate ->
                viewModel.selectedDate = newDate
            }
        )
    }
}