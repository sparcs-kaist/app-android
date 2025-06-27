package com.example.soap.Features.NavigationBar.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme

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
                .background(MaterialTheme.colorScheme.surface)
                .clickable { /* TODO: Menu */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.notification),
                contentDescription = "Menu",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.outline
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
                .background(MaterialTheme.colorScheme.surface)
                .clickable { /* TODO: Menu */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "Setting",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.outline
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
    val selectedColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline


    Box(Modifier.clip(RoundedCornerShape(16.dp))) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp)
        ) {
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
}

@Composable
fun SearchTopButton() {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .shadow(8.dp, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { /* TODO: Menu */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = "Search",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}


@Composable
fun SearchBottomButton(){

    Box(Modifier.clip(RoundedCornerShape(16.dp))) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clickable { }
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Search",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme {
        NotificationButton()
        
        NavigationButton(
            false,
            "title",
            painterResource(R.drawable.taxi),
            {}
        )
    }
}