package org.sparcs.soap.app.shared.views.contentViews

import android.text.format.DateFormat
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * A read-only outlined field with a trailing icon that opens a picker below it; tapping it calls
 * [onClick]. While its picker is open, it keeps the focused look.
 */
@Composable
fun PickerField(
    label: String,
    value: String,
    icon: ImageVector,
    isOpen: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    LaunchedEffect(interaction, enabled) {
        interaction.interactions.collect { if (enabled && it is PressInteraction.Release) onClick() }
    }
    val colors = OutlinedTextFieldDefaults.colors(focusedTrailingIconColor = MaterialTheme.colorScheme.primary)
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        label = { Text(label) },
        trailingIcon = { Icon(icon, contentDescription = null) },
        interactionSource = interaction,
        colors = if (isOpen) OutlinedTextFieldDefaults.colors(
            focusedTrailingIconColor = colors.focusedTrailingIconColor,
            unfocusedBorderColor = colors.focusedIndicatorColor,
            unfocusedLabelColor = colors.focusedLabelColor,
            unfocusedTrailingIconColor = colors.focusedTrailingIconColor,
        ) else colors,
        modifier = modifier
    )
}

/**
 * Hour and minute wheels for a time of day, with an AM/PM wheel when the system uses 12-hour
 * time. Minutes come in [step]s; [onChange] gets the minutes since midnight once every wheel
 * settles.
 */
@Composable
fun TimeWheel(
    minuteOfDay: Int,
    step: Int,
    onChange: (Int) -> Unit,
) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val locale = Locale.getDefault()
    // Some languages, Korean among them, put AM/PM before the time.
    val periodFirst = remember(locale) { DateFormat.getBestDateTimePattern(locale, "hma").startsWith("a") }
    val periods = remember(locale) { DateFormatSymbols.getInstance(locale).amPmStrings.toList() }

    val hourOfDay = minuteOfDay / 60
    // In 12-hour time the hour wheel runs 12, 1, …, 11, and the AM/PM wheel adds the 12.
    val hourState = rememberLazyListState(if (is24Hour) hourOfDay else hourOfDay % 12)
    val periodState = rememberLazyListState(hourOfDay / 12)
    val minuteState = rememberLazyListState(minuteOfDay % 60 / step)
    val currentOnChange by rememberUpdatedState(onChange)
    LaunchedEffect(hourState, periodState, minuteState, is24Hour) {
        snapshotFlow {
            val states = listOf(hourState, periodState, minuteState)
            if (states.any { it.isScrollInProgress }) null
            else {
                val hour = if (is24Hour) hourState.centredIndex()
                else hourState.centredIndex() + 12 * periodState.centredIndex()
                hour * 60 + minuteState.centredIndex() * step
            }
        }.filterNotNull().collect { currentOnChange(it) }
    }

    val hours = remember(is24Hour) {
        if (is24Hour) (0 until 24).map { "%02d".format(it) }
        else (0 until 12).map { (if (it == 0) 12 else it).toString() }
    }
    val minutes = remember(step) { (0 until 60 step step).map { "%02d".format(it) } }
    val periodWheel = @Composable {
        Wheel(periods, periodState, label = periods.joinToString("/"), style = MaterialTheme.typography.titleMedium)
    }

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // The band behind the selected row.
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .width(WHEEL_WIDTH * (if (is24Hour) 2 else 3) + 32.dp)
                .height(WHEEL_ROW_HEIGHT)
        ) {}
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!is24Hour && periodFirst) periodWheel()
            Wheel(hours, hourState, label = stringResource(R.string.hour))
            Text(
                ":",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )
            Wheel(minutes, minuteState, label = stringResource(R.string.minute))
            if (!is24Hour && !periodFirst) periodWheel()
        }
    }
}

private val WHEEL_ROW_HEIGHT = 48.dp
private val WHEEL_WIDTH = 72.dp
private const val WHEEL_VISIBLE_ROWS = 5

/** The index of the row in the middle of the wheel. */
private fun LazyListState.centredIndex(): Int {
    val info = layoutInfo
    val centre = (info.viewportStartOffset + info.viewportEndOffset) / 2
    return info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - centre) }?.index
        ?: firstVisibleItemIndex
}

/** A snapping column of [items]; rows fade away from the middle. */
@Composable
private fun Wheel(
    items: List<String>,
    state: LazyListState,
    label: String,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    val scope = rememberCoroutineScope()
    val selected by remember { derivedStateOf { state.centredIndex() } }
    LazyColumn(
        state = state,
        flingBehavior = rememberSnapFlingBehavior(state),
        contentPadding = PaddingValues(vertical = WHEEL_ROW_HEIGHT * (WHEEL_VISIBLE_ROWS / 2)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(WHEEL_WIDTH)
            .height(WHEEL_ROW_HEIGHT * WHEEL_VISIBLE_ROWS)
            .semantics { contentDescription = label }
    ) {
        itemsIndexed(items) { index, item ->
            val distance = abs(index - selected)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WHEEL_ROW_HEIGHT)
                    .selectable(
                        selected = index == selected,
                        onClick = { scope.launch { state.animateScrollToItem(index) } }
                    )
            ) {
                Text(
                    text = item,
                    style = style,
                    color = if (distance == 0) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(if (distance == 0) 1f else (0.7f - 0.25f * (distance - 1)).coerceAtLeast(0.2f))
                )
            }
        }
    }
}
