package com.example.soap.Features.Timetable.Components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Usecases.MockTimetableUseCase
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.lightGray0
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
            MyTableDropDownItems(viewModel)

            BottomMenuDropDownItems(viewModel)
        }
    }
}

@Composable
fun MyTableDropDownItems(
    viewModel: TimetableViewModel
) {
    val scope = CoroutineScope(Dispatchers.Main)
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
                viewModel.timetableIDsForSelectedSemester.forEachIndexed { index, id ->
                    val displayName = if (id.contains("myTable")) "My Table" else "Table $index"
                    val isSelected = id == viewModel.selectedTimetable.value?.id
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = { viewModel.selectTimetable(id) },
                        leadingIcon = {
                            if (isSelected) Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text("New Table") },
                    onClick = {
                        scope.launch {
                            try {
                                viewModel.createTable()
                            } catch (e: Exception) {
                                // TODO: handle error
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.round_add),
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomMenuDropDownItems(viewModel: TimetableViewModel){
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
        color = MaterialTheme.colorScheme.lightGray0,
        modifier = Modifier.padding(4.dp)
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_delete), color = Color(0xFFE54C65)) },
        onClick = { if(viewModel.isEditable) viewModel.deleteTable() },
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