package com.example.soap.Features.Search.Components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Shared.Extensions.elevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchNavigationBar(
    scrollState: ScrollState
) {
    TopAppBar(
        title = {
            Row {
                Text(
                    text = stringResource(Channel.SearchView.title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.shadow(scrollState.elevation())
    )
}