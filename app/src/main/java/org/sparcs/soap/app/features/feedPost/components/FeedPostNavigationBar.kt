package org.sparcs.soap.app.features.feedPost.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.feed.FeedReportType
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.features.post.components.PostCommentActionsMenu
import org.sparcs.soap.app.theme.ui.Theme

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