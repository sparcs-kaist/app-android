package com.example.soap.Features.TaxiRoomCreation.Components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.Features.NavigationBar.Components.DismissButton
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiRoomCreationNavigationBar(
    onDismiss: () -> Unit,
    isEnabled: Boolean
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = onDismiss) },

        title = {
            Text(
                text = "New Room",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            },
        actions = {
            SendButton(
                isEnabled = isEnabled,
                onClick = onDismiss,
                onError = {
                    errorMessage = it
                    showErrorDialog = true
                }
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun SendButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    TextButton(
        onClick = {
            coroutineScope.launch {
                try {
                    // viewModel.createRoom(title)
                    // viewModel.fetchData()
                    onClick()
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Unknown error")
                }
            }
        },
        enabled = isEnabled,
        modifier = Modifier.semantics { contentDescription = "Create Room Button" }
    ) {
        Text(
            text = stringResource(R.string.submit),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.grayBB
        )
    }
}


@Composable
@Preview
private fun Preview(){
    Theme { TaxiRoomCreationNavigationBar({}, false) }
}

