package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.elevation
import org.sparcs.soap.app.theme.ui.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableViewNavigationBar(
    scrollState: ScrollState,
    onClick: () -> Unit,
    isButtonEnabled: Boolean,
    onActivityClick: () -> Unit = {},
    onCreditsClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
) {
    TopAppBar(
        title = {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = stringResource(Channel.TimeTable.title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = onCreditsClick) {
                Icon(Icons.Outlined.School, contentDescription = stringResource(R.string.credit_calculation))
            }
            TimetableFriendsButton(onClick = onFriendsClick)
            TimetableAddButton(
                enabled = isButtonEnabled,
                onAddClass = onClick,
                onAddActivity = onActivityClick
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.shadow(scrollState.elevation())
    )
}

@Composable
fun TimetableFriendsButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Rounded.Group, contentDescription = stringResource(R.string.friends_title))
    }
}

@Preview
@Composable
private fun TimetableNavigationBarPreview() {
    Theme { TimetableViewNavigationBar(rememberScrollState(), {}, true) }
}
