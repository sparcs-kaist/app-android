package org.sparcs.App.Features.UserPostList.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.App.Features.NavigationBar.Components.SearchButton
import org.sparcs.R
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.darkGray

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
            Row(
                modifier = Modifier.clickable { navController.popBackStack() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.darkGray
                )
            }
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