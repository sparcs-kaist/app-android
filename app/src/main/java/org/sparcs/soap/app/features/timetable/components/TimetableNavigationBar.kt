package org.sparcs.soap.app.features.timetable.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.theme.ui.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableViewNavigationBar(
    onClick: () -> Unit,
    isButtonEnabled: Boolean,
    onActivityClick: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = stringResource(Channel.TimeTable.title)) },
        actions = {
            TimetableAddButton(
                enabled = isButtonEnabled,
                onAddClass = onClick,
                onAddActivity = onActivityClick
            )
        }
    )
}

@Preview
@Composable
private fun TimetableNavigationBarPreview() {
    Theme { TimetableViewNavigationBar({}, true) }
}
