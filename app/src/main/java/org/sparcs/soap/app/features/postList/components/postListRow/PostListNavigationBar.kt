package org.sparcs.soap.app.features.postList.components.postListRow

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.features.navigationBar.components.SearchButton
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoardNavigationBar(
    title: String,
    subTitle: String,
    onClickSearch: () -> Unit,
    isSelected: Boolean,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        navigationIcon = {
            BackButton { navController.popBackStack() }
        },
        // The board's name with its group below, as the Expressive app bar's own subtitle.
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        subtitle = { Text(text = subTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        actions = {
            SearchButton(
                onClick = { onClickSearch() },
                isSelected = isSelected
            )
        },
        scrollBehavior = scrollBehavior
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun Preview() {
    Theme { BoardNavigationBar("Title", "notices", {}, false, rememberNavController()) }
}