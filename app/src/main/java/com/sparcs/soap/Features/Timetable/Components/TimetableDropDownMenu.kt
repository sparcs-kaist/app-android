package com.sparcs.soap.Features.Timetable.Components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.Domain.Usecases.MockTimetableUseCase
import com.sparcs.soap.Features.Timetable.TimetableViewModel
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import com.sparcs.soap.ui.theme.lightGray0
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TimetableDropDownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    viewModel: TimetableViewModel
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            MyTableDropDownItems(viewModel, onDismiss)

            BottomMenuDropDownItems(viewModel, onDismiss)
        }
    }
}

@Composable
private fun TopDropDownItems(){ //지도, 시험 시간표(바꿀 수 있도록)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconWithText(
            icon = painterResource(R.drawable.outline_timetable),
            text = stringResource(R.string.timetable)
        )//기본

        IconWithText(
            icon = painterResource(R.drawable.outline_description),
            text = stringResource(R.string.timetable)
        )//시험

        IconWithText(
            icon = painterResource(R.drawable.rounded_location_on),
            text = stringResource(R.string.timetable)
        )//지도
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.lightGray0,
        modifier = Modifier.padding(4.dp)
    )
}//TODO - 나중에?

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
fun MyTableDropDownItems(
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit
) {
    val selectedTimetable by viewModel.timetableUseCase.selectedTimetable.collectAsState()
    Column {
        viewModel.timetableIDsForSelectedSemester.forEachIndexed { index, id ->
            val displayName = if (id.contains("myTable")) stringResource(R.string.my_table) else "Table $index"
            val isSelected = id == selectedTimetable?.id

            DropdownMenuItem(
                text = { Text(displayName) },
                onClick = { viewModel.selectTimetable(id); onDismiss() },
                leadingIcon = {
                    if (isSelected) Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected"
                    )
                }
            )
        }
    }
}

@Composable
private fun BottomMenuDropDownItems(
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit
){
    val scope = CoroutineScope(Dispatchers.Main)
    val deleteColor =  if(viewModel.isEditable.collectAsState().value) Color(0xFFE54C65) else MaterialTheme.colorScheme.grayBB
    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_add)) },
        onClick = {
            scope.launch {
                try {
                    viewModel.createTable()
                } catch (e: Exception) {
                    Log.e("TimetableViewModel", "Error creating table: ${e.message}")
                }
            }
            onDismiss()
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.round_add),
                contentDescription = null
            )
        }
    )

    HorizontalDivider(
        color = MaterialTheme.colorScheme.lightGray0,
        modifier = Modifier.padding(4.dp)
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_delete), color = deleteColor) },
        onClick = { onDismiss(); if(viewModel.isEditable.value) viewModel.deleteTable() },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.outline_delete),
                contentDescription = null,
                tint = deleteColor
            )
        }
    )
}
@Composable
@Preview
private fun Preview(){
    val vm by remember { mutableStateOf( TimetableViewModel(MockTimetableUseCase()))}
    Theme {
        Box(Modifier.fillMaxSize()){
            Button(
                onClick = {}
            ) {
                TimetableDropDownMenu(expanded = true, onDismiss = {}, vm)
            }
        }
    }
}