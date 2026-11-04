package org.sparcs.soap.app.features.settings.ara

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.boardList.components.icon
import org.sparcs.soap.app.features.boardList.components.sectionIcon
import org.sparcs.soap.app.features.boardList.components.sectionTitle
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSegmentRow
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.settings.components.settingsSegmentedColors
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.onlyBottom
import org.sparcs.soap.app.shared.extensions.plus
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.extensions.withoutBottom
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.post.PreviewBoardSectionsSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoardSectionsSettingsView(
    navController: NavController,
    viewModel: BoardSectionsSettingsViewModelProtocol = hiltViewModel<BoardSectionsSettingsViewModel>(),
) {
    LaunchedEffect(viewModel) { viewModel.loadBoards() }

    val topBarScrollBehavior = topAppBarScrollBehavior()
    val boardsState = viewModel.boardsState
    val boards = (boardsState as? BoardSectionsSettingsViewModel.BoardsState.Loaded)?.boards.orEmpty()

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.board_sections),
                onDismiss = { navController.popBackStack() },
                scrollBehavior = topBarScrollBehavior,
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Board Sections Settings")
    ) { padding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.withoutBottom())
        ) {
            // Each group is a card of segmented rows, as on the other settings pages; the list
            // scrolls behind the gesture bar and ends clear of it.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = padding.onlyBottom() + PaddingValues(bottom = 16.dp)
            ) {
                shownSections(viewModel, boards)

                when (boardsState) {
                    BoardSectionsSettingsViewModel.BoardsState.Loading -> item(key = "loading") {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                            LoadingIndicator()
                        }
                    }

                    is BoardSectionsSettingsViewModel.BoardsState.Error -> item(key = "error") {
                        ErrorView(
                            error = boardsState.error,
                            defaultMessageResId = R.string.error_fetch_boards,
                            onRetry = viewModel::loadBoards
                        )
                    }

                    is BoardSectionsSettingsViewModel.BoardsState.Loaded -> addableSections(viewModel, boardsState)
                }

                item(key = "reset") {
                    SettingsSegmentRow(0, 1, Modifier.padding(top = 24.dp)) {
                        SettingsNavigationItem(
                            text = stringResource(R.string.board_sections_reset),
                            onClick = viewModel::resetToDefault,
                            enabled = viewModel.sections != BoardListSection.defaults,
                            leadingContent = { Icon(Icons.Outlined.RestartAlt, contentDescription = null) },
                            destructive = true,
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.shownSections(
    viewModel: BoardSectionsSettingsViewModelProtocol,
    boards: List<AraBoard>,
) {
    val sections = viewModel.sections
    item(key = "shown-header") { SettingsSectionHeader(stringResource(R.string.board_sections_shown)) }
    itemsIndexed(sections, key = { _, section -> "shown-${section.id}" }) { index, section ->
        val board = boardFor(section, boards)
        val title = sectionTitle(section, board)
        SettingsSegmentRow(index, sections.size, Modifier.animateItem()) {
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(index, sections.size),
                colors = settingsSegmentedColors(),
                leadingContent = { Icon(sectionIcon(section, board), contentDescription = null) },
                trailingContent = {
                    Row {
                        IconButton(
                            onClick = { viewModel.move(section, -1) },
                            enabled = index > 0,
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = stringResource(R.string.move_up))
                        }
                        IconButton(
                            onClick = { viewModel.move(section, 1) },
                            enabled = index < sections.lastIndex,
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = stringResource(R.string.move_down))
                        }
                        IconButton(onClick = { viewModel.remove(section) }, shapes = IconButtonDefaults.shapes()) {
                            Icon(
                                Icons.Outlined.RemoveCircleOutline,
                                contentDescription = stringResource(R.string.board_sections_remove, title),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
            ) { Text(title) }
        }
    }
    item(key = "shown-footer") {
        Text(
            text = stringResource(R.string.board_sections_footer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Lined up with the header and the rows' text, inset 16dp and padded 16dp.
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}

private fun LazyListScope.addableSections(
    viewModel: BoardSectionsSettingsViewModelProtocol,
    loaded: BoardSectionsSettingsViewModel.BoardsState.Loaded,
) {
    val highlights = BoardSectionsOrder.addableHighlights(viewModel.sections)
    if (highlights.isNotEmpty()) {
        item(key = "highlights-header") { SettingsSectionHeader(stringResource(R.string.board_sections_highlights)) }
        itemsIndexed(highlights, key = { _, it -> "add-${it.id}" }) { index, section ->
            SettingsSegmentRow(index, highlights.size, Modifier.animateItem()) {
                AddSectionRow(section, board = null, onAdd = { viewModel.add(section) })
            }
        }
    }

    loaded.groups.forEach { group ->
        val addable = BoardSectionsOrder.addableBoards(viewModel.sections, loaded.boards, group)
        if (addable.isEmpty()) return@forEach
        item(key = "group-${group.id}") { SettingsSectionHeader(group.name.localized(), icon = group.icon) }
        itemsIndexed(addable, key = { _, it -> "add-board-${it.slug}" }) { index, board ->
            val section = BoardListSection.Board(board.slug)
            SettingsSegmentRow(index, addable.size, Modifier.animateItem()) {
                AddSectionRow(section, board = board, onAdd = { viewModel.add(section) })
            }
        }
    }
}

@Composable
private fun AddSectionRow(section: BoardListSection, board: AraBoard?, onAdd: () -> Unit) {
    val title = sectionTitle(section, board)
    SettingsNavigationItem(
        text = title,
        onClick = onAdd,
        leadingContent = { Icon(sectionIcon(section, board), contentDescription = null) },
        trailingContent = {
            Icon(
                Icons.Outlined.AddCircleOutline,
                contentDescription = stringResource(R.string.board_sections_add, title),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}

private fun boardFor(section: BoardListSection, boards: List<AraBoard>): AraBoard? =
    (section as? BoardListSection.Board)?.let { s -> boards.firstOrNull { it.slug == s.slug } }

@Preview(showBackground = true)
@Composable
private fun LoadedPreview() {
    Theme {
        BoardSectionsSettingsView(rememberNavController(), PreviewBoardSectionsSettingsViewModel())
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    Theme {
        BoardSectionsSettingsView(
            rememberNavController(),
            PreviewBoardSectionsSettingsViewModel(
                boardsState = BoardSectionsSettingsViewModel.BoardsState.Loading,
                sections = BoardListSection.defaults
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    Theme {
        BoardSectionsSettingsView(
            rememberNavController(),
            PreviewBoardSectionsSettingsViewModel(boardsState = BoardSectionsSettingsViewModel.BoardsState.Error(Exception()))
        )
    }
}
