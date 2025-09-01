package com.example.soap.Features.Settings.OTL

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Features.Settings.SettingsViewModel
import com.example.soap.Features.Settings.SettingsViewModelProtocol
import com.example.soap.Shared.ViewModelMocks.MockSettingsViewModel
import com.example.soap.ui.theme.Theme


@Composable
fun OTLSettingsView(viewModel: SettingsViewModelProtocol) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {

        Text("Major", style = MaterialTheme.typography.titleMedium)

        MajorPicker(
            selected = viewModel.otlMajor.value,
            options = viewModel.otlMajorList,
            onSelected = { viewModel.otlMajor.value = it }
        )
    }
}

@Composable
fun MajorPicker(
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected ?: "Select Major",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            label = { Text("Major") }
        )

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
    Theme {
        OTLSettingsView(MockSettingsViewModel(SettingsViewModel.ViewState.Loaded))
    }
}