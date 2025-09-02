package com.example.soap.Features.TaxiChat.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.Components.DismissButton
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatViewNavigationBar(
    room: TaxiRoom,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = { onDismiss() }) },
        title = {
            Column {
                Text(text = room.title, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(
                    text = "${room.source.title} → ${room.destination.title}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        actions = { CallTaxiButton(onClick) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun CallTaxiButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = "Call Taxi",
            tint = MaterialTheme.colorScheme.darkGray,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { TaxiChatViewNavigationBar(TaxiRoom.mock(), {}, {}) }
}

