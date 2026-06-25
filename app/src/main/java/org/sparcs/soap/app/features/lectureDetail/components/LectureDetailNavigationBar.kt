package org.sparcs.soap.app.features.lectureDetail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.features.navigationBar.components.DismissButton
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.darkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailNavigationBar(
    navController: NavController,
    text: String,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    isCurrentTimetable: Boolean,
    isEnabled: Boolean
) {
    var lineCount by remember { mutableIntStateOf(1) }
    var hasMeasured by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        navigationIcon = {
            DismissButton { navController.popBackStack() }
        },
        title = {
            Text(
                text = text,
                style = if (lineCount > 1) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                onTextLayout = { textLayoutResult ->
                    if(hasMeasured) return@Text
                    hasMeasured = true
                    val newLineCount = textLayoutResult.lineCount
                    if (lineCount != newLineCount) {
                        lineCount = newLineCount
                    }
                }
            )
        },
        actions = {
            if(!isEnabled) return@CenterAlignedTopAppBar
            if(!isCurrentTimetable) {
                IconButton(
                    onClick = {
                        onAdd()
                        navController.popBackStack()
                    },
                    colors = IconButtonDefaults.iconButtonColors(Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Plus",
                        tint = MaterialTheme.colorScheme.darkGray
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        onDelete()
                        navController.popBackStack()
                              },
                    colors = IconButtonDefaults.iconButtonColors(Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.darkGray
                    )
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
@Preview
private fun Preview(){
    Theme{ LectureDetailNavigationBar(rememberNavController(), "title", {}, {},
        isCurrentTimetable = false,
        isEnabled = true
    ) }
}