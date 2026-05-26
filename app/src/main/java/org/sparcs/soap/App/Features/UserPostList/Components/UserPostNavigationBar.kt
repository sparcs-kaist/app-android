package org.sparcs.soap.App.Features.UserPostList.Components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.Features.NavigationBar.Components.SearchButton
import org.sparcs.soap.App.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPostNavigationBar(
    title: String,
    onClickSearch: () -> Unit,
    isSelected: Boolean,
    navController: NavController,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            DismissButton { navController.popBackStack() }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            SearchButton(
                onClick = { onClickSearch() },
                isSelected = isSelected,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}


@Composable
@Preview
private fun Preview() {
    Theme { UserPostNavigationBar("Title", {}, false, rememberNavController()) }
}