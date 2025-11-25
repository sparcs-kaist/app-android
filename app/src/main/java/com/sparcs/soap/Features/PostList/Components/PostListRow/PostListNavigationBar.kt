package com.sparcs.soap.Features.PostList.Components.PostListRow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import com.sparcs.soap.Features.NavigationBar.Components.SearchButton
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.darkGray
import com.sparcs.soap.ui.theme.grayBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardNavigationBar(
    title: String,
    subTitle: String,
    onClickSearch: () -> Unit,
    isSelected: Boolean,
    navController: NavController,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Icon(
                painter = painterResource(R.drawable.arrow_back_ios),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.darkGray,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
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
        )
    )
}


@Composable
@Preview
private fun Preview() {
    Theme { BoardNavigationBar("Title", "notices", {}, false, rememberNavController()) }
}