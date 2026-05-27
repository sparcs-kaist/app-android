package org.sparcs.soap.App.Features.TaxiList.Components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.NavigationBar.Components.AddButton
import org.sparcs.soap.App.Features.NavigationBar.Components.ChatButton
import org.sparcs.soap.App.Shared.Extensions.elevation


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiListNavigationBar(
    scrollState: ScrollState,
    isButtonEnabled: Boolean,
    navController: NavController
) {
    TopAppBar(
        title = {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = stringResource(Channel.Taxi.title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            if (isButtonEnabled) {
                AddButton(
                    contentDescription = "Create Taxi Room",
                    onClick = {
                        navController.navigate(Channel.TaxiRoomCreation.name) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            ChatButton(onClick = { navController.navigate(Channel.TaxiChatListView.name) })
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.shadow(scrollState.elevation())
    )
}