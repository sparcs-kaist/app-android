package org.sparcs.soap.app.features.friends

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Friend
import org.sparcs.soap.app.features.friends.components.InitialsAvatar
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.isDark
import org.sparcs.soap.buddyPreviewSupport.friends.PreviewFriendsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListView(
    viewModel: FriendsListViewModelProtocol = hiltViewModel<FriendsListViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val alertState by viewModel.alertState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var friendToDelete by remember { mutableStateOf<Friend?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.loadMyCode()
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(R.string.friends_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Channel.AddFriends.name) },
                icon = { Icon(Icons.Rounded.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.friends_add_friend)) }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .analyticsScreen("Friends")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val current = state) {
                FriendsListViewModel.ViewState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                FriendsListViewModel.ViewState.Empty -> FriendsMessage(
                    icon = Icons.Rounded.Group,
                    title = stringResource(R.string.friends_empty_title),
                    message = stringResource(R.string.friends_empty_message)
                )

                is FriendsListViewModel.ViewState.Error -> FriendsMessage(
                    icon = Icons.Rounded.Warning,
                    title = stringResource(R.string.friends_load_error_title),
                    message = current.error.localizedMessage
                        ?: stringResource(R.string.error_unknown_try_again),
                    action = {
                        Button(onClick = viewModel::load) {
                            Text(stringResource(R.string.friends_try_again))
                        }
                    }
                )

                is FriendsListViewModel.ViewState.Loaded -> PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // Room for the FAB so the last row can scroll clear of it.
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(current.friends, key = { it.id }) { friend ->
                            FriendRow(
                                friend = friend,
                                onClick = {
                                    navController.navigate(
                                        "${Channel.FriendTimetable.name}/${friend.id}?name=${android.net.Uri.encode(friend.name)}"
                                    )
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(friend) },
                                onDelete = { friendToDelete = friend },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    friendToDelete?.let { friend ->
        AlertDialog(
            onDismissRequest = { friendToDelete = null },
            title = { Text(stringResource(R.string.friends_delete_title)) },
            text = { Text(stringResource(R.string.friends_delete_message, friend.name)) },
            confirmButton = {
                TextButton(onClick = {
                    friendToDelete = null
                    viewModel.delete(friend)
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { friendToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    GlobalAlertDialog(
        isPresented = alertState != null,
        state = alertState,
        onDismiss = viewModel::dismissAlert
    )
}

/**
 * Swipe right to (un)favourite, swipe left to delete; the same actions live in
 * the overflow menu and on long press for anyone who doesn't discover swipes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun FriendRow(
    friend: Friend,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> onToggleFavorite()
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            // Neither action removes the row by itself; it springs back and the
            // list updates once the server confirms.
            false
        }
    )
    // Swipes can still leave it resting past the threshold; reset it.
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            scope.launch { dismissState.reset() }
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            SwipeBackground(direction = dismissState.dismissDirection, isFavorite = friend.isFavorite)
        }
    ) {
        ListItem(
            headlineContent = {
                Text(friend.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = if (friend.hasScheduleNow) {
                { Text(stringResource(R.string.friends_in_class_now)) }
            } else {
                null
            },
            leadingContent = { FriendAvatar(friend) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (friend.isFavorite) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = stringResource(R.string.friends_favorite_badge),
                            tint = FavoriteColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.friends_more_options)
                            )
                        }
                        FriendMenu(
                            expanded = isMenuExpanded,
                            isFavorite = friend.isFavorite,
                            onDismiss = { isMenuExpanded = false },
                            onToggleFavorite = onToggleFavorite,
                            onDelete = onDelete
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { isMenuExpanded = true }
            )
        )
    }
}

@Composable
private fun FriendMenu(
    expanded: Boolean,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = {
                Text(stringResource(if (isFavorite) R.string.friends_unfavorite else R.string.friends_favorite))
            },
            leadingIcon = {
                Icon(if (isFavorite) Icons.Rounded.StarOutline else Icons.Rounded.Star, contentDescription = null)
            },
            onClick = {
                onDismiss()
                onToggleFavorite()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete)) },
            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}

@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue, isFavorite: Boolean) {
    val (color, icon, alignment) = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            if (isFavorite) Icons.Rounded.StarOutline else Icons.Rounded.Star,
            Alignment.CenterStart
        )

        SwipeToDismissBoxValue.EndToStart -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Rounded.Delete,
            Alignment.CenterEnd
        )

        SwipeToDismissBoxValue.Settled -> Triple(Color.Transparent, null, Alignment.Center)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = if (direction == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }
    }
}

/** Letter avatar with a green presence dot while the friend is in class. */
@Composable
private fun FriendAvatar(friend: Friend) {
    Box {
        InitialsAvatar(name = friend.name)
        if (friend.hasScheduleNow) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(2.dp)
                    .background(inClassColor(), CircleShape)
            )
        }
    }
}

@Composable
private fun FriendsMessage(
    icon: ImageVector,
    title: String,
    message: String,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        action?.invoke()
    }
}

private val FavoriteColor = Color(0xFFF9AB00)

@Composable
internal fun inClassColor(): Color =
    if (MaterialTheme.colorScheme.isDark()) Color(0xFF81C995) else Color(0xFF1E8E3E)

// MARK: - Previews

@Preview(showBackground = true)
@Composable
private fun FriendsListLoadedPreview() {
    Theme {
        FriendsListView(
            viewModel = PreviewFriendsListViewModel(
                FriendsListViewModel.ViewState.Loaded(Friend.mockList())
            ),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun FriendsListEmptyPreview() {
    Theme {
        FriendsListView(
            viewModel = PreviewFriendsListViewModel(FriendsListViewModel.ViewState.Empty),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendsListErrorPreview() {
    Theme {
        FriendsListView(
            viewModel = PreviewFriendsListViewModel(
                FriendsListViewModel.ViewState.Error(Exception("The server couldn’t be reached."))
            ),
            navController = rememberNavController()
        )
    }
}
