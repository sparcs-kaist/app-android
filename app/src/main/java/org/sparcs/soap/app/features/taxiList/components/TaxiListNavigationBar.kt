package org.sparcs.soap.app.features.taxiList.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.navigationBar.components.ChatButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiListNavigationBar(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    // New rooms are created from the floating action button on the list.
    TopAppBar(
        // Bold, as on every tab's root screen.
        title = { Text(text = stringResource(Channel.Taxi.title), fontWeight = FontWeight.Bold) },
        actions = {
            ChatButton(onClick = { navController.navigate(Channel.TaxiChatListView.name) })
        },
        scrollBehavior = scrollBehavior
    )
}