package org.sparcs.soap.app.features.timetable.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.theme.ui.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableViewNavigationBar(
    onClick: () -> Unit,
    isButtonEnabled: Boolean,
    onActivityClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        // Bold, as on every tab's root screen.
        title = { Text(text = stringResource(Channel.TimeTable.title), fontWeight = FontWeight.Bold) },
        actions = {
            TimetableAddButton(
                enabled = isButtonEnabled,
                onAddClass = onClick,
                onAddActivity = onActivityClick
            )
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun TimetableNavigationBarPreview() {
    Theme { TimetableViewNavigationBar({}, true) }
}
