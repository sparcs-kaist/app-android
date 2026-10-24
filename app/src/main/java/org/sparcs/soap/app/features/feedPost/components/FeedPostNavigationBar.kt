package org.sparcs.soap.app.features.feedPost.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.feed.FeedReportType
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.features.post.components.PostCommentActionsMenu
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.darkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostNavigationBar(
    navController: NavController,
    onDelete: () -> Unit,
    onReport: (t: FeedReportType) -> Unit,
    onTranslate: () -> Unit,
    onSummarize: () -> Unit = {},
    isMine: Boolean?,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        navigationIcon = { BackButton(onClick = { navController.popBackStack() }) },
        title = {
            Text(
                text = stringResource(R.string.feed),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            PostCommentActionsMenu(
                enumClass = FeedReportType::class,
                isMine = isMine,
                onDelete = onDelete,
                onReport = onReport,
                onTranslate = onTranslate,
                onSummarize = onSummarize,
                isComment = false
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        scrollBehavior = scrollBehavior
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun Preview() {
    Theme { FeedPostNavigationBar(rememberNavController(), {}, {}, {}, {}, false) }
}