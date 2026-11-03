package org.sparcs.soap.app.features.settings.ara

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.ara.AraBlockedUser
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersView(
    navController: NavController,
    viewModel: BlockedUsersViewModelProtocol = hiltViewModel<BlockedUsersViewModel>(),
) {
    LaunchedEffect(viewModel) { viewModel.load() }

    val pullState = rememberPullToRefreshState()
    PullToRefreshHapticHandler(pullState, viewModel.isRefreshing)

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.blocked_users),
                onDismiss = { navController.popBackStack() },
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.analyticsScreen("Blocked Users")
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = viewModel::refresh,
            state = pullState,
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                when (val state = viewModel.state) {
                    BlockedUsersViewModel.ViewState.Loading -> item(key = "loading") {
                        FullHeight { CircularProgressIndicator() }
                    }

                    is BlockedUsersViewModel.ViewState.Error -> item(key = "error") {
                        FullHeight {
                            ErrorView(
                                error = state.error,
                                defaultMessageResId = R.string.error_fetch_blocked_users,
                                onRetry = viewModel::load
                            )
                        }
                    }

                    is BlockedUsersViewModel.ViewState.Loaded -> if (state.users.isEmpty()) {
                        item(key = "empty") { FullHeight { EmptyBlockedUsers() } }
                    } else {
                        items(state.users, key = { it.id }) { user ->
                            BlockedUserRow(
                                user = user,
                                onUnblock = { viewModel.unblock(user) },
                                modifier = Modifier.animateItem()
                            )
                        }
                        item(key = "footer") {
                            Text(
                                text = stringResource(R.string.blocked_users_footer),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        onDismiss = { viewModel.isAlertPresented = false },
        state = viewModel.alertState
    )
}

@Composable
private fun LazyItemScope.FullHeight(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillParentMaxSize()
    ) {
        content()
    }
}

@Composable
private fun EmptyBlockedUsers() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Outlined.HowToReg,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.blocked_users_empty),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.blocked_users_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockedUserRow(user: AraBlockedUser, onUnblock: () -> Unit, modifier: Modifier = Modifier) {
    val currentOnUnblock by rememberUpdatedState(onUnblock)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) currentOnUnblock()
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = { UnblockSwipeBackground() },
        modifier = modifier
    ) {
        ListItem(
            leadingContent = {
                AsyncImage(
                    model = user.profilePictureURL?.toString(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            },
            headlineContent = { Text(user.nickname, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingContent = {
                OutlinedButton(onClick = onUnblock) { Text(stringResource(R.string.unblock)) }
            }
        )
    }
}

@Composable
private fun UnblockSwipeBackground() {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.PersonOff, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            Text(
                text = stringResource(R.string.unblock),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

private class PreviewBlockedUsersViewModel(override val state: BlockedUsersViewModel.ViewState) : BlockedUsersViewModelProtocol {
    override val isRefreshing: Boolean = false
    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false
    override fun load() {}
    override fun refresh() {}
    override fun unblock(user: AraBlockedUser) {}
}

@Preview
@Composable
private fun Preview() {
    Theme {
        BlockedUsersView(
            navController = rememberNavController(),
            viewModel = PreviewBlockedUsersViewModel(
                BlockedUsersViewModel.ViewState.Loaded(
                    listOf(AraBlockedUser(1, "신나는 포도", null), AraBlockedUser(2, "귀여운 도토리", null))
                )
            )
        )
    }
}

@Preview
@Composable
private fun EmptyPreview() {
    Theme {
        BlockedUsersView(
            navController = rememberNavController(),
            viewModel = PreviewBlockedUsersViewModel(BlockedUsersViewModel.ViewState.Loaded(emptyList()))
        )
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    Theme {
        BlockedUsersView(
            navController = rememberNavController(),
            viewModel = PreviewBlockedUsersViewModel(BlockedUsersViewModel.ViewState.Error(Exception()))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeBackgroundPreview() {
    Theme { Box(Modifier.size(width = 360.dp, height = 72.dp)) { UnblockSwipeBackground() } }
}
