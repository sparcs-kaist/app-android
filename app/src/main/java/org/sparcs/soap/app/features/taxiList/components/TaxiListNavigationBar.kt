package org.sparcs.soap.app.features.taxiList.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.navigationBar.components.AddButton
import org.sparcs.soap.app.features.navigationBar.components.ChatButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiListNavigationBar(
    isButtonEnabled: Boolean,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = { Text(text = stringResource(Channel.Taxi.title)) },
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
        scrollBehavior = scrollBehavior
    )
}