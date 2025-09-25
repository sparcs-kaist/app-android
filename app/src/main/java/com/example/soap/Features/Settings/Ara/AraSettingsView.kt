package com.example.soap.Features.Settings.Ara

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Settings.Components.RowElementView
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.Features.Settings.SettingsViewModel
import com.example.soap.Features.Settings.SettingsViewModelProtocol
import com.example.soap.Shared.ViewModelMocks.MockSettingsViewModel
import com.example.soap.ui.theme.Theme

@Composable
fun AraSettingsView(
    viewModel: SettingsViewModelProtocol,
    navController: NavController
) {
    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = "Ara Settings",
                onDismiss = { navController.navigate(Channel.Settings.name )}
            )
        }
    ){ innerPadding ->

        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ){
            item {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                RowElementView(
                    title = "Nickname",
                    content = "오열하는 운영체제 및 실험_2f94d"
                )
            }

            item {
                Text(
                    text = "ContentPreferences",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Allow NSFW")
                    Switch(
                        checked = viewModel.araAllowNSFWPosts.value,
                        onCheckedChange = { viewModel.araAllowNSFWPosts.value = it }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Allow Political")
                    Switch(
                        checked = viewModel.araAllowPoliticalPosts.value,
                        onCheckedChange = { viewModel.araAllowPoliticalPosts.value = it }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                RowElementView(
                    title = "Blocked Users",
                    content = "${viewModel.araBlockedUsers.value.size}",
                    modifier = Modifier.clickable {
                    navController.navigate(Channel.AraBlockedUsersSettings.name)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview(){
    Theme {
        AraSettingsView(MockSettingsViewModel(SettingsViewModel.ViewState.Loaded), rememberNavController())
    }
}