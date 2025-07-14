package com.example.soap.Features.TaxiRoomCreation.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun TaxiCapacityPicker(
    capacity: Int,
    onCapacityChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Capacity",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        Row(Modifier.clickable { expanded = true }){
            Text(
                text = capacity.let { "$it people" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.soapColors.grayBB
            )

            Spacer(Modifier.padding(2.dp))

            Icon(
                painter = painterResource(R.drawable.round_swap_vert),
                contentDescription = "Expand dropdown",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.soapColors.grayBB
            )
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.soapColors.surface)
    ) {
        (1..4).forEach { count ->
            DropdownMenuItem(
                text = { Text("$count people") },
                onClick = {
                    onCapacityChange(count)
                    expanded = false
                }
            )
        }
    }
}
@Composable
@Preview
private fun Preview(){
    SoapTheme { TaxiCapacityPicker(4, {}) }
}