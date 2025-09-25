package com.example.soap.Features.Settings.OTL

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.Shared.ViewModelMocks.MockOTLSettingsViewModel
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB


@Composable
fun OTLSettingsView(
    viewModel: OTLSettingsViewModelProtocol,
    navController: NavController
) {

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = "OTL Settings",
                onDismiss = { navController.navigate(Channel.Settings.name )}
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
        ) {
            MajorPicker(
                selected = viewModel.otlMajor,
                options = viewModel.otlMajorList,
                onSelected = { viewModel.otlMajor = it }
            )
        }
    }
}

@Composable
fun MajorPicker(
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "Major",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = selected ?: "Select Major",
            modifier = Modifier.clickable { expanded = true },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.grayBB
        )

        Spacer(Modifier.width(8.dp))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    val vm = remember { MockOTLSettingsViewModel() }
    Theme {
        OTLSettingsView(vm, rememberNavController())
    }
}