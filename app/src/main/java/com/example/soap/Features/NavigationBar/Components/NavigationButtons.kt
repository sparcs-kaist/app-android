package com.example.soap.Features.NavigationBar.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.soap.R

@Composable
fun NotificationButton(){
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { /* TODO: Menu */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.notification),
                contentDescription = "Menu",
                modifier = Modifier.size(28.dp),
                tint = Color.DarkGray
            )
        }
    }
}

@Composable
fun SettingButton() {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { /* TODO: Menu */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "Setting",
                modifier = Modifier.size(28.dp),
                tint = Color.DarkGray
            )
        }
    }
}

@Composable
fun NavigationButton(
    isSelected: Boolean,
    title : String,
    icon : Painter,
    onClick : () -> Unit
){
    val selectedColor = if (isSelected) Color(0xFF6157CD) else Color.Gray


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable (onClick = onClick )
    ){
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = selectedColor
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = selectedColor
        )
    }
}


@Composable
fun SearchButton(){

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {  }
    ){
        Icon(
            painter = painterResource(R.drawable.search),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp),
            tint = Color.Gray
        )
        Text(
            text = "Search",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
