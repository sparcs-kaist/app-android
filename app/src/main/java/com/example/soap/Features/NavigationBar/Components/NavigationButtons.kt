package com.example.soap.Features.NavigationBar.Components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun NotificationButton(){
    IconButton(
        onClick = {},
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.notification),
            contentDescription = "Menu",
            tint = MaterialTheme.soapColors.darkGray,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun SettingButton() {
    IconButton(
        onClick = {},
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = "Menu",
            tint = MaterialTheme.soapColors.darkGray,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun AddButton(
    contentDescription: String,
    onClick: ()-> Unit
) {
    IconButton(
        onClick = { onClick() },
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.round_add),
            contentDescription = contentDescription,
            tint = MaterialTheme.soapColors.darkGray,
            modifier = Modifier.size(30.dp)
        )

    }
}

@Composable
fun ChatButton(){
    IconButton(
        onClick = {},
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.chat_bubble_outline),
            contentDescription = "chat",
            tint = MaterialTheme.soapColors.darkGray,
            modifier = Modifier.size(30.dp)
        )

    }
}

@Composable
fun SearchButton() {
    IconButton(
        onClick = {},
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.search),
            contentDescription = "Search",
            tint = MaterialTheme.soapColors.darkGray,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun DismissButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_back_ios),
            contentDescription = "Cancel",
            tint = MaterialTheme.soapColors.darkGray
        )
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme {
        SearchButton()
    }
}