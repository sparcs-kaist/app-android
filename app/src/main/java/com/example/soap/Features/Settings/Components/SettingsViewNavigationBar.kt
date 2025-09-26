package com.example.soap.Features.Settings.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.soap.Features.NavigationBar.Components.DismissButton
import com.example.soap.Features.NavigationBar.Components.SearchButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsViewNavigationBar(
    title: String,
    onDismiss: () -> Unit,
    isSearchEnabled: Boolean?= false
) {
    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = { onDismiss() }) },
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = { if(isSearchEnabled == true) SearchButton() }
    )
}