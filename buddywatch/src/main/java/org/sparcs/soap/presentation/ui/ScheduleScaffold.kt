package org.sparcs.soap.presentation.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.presentation.theme.SoapTheme

@Composable
fun ScheduleScaffold(
    title: String,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    onShowOptions: (() -> Unit)? = null,
    content: ScalingLazyListScope.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway(state)) },
        positionIndicator = { PositionIndicator(scalingLazyListState = state) }
    ) {
        ScalingLazyColumn(
            state = state,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 32.dp),
            modifier = Modifier.fillMaxSize()
                .rotaryScrollable(RotaryScrollableDefaults.behavior(state), focusRequester)
                .focusRequester(focusRequester)
                .focusable()
        ) {
            item { ListHeader { Text(title) } }
            content()
            if (onShowOptions != null) item { ViewOptionsButton(onShowOptions) }
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
fun ViewOptionsButton(onClick: () -> Unit) {
    CompactChip(onClick = onClick, label = { Text(stringResource(R.string.view_options)) })
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScheduleScaffoldPreview() {
    SoapTheme {
        ScheduleScaffold(stringResource(R.string.view_list), onShowOptions = {}) {
            item { Text(stringResource(R.string.no_schedule)) }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ViewOptionsButtonPreview() {
    SoapTheme { ViewOptionsButton {} }
}
