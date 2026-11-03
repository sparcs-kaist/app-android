package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.AddButton
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape

@Composable
fun TimetableAddButton(enabled: Boolean, onAddClass: () -> Unit, onAddActivity: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AddButton(
            contentDescription = stringResource(R.string.activity_add),
            onClick = { expanded = true },
            isEnabled = enabled
        )
        ExpressiveMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            MenuGroup(index = 0, count = 1) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.activity_add_class)) },
                    leadingIcon = { Icon(Icons.Default.School, null) },
                    onClick = { expanded = false; onAddClass() },
                    shape = menuItemShape(0, 2),
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.activity_new)) },
                    leadingIcon = { Icon(Icons.Default.Event, null) },
                    onClick = { expanded = false; onAddActivity() },
                    shape = menuItemShape(1, 2),
                )
            }
        }
    }
}
