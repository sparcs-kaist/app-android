package org.sparcs.soap.materialpreview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Feed
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.theme.ui.Theme

// ---------------------------------------------------------------------------------------------
// Shared building blocks (all default Material 3: default colours, 24dp icons, default tint)
// ---------------------------------------------------------------------------------------------

@Composable
private fun BackIcon() {
    IconButton(onClick = {}) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}

@Composable
private fun CloseIcon() {
    IconButton(onClick = {}) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.close)
        )
    }
}

@Composable
private fun OverflowIcon() {
    IconButton(onClick = {}) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more)
        )
    }
}

/** Compose/modal screens: Close + title + text action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeTopBar(
    title: String,
    actionLabel: String,
    enabled: Boolean,
    busy: Boolean,
) {
    TopAppBar(
        navigationIcon = { CloseIcon() },
        title = { Text(title) },
        actions = {
            if (busy) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                TextButton(onClick = {}, enabled = enabled) {
                    Text(actionLabel)
                }
            }
        }
    )
}

/** Tab roots: Scaffold with a LargeTopAppBar and the creation action as a FAB. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabRootScaffold(
    title: String,
    actions: @Composable () -> Unit = {},
    fab: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                actions = { actions() }
            )
        },
        floatingActionButton = { fab?.invoke() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Detail screens
// ---------------------------------------------------------------------------------------------

// before: features/post/components/PostNavigationBar.kt#Preview
// iOS "‹ Board" back label -> standard back arrow; the board name becomes the bar title.
// Translate / summarize / report / delete stay in the overflow menu (MoreVert).
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "PostNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostNavigationBar() {
    Theme {
        Surface {
            TopAppBar(
                navigationIcon = { BackIcon() },
                title = { Text("Board") },
                actions = { OverflowIcon() }
            )
        }
    }
}

// before: features/feedPost/components/FeedPostNavigationBar.kt#Preview
// iOS "‹ Feed" back label -> standard back arrow with this screen's title.
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "FeedPostNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_FeedPostNavigationBar() {
    Theme {
        Surface {
            TopAppBar(
                navigationIcon = { BackIcon() },
                title = { Text("Post") },
                actions = { OverflowIcon() }
            )
        }
    }
}

// before: features/postCompose/components/PostComposeNavigationBar.kt#Preview
@Preview(name = "PostComposeNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostComposeNavigationBar() {
    Theme {
        Surface {
            ComposeTopBar(
                title = stringResource(R.string.write),
                actionLabel = stringResource(R.string.submit),
                enabled = false,
                busy = false
            )
        }
    }
}

// before: features/postCompose/components/PostComposeNavigationBar.kt#Preview
@Preview(name = "PostComposeNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostComposeNavigationBar_Enabled() {
    Theme {
        Surface {
            ComposeTopBar(
                title = stringResource(R.string.write),
                actionLabel = stringResource(R.string.submit),
                enabled = true,
                busy = false
            )
        }
    }
}

// before: features/postCompose/components/PostComposeNavigationBar.kt#Preview
@Preview(name = "PostComposeNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostComposeNavigationBar_Uploading() {
    Theme {
        Surface {
            ComposeTopBar(
                title = stringResource(R.string.write),
                actionLabel = stringResource(R.string.submit),
                enabled = true,
                busy = true
            )
        }
    }
}

// before: features/feedPostCompose/components/FeedPostComposeNavigationBar.kt#Preview
@Preview(name = "FeedPostComposeNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_FeedPostComposeNavigationBar() {
    Theme {
        Surface {
            ComposeTopBar(
                title = stringResource(R.string.write),
                actionLabel = stringResource(R.string.submit),
                enabled = false,
                busy = false
            )
        }
    }
}

// before: features/feedPostCompose/components/FeedPostComposeNavigationBar.kt#Preview
@Preview(name = "FeedPostComposeNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_FeedPostComposeNavigationBar_Enabled() {
    Theme {
        Surface {
            ComposeTopBar(
                title = stringResource(R.string.write),
                actionLabel = stringResource(R.string.submit),
                enabled = true,
                busy = false
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tab roots
// ---------------------------------------------------------------------------------------------

// before: features/feed/components/FeedViewNavigationBar.kt#Preview
// "+" in the bar -> ExtendedFloatingActionButton; "..." (settings) -> MoreVert overflow.
@Preview(name = "FeedViewNavigationBar", showBackground = true, widthDp = 411, heightDp = 300)
@Composable
private fun Material_FeedViewNavigationBar() {
    Theme {
        Surface {
            TabRootScaffold(
                title = stringResource(R.string.start),
                actions = { OverflowIcon() },
                fab = {
                    ExtendedFloatingActionButton(
                        onClick = {},
                        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        text = { Text(stringResource(R.string.write)) }
                    )
                }
            )
        }
    }
}

// before: features/taxiList/components/TaxiListNavigationBar.kt (no @Preview)
// "+" (create room) -> ExtendedFloatingActionButton; chat stays as a 24dp action.
@Preview(name = "TaxiListNavigationBar", showBackground = true, widthDp = 411, heightDp = 300)
@Composable
private fun Material_TaxiListNavigationBar() {
    Theme {
        Surface {
            TabRootScaffold(
                title = stringResource(R.string.taxi),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.chats)
                        )
                    }
                },
                fab = {
                    ExtendedFloatingActionButton(
                        onClick = {},
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.taxi_room_creation)) }
                    )
                }
            )
        }
    }
}

// before: features/boardList/components/BoardListNavigationBar.kt#Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "BoardListNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_BoardListNavigationBar() {
    Theme {
        Surface {
            LargeTopAppBar(title = { Text(stringResource(R.string.boards)) })
        }
    }
}

// before: features/boardList/components/BoardListNavigationBar.kt#Preview
// Small-bar alternative for tab roots (what LargeTopAppBar collapses to on scroll).
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "BoardListNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_BoardListNavigationBar_Small() {
    Theme {
        Surface {
            TopAppBar(title = { Text(stringResource(R.string.boards)) })
        }
    }
}

// before: features/timetable/components/TimetableNavigationBar.kt#TimetableNavigationBarPreview
// "+" dropdown (add class / new activity) -> ExtendedFloatingActionButton.
@Preview(name = "TimetableViewNavigationBar", showBackground = true, widthDp = 411, heightDp = 300)
@Composable
private fun Material_TimetableViewNavigationBar() {
    Theme {
        Surface {
            TabRootScaffold(
                title = stringResource(R.string.timetable),
                fab = {
                    ExtendedFloatingActionButton(
                        onClick = {},
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.activity_add)) }
                    )
                }
            )
        }
    }
}

// before: features/search/components/SearchNavigationBar.kt (no @Preview)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "SearchNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_SearchNavigationBar() {
    Theme {
        Surface {
            LargeTopAppBar(title = { Text(stringResource(R.string.search)) })
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Taxi chat
// ---------------------------------------------------------------------------------------------

// before: features/taxiChat/components/TaxiChatViewNavigationBar.kt#Preview
// Center-aligned bar + ArrowBackIos -> TopAppBar with back arrow, two-line title, MoreVert.
// Arrival row -> ListItem with AssistChip ("Status") + FilterChip ("Arrival").
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TaxiChatViewNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiChatViewNavigationBar() {
    Theme {
        Surface {
            val room = TaxiRoom.mock()
            val arrivedCount = room.participants.count { it.isArrived }
            val totalCount = room.participants.size
            val isAllArrived = totalCount > 0 && arrivedCount == totalCount
            val isArrived = false
            val arrivalText = when {
                isAllArrived -> pluralStringResource(R.plurals.taxi_all_arrived, totalCount, totalCount)
                arrivedCount == 0 -> stringResource(R.string.taxi_arrived_question)
                else -> pluralStringResource(R.plurals.taxi_arrived_count, arrivedCount, arrivedCount)
            }

            Column {
                TopAppBar(
                    navigationIcon = { BackIcon() },
                    title = {
                        Column {
                            Text(
                                text = "${room.title} ${room.emojiIdentifier.display}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${room.source.title.localized()} → ${room.destination.title.localized()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = { OverflowIcon() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(arrivalText) },
                    trailingContent = {
                        Row {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.taxi_status)) }
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = isArrived,
                                onClick = {},
                                label = {
                                    Text(
                                        stringResource(
                                            if (isArrived) R.string.taxi_arrival_complete else R.string.taxi_arrival
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            )
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Settings / lecture detail
// ---------------------------------------------------------------------------------------------

// before: features/settings/components/SettingsViewNavigationBar.kt (no @Preview)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "SettingsViewNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_SettingsViewNavigationBar() {
    Theme {
        Surface {
            TopAppBar(
                navigationIcon = { BackIcon() },
                title = { Text(stringResource(R.string.settings)) }
            )
        }
    }
}

// before: features/settings/components/SettingsViewNavigationBar.kt (no @Preview)
// isSearchEnabled = true; selected state -> IconToggleButton (default checked colour).
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "SettingsViewNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_SettingsViewNavigationBar_Search() {
    Theme {
        Surface {
            TopAppBar(
                navigationIcon = { BackIcon() },
                title = { Text(stringResource(R.string.interested_departments)) },
                actions = {
                    IconToggleButton(checked = false, onCheckedChange = {}) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                }
            )
        }
    }
}

// before: features/settings/components/SettingsViewNavigationBar.kt (no @Preview)
// isEditable = true, isDoneEnabled = true.
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "SettingsViewNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_SettingsViewNavigationBar_Editable() {
    Theme {
        Surface {
            TopAppBar(
                navigationIcon = { BackIcon() },
                title = { Text(stringResource(R.string.interested_departments)) },
                actions = {
                    TextButton(onClick = {}, enabled = true) {
                        Text(stringResource(R.string.done))
                    }
                }
            )
        }
    }
}

// before: features/lectureDetail/components/LectureDetailNavigationBar.kt#Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "LectureDetailNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_LectureDetailNavigationBar() {
    Theme {
        Surface {
            TopAppBar(
                navigationIcon = { BackIcon() },
                title = { Text("title", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_course)
                        )
                    }
                }
            )
        }
    }
}

// before: features/lectureDetail/components/LectureDetailNavigationBar.kt#Preview
// isCurrentTimetable = true, wishlisted.
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "LectureDetailNavigationBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_LectureDetailNavigationBar_InTimetable() {
    Theme {
        Surface {
            TopAppBar(
                navigationIcon = { BackIcon() },
                title = { Text("title", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    IconToggleButton(checked = true, onCheckedChange = {}) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Wishlist"
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Navigation buttons
// ---------------------------------------------------------------------------------------------

// before: features/navigationBar/components/NavigationButtons.kt#Preview
// SearchButton: default IconButton, 24dp icon, default tint.
@Preview(name = "SearchButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_SearchButton() {
    Theme {
        Surface {
            IconToggleButton(checked = false, onCheckedChange = {}) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
        }
    }
}

// before: features/navigationBar/components/NavigationButtons.kt#Preview
// AddButton, ChatButton, SearchButton (unselected / selected), DismissButton (-> back arrow).
@Preview(name = "NavigationButtons", showBackground = true, widthDp = 411)
@Composable
private fun Material_NavigationButtons() {
    Theme {
        Surface {
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
                IconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Filled.Add, contentDescription = "Add (disabled)")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = stringResource(R.string.chats))
                }
                IconToggleButton(checked = false, onCheckedChange = {}) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                }
                IconToggleButton(checked = true, onCheckedChange = {}) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                }
                BackIcon()
                CloseIcon()
                OverflowIcon()
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Bottom navigation
// ---------------------------------------------------------------------------------------------

private data class TopBarsTabSpec(
    val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val topBarsTabs = listOf(
    TopBarsTabSpec(R.string.start, Icons.AutoMirrored.Filled.Feed, Icons.AutoMirrored.Outlined.Feed),
    TopBarsTabSpec(
        R.string.boards,
        Icons.AutoMirrored.Filled.FormatListBulleted,
        Icons.AutoMirrored.Outlined.FormatListBulleted
    ),
    TopBarsTabSpec(R.string.timetable, Icons.Filled.TableChart, Icons.Outlined.TableChart),
    TopBarsTabSpec(R.string.taxi, Icons.Filled.LocalTaxi, Icons.Outlined.LocalTaxi),
    TopBarsTabSpec(R.string.search, Icons.Filled.Search, Icons.Outlined.Search),
)

@Composable
private fun MaterialBottomBar(selectedIndex: Int = 0) {
    NavigationBar {
        topBarsTabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            val label = stringResource(tab.label)
            NavigationBarItem(
                selected = selected,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = label
                    )
                },
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}

// before: features/navigationBar/NavigationBar.kt#Preview
// Full tab-root chrome: LargeTopAppBar + FAB + default NavigationBar (secondaryContainer pill).
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "MainTabBar", showBackground = true, widthDp = 411, heightDp = 640)
@Composable
private fun Material_MainTabBar() {
    Theme {
        Surface {
            Scaffold(
                topBar = {
                    LargeTopAppBar(
                        title = { Text(stringResource(R.string.start)) },
                        actions = { OverflowIcon() }
                    )
                },
                bottomBar = { MaterialBottomBar(selectedIndex = 0) },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = {},
                        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        text = { Text(stringResource(R.string.write)) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }
        }
    }
}

// before: features/navigationBar/NavigationBar.kt#Preview
@Preview(name = "MainTabBar", showBackground = true, widthDp = 411)
@Composable
private fun Material_MainTabBar_BarOnly() {
    Theme {
        Surface {
            MaterialBottomBar(selectedIndex = 0)
        }
    }
}

// before: features/navigationBar/NavigationBar.kt#Preview
// Rail layout (used when the window is wider than a portrait phone).
@Preview(name = "MainTabBar", showBackground = true, widthDp = 411, heightDp = 480)
@Composable
private fun Material_MainTabBar_Rail() {
    Theme {
        Surface {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    header = {
                        ExtendedFloatingActionButton(
                            onClick = {},
                            icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            text = { Text(stringResource(R.string.write)) },
                            expanded = false
                        )
                    }
                ) {
                    topBarsTabs.forEachIndexed { index, tab ->
                        val selected = index == 0
                        val label = stringResource(tab.label)
                        NavigationRailItem(
                            selected = selected,
                            onClick = {},
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = label
                                )
                            },
                            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
