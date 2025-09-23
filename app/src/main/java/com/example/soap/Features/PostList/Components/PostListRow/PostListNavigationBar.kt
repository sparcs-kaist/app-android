package com.example.soap.Features.PostList.Components.PostListRow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Components.SearchButton
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray
import com.example.soap.ui.theme.grayBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardNavigationBar(
    title: String,
    subTitle: String,
    navController: NavController
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
                Text(
                    text = stringResource(R.string.start),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.darkGray
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally){
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
            SearchButton()
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}



@Composable
@Preview
private fun Preview(){
    Theme{ BoardNavigationBar("Title","notices", rememberNavController()) }
}