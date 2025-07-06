package com.example.soap.Features.NavigationBar.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostNavigationBar(navController : NavController){

    TopAppBar(
        title = {
            Column(Modifier.background(MaterialTheme.soapColors.background)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, end = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { navController.navigate(Channel.TrendingBoard.name) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_ios),
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(R.string.trending_board),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Box(Modifier.align(Alignment.CenterEnd)) {
                        Row {
                            NotificationTopButton()

                            MenuTopButton()
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.soapColors.background
        )
    )

}

@Composable
private fun NotificationTopButton() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { /* TODO: Menu */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.notification),
            contentDescription = "Search",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.soapColors.darkGray
        )

    }
}

@Composable
private fun MenuTopButton() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { /* TODO: Menu */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = "Search",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.soapColors.darkGray
        )

    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme{ PostNavigationBar(rememberNavController()) }
}