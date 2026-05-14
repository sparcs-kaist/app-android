package org.sparcs.soap.App.Features.FeedPost.Components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.darkGray
import org.sparcs.soap.Features.Post.PostCommentActionsMenu
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostNavigationBar(
    navController: NavController,
    onDelete: () -> Unit,
    onReport: (T: FeedReportType) -> Unit,
    onTranslate: () -> Unit,
    isMine: Boolean?,
) {
    TopAppBar(
        navigationIcon = {
            TextButton(
                onClick = {  navController.popBackStack() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.darkGray
                    )
                    Text(
                        text = stringResource(R.string.feed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.darkGray
                    )
                }
            }
        },
        title = {},

        actions = {
            PostCommentActionsMenu(
                enumClass = FeedReportType::class,
                isMine = isMine,
                onDelete = onDelete,
                onReport = onReport,
                onTranslate = onTranslate,
                isComment = false,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.padding(4.dp))
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}


@Composable
@Preview
private fun Preview() {
    Theme { FeedPostNavigationBar(rememberNavController(), {}, {}, {}, false) }
}