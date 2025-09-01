package com.example.soap.Features.Settings.Ara

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AraBlockedUsersView(initialBlockedUsers: List<String>){
    var blockedUsers by remember { mutableStateOf(initialBlockedUsers.toMutableList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked Users") },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(
                items = blockedUsers,
                key = { it }
            ) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = user)
                    IconButton(onClick = {
                        blockedUsers = blockedUsers.toMutableList().also { it.remove(user) }
                        // TODO: implement API call
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Unblock"
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Preview
@Composable
private fun Preview(){
    Theme {
        AraBlockedUsersView(listOf("Nickname 1", "Nickname 2"))
    }
}