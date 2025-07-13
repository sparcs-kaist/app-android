package com.example.soap.Features.Timetable.Components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun TimetableDropDownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(MaterialTheme.soapColors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            TopDropDownItems()

            MiddleDropDownItems()

            BottomDropDownItems()

        }
    }
}

@Composable
private fun TopDropDownItems(){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconWithText(
            icon = painterResource(R.drawable.outline_timetable),
            text = stringResource(R.string.timetable)
        )

        IconWithText(
            icon = painterResource(R.drawable.rounded_location_on),
            text = stringResource(R.string.timetable)
        )

        IconWithText(
            icon = painterResource(R.drawable.outline_edit),
            text = stringResource(R.string.timetable)
        )
    }

    HorizontalDivider(
        color = MaterialTheme.soapColors.gray0Border,
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
private fun MiddleDropDownItems(){
    var isInternalMenuOpen by remember { mutableStateOf(false) }

    Column {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.my_table)) },
            onClick = { isInternalMenuOpen = !isInternalMenuOpen },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward_ios),
                    contentDescription = "Expand",
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(if (isInternalMenuOpen) 270f else 0f)
                )
            }
        )
        AnimatedVisibility(
            visible = isInternalMenuOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Text("시간표 항목 1", modifier = Modifier.padding(8.dp))
                Text("시간표 항목 2", modifier = Modifier.padding(8.dp))
                Text("시간표 항목 3", modifier = Modifier.padding(8.dp))
            }
        }

    }

    HorizontalDivider(
        color = MaterialTheme.soapColors.gray0Border,
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
private fun IconWithText(
    icon: Painter,
    text: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = text
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }

}

@Composable
private fun BottomDropDownItems(){
    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_add)) },
        onClick = { /* TODO */ },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.round_add),
                contentDescription = null
            )
        }
    )

    HorizontalDivider(
        color = MaterialTheme.soapColors.gray0Border,
        modifier = Modifier.padding(4.dp)
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_delete), color = Color(0xFFE54C65)) },
        onClick = { /* TODO */ },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.outline_delete),
                contentDescription = null,
                tint = Color(0xFFE54C65)
            )
        }
    )
}
@Composable
@Preview
private fun Preview(){
    SoapTheme {
        Box(Modifier.fillMaxSize()){
            Button(
                onClick = {}
            ) {
                TimetableDropDownMenu(expanded = true, onDismiss = {})
            }
        }
    }
}