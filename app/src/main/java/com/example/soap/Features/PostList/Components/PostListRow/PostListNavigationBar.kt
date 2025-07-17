package com.example.soap.Features.PostList.Components.PostListRow

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.NavigationBar.Components.SearchButton
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardNavigationBar(navController: NavController) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Row(
                modifier = Modifier.clickable { navController.navigate(Channel.Start.name) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = null,
                    tint = MaterialTheme.soapColors.darkGray
                )
                Text(
                    text = stringResource(R.string.start),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.soapColors.darkGray
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.trending_board),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            SearchButton()
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.soapColors.background
        )
    )
}



@Composable
@Preview
private fun Preview(){
    SoapTheme{ BoardNavigationBar(rememberNavController()) }
}