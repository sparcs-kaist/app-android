package org.sparcs.soap.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.presentation.LectureViewOption
import org.sparcs.soap.presentation.theme.SoapTheme

@Composable
fun ViewOptionsView(selection: LectureViewOption, onSelect: (LectureViewOption) -> Unit) {
    ScheduleScaffold(stringResource(R.string.view_options)) {
        items(LectureViewOption.entries) { option ->
            ToggleChip(
                checked = option == selection,
                onCheckedChange = { onSelect(option) },
                label = { Text(stringResource(option.title)) },
                toggleControl = { RadioButton(selected = option == selection) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ViewOptionsViewPreview() {
    SoapTheme { ViewOptionsView(LectureViewOption.UP_NEXT) {} }
}
