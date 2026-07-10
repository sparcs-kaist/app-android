package org.sparcs.soap.app.features.postList.components.postListRow

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.features.navigationBar.components.DismissButton
import org.sparcs.soap.app.features.navigationBar.components.SearchButton
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardNavigationBar(
    title: String,
    subTitle: String,
    onClickSearch: () -> Unit,
    isSelected: Boolean,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            DismissButton { navController.popBackStack() }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.grayBB
                )
            }
        },
        actions = {
            SearchButton(
                onClick = { onClickSearch() },
                isSelected = isSelected
                )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        scrollBehavior = scrollBehavior
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun Preview() {
    Theme { BoardNavigationBar("Title", "notices", {}, false, rememberNavController()) }
}