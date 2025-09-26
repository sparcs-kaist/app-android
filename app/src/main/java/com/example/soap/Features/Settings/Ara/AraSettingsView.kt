package com.example.soap.Features.Settings.Ara

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.Features.Settings.Taxi.NavigationLinkWithIcon
import com.example.soap.R
import com.example.soap.Shared.ViewModelMocks.MockAraSettingsViewModel
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AraSettingsView(
    viewModel: AraSettingsViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showNicknameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUser()
    }

    LaunchedEffect(viewModel.allowNSFW, viewModel.allowPolitical) {
        viewModel.updateContentPreference()
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = "Ara Settings",
                onDismiss = { navController.navigate(Channel.Settings.name) }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (state) {
                is AraSettingsViewModel.ViewState.Loading -> LoadingView()
                is AraSettingsViewModel.ViewState.Loaded -> LoadedView(
                    viewModel,
                    navController
                ) { showNicknameDialog = true }

                is AraSettingsViewModel.ViewState.Error -> {
                    val message = (state as AraSettingsViewModel.ViewState.Error).message
                    Text("Error: $message", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (showNicknameDialog) {
            AlertDialog(
                onDismissRequest = { showNicknameDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showNicknameDialog = false
                        scope.launch { viewModel.updateNickname() }
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showNicknameDialog = false }) { Text("Cancel") }
                },
                title = { Text("Warning") },
                text = { Text("Nicknames can only be changed every 3 months. Change nickname to ${viewModel.nickname}?") }
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = "Unknown", onValueChange = {}, enabled = false)
        Spacer(Modifier.height(16.dp))
        Text("Posts", style = MaterialTheme.typography.titleMedium)
        Switch(checked = true, onCheckedChange = {})
        Switch(checked = true, onCheckedChange = {})
    }
}

@Composable
private fun LoadedView(
    viewModel: AraSettingsViewModelProtocol,
    navController: NavController,
    onNicknameClick: () -> Unit
) {
    val nicknameUpdatable = viewModel.nicknameUpdatable
    val nicknameUpdatableFrom = viewModel.nicknameUpdatableFrom
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column {
        //profile
        Text("Profile", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = viewModel.nickname,
            onValueChange = { viewModel.nickname = it },
            enabled = nicknameUpdatable,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { onNicknameClick() },
            modifier = Modifier.fillMaxWidth()
        )

        if (!nicknameUpdatable && nicknameUpdatableFrom != null) {
            Text(
                text = "You can't change nickname until ${formatter.format(nicknameUpdatableFrom)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.grayBB
            )
        }

        Text(
            text = "Nicknames can only be changed every 3 months.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.grayBB
        )

        Spacer(Modifier.height(16.dp))

        //content preferences
        Text("Content Preferences", style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Allow NSFW")
            Spacer(Modifier.weight(1f))
            Switch(checked = viewModel.allowNSFW, onCheckedChange = { viewModel.allowNSFW = it })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Allow Political")
            Spacer(Modifier.weight(1f))
            Switch(checked = viewModel.allowPolitical, onCheckedChange = { viewModel.allowPolitical = it })
        }

        Spacer(Modifier.height(16.dp))

        //posts
        Text("Posts", style = MaterialTheme.typography.titleMedium)

        NavigationLinkWithIcon(
            onClick = {
                val json = Uri.encode(Gson().toJson(AraMyPostViewModel.PostType.ALL))
                navController.navigate(Channel.AraMyPostSettings.name + "?type_json=$json")
            },
            text = "My Posts",
            icon = painterResource(R.drawable.round_format_list_bulleted)
        )

        NavigationLinkWithIcon(
            onClick = {
                val json = Uri.encode(Gson().toJson(AraMyPostViewModel.PostType.BOOKMARK))
                navController.navigate(Channel.AraMyPostSettings.name + "?type_json=$json")
            },
            text = "Bookmarked Posts",
            icon = painterResource(R.drawable.bookmark_border)
        )
    }
}


@Preview
@Composable
private fun Preview(){
    Theme {
        AraSettingsView(MockAraSettingsViewModel(AraSettingsViewModel.ViewState.Loaded), rememberNavController())
    }
}