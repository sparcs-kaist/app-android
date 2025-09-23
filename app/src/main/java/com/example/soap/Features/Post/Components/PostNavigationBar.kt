package com.example.soap.Features.Post.Components

import PostCommentActionsMenu
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostNavigationBar(
    boardGroup: String,
    navController : NavController,
    onDelete: () -> Unit,
    onReport: (AraContentReportType) -> Unit,
    onTranslate: () -> Unit,
    isMine: Boolean?
) {
    TopAppBar(
        navigationIcon = {
            Row(
                modifier = Modifier.clickable {
                    navController.popBackStack()
                                              },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.darkGray
                )
                Text(
                    text = boardGroup,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.darkGray
                )
            }
        },
        title = {},

        actions = {
            PostCommentActionsMenu(
                isMine = isMine,
                onDelete = onDelete,
                onReport = onReport,
                onTranslate = onTranslate,
                isComment = false,
                modifier = Modifier.size(28.dp)
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )

}

@Composable
@Preview
private fun Preview(){
    Theme{ PostNavigationBar("Board", rememberNavController(), {}, {}, {}, false) }
}