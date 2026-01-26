package org.sparcs.soap.App.Features.Course.Components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.theme.ui.Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseNavigationBar(
    navController: NavController,
    text: String,
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
                    if (hasMeasured) return@Text
                    hasMeasured = true
                    val newLineCount = textLayoutResult.lineCount
                    if (lineCount != newLineCount) {
                        lineCount = newLineCount
                    }
                }
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { CourseNavigationBar(rememberNavController(), "title") }
}