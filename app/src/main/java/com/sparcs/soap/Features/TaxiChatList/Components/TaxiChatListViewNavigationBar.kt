package com.sparcs.soap.Features.TaxiChatList.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.sparcs.soap.Features.NavigationBar.Components.DismissButton
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatListViewNavigationBar(
    onDismiss: () -> Unit
) {
    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = { onDismiss() }) },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.chats),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}



@Composable
@Preview
private fun Preview(){
    Theme {
        TaxiChatListViewNavigationBar({}) }
}

