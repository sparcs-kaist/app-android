package org.sparcs.App.Features.NavigationBar.Components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.darkGray
import org.sparcs.App.theme.ui.lightGray0

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
            imageVector = Icons.Rounded.Add,
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
            imageVector = Icons.Outlined.ChatBubbleOutline,
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
            imageVector = Icons.Rounded.Search,
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
            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.darkGray,
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