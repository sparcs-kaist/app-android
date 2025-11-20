package com.sparcs.soap.Features.NavigationBar.Components

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
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.darkGray
import com.sparcs.soap.ui.theme.lightGray0

@Composable
fun NotificationButton(){
    IconButton(
        onClick = {},
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.notification),
            contentDescription = "Menu",
            tint = MaterialTheme.colorScheme.darkGray,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun SettingButton(
    onClick: () -> Unit ={}
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = "Menu",
            tint = MaterialTheme.colorScheme.darkGray,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun AddButton(
    contentDescription: String,
    onClick: ()-> Unit,
    isEnabled: Boolean = true
) {
    IconButton(
        onClick = { if(isEnabled) onClick() },
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.round_add),
            contentDescription = contentDescription,
            tint = if(isEnabled) MaterialTheme.colorScheme.darkGray else MaterialTheme.colorScheme.lightGray0,
            modifier = Modifier.size(30.dp)
        )

    }
}

@Composable
fun ChatButton(onClick: () -> Unit){
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.chat_bubble_outline),
            contentDescription = "chat",
            tint = MaterialTheme.colorScheme.darkGray,
            modifier = Modifier.size(30.dp)
        )

    }
}

@Composable
fun SearchButton(
    onClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    IconButton(
        onClick = { onClick() },
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.search),
            contentDescription = "Search",
            tint = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.darkGray,
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
            tint = MaterialTheme.colorScheme.darkGray
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        SearchButton({}, false)
    }
}