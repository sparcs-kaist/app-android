package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.AddButton

@Composable
fun TimetableAddButton(enabled: Boolean, onAddClass: () -> Unit, onAddActivity: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AddButton(contentDescription = stringResource(R.string.activity_add), onClick = { expanded = true }, isEnabled = enabled)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.activity_add_class)) }, leadingIcon = { Icon(Icons.Default.School, null) }, onClick = { expanded = false; onAddClass() })
            DropdownMenuItem(text = { Text(stringResource(R.string.activity_new)) }, leadingIcon = { Icon(Icons.Default.Event, null) }, onClick = { expanded = false; onAddActivity() })
        }
    }
}
