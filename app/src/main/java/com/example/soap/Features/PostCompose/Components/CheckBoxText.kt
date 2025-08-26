package com.example.soap.Features.PostCompose.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray


@Composable
fun CheckBoxText(
    text: String,
    isChecked: Boolean,
    onCheckedChange:((Boolean) -> Unit)?
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 4.dp) {

            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(MaterialTheme.colorScheme.darkGray),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        CheckBoxText(
            text = "Text",
            isChecked = false,
            onCheckedChange = {}
    )
    }
}
