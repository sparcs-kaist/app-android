package com.example.soap.Features.LectureDetail.Components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureDetailNavigationBar(
    navController: NavController,
    text: String,
    onAdd: (() -> Unit)?
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.darkGray
                )
            }
        },
        title = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        actions = {
            if(onAdd != null) {
                IconButton(
                    onClick = {
                        onAdd()
                        navController.popBackStack()
                    },
                    colors = IconButtonDefaults.iconButtonColors(Color.Transparent),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_add),
                        contentDescription = "Plus",
                        tint = MaterialTheme.colorScheme.darkGray
                    )
                }
            } else {
                IconButton(
                    onClick = {},
                    colors = IconButtonDefaults.iconButtonColors(Color.Transparent),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_delete),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.darkGray
                    )
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
@Preview
private fun Preview(){
    Theme{ LectureDetailNavigationBar(rememberNavController(), "title", null) }
}