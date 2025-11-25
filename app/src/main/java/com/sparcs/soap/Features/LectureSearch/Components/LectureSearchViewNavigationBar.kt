package com.sparcs.soap.Features.LectureSearch.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.sparcs.soap.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureSearchViewNavigationBar(
    title: String
) {
    CenterAlignedTopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
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
    Theme {
       LectureSearchViewNavigationBar("title") }
}

