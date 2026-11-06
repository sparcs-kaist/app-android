package org.sparcs.soap.app.features.settings.ara

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DragHandle
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    val listState = rememberLazyListState()
    val dragState = remember(listState) { SectionDragState(listState) }

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
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = padding.onlyBottom() + PaddingValues(bottom = 16.dp)
            ) {
                shownSections(viewModel, boards, dragState)

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
    dragState: SectionDragState,
) {
    val sections = viewModel.sections
    item(key = "shown-header") { SettingsSectionHeader(stringResource(R.string.board_sections_shown)) }
    itemsIndexed(sections, key = { _, section -> shownKey(section) }) { index, section ->
        val board = boardFor(section, boards)
        val title = sectionTitle(section, board)
        val isDragged = dragState.draggedID == section.id
        val shapes = ListItemDefaults.segmentedShapes(index, sections.size)
        val moveUp = stringResource(R.string.move_up)
        val moveDown = stringResource(R.string.move_down)
        SettingsSegmentRow(
            index,
            sections.size,
            // The dragged row follows the finger above the others; the rest slide out of its way.
            if (isDragged) {
                Modifier
                    .zIndex(1f)
                    .graphicsLayer { translationY = dragState.offset }
            } else Modifier.animateItem()
        ) {
            SegmentedListItem(
                shapes = shapes,
                colors = settingsSegmentedColors(),
                leadingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DragHandle(
                            contentDescription = stringResource(R.string.board_sections_reorder, title),
                            state = dragState,
                            section = section,
                            sections = { viewModel.sections },
                            onMove = { viewModel.move(section, it) },
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(sectionIcon(section, board), contentDescription = null)
                    }
                },
                trailingContent = {
                    IconButton(onClick = { viewModel.remove(section) }, shapes = IconButtonDefaults.shapes()) {
                        Icon(
                            Icons.Outlined.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.board_sections_remove, title),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    // Lifted while dragged, so it reads as picked up and covers the row it passes.
                    .graphicsLayer {
                        if (isDragged) {
                            shadowElevation = 8.dp.toPx()
                            shape = shapes.shape
                            clip = true
                        }
                    }
                    // The handle needs a finger; screen readers move rows with these actions instead.
                    .semantics {
                        customActions = listOfNotNull(
                            if (index > 0) CustomAccessibilityAction(moveUp) { viewModel.move(section, -1); true } else null,
                            if (index < sections.lastIndex) {
                                CustomAccessibilityAction(moveDown) { viewModel.move(section, 1); true }
                            } else null,
                        )
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

private fun shownKey(section: BoardListSection) = "shown-${section.id}"

/**
 * The leading handle of a shown row: dragging it moves the row through the list, one place each
 * time it passes the middle of a neighbour, with a tick to mark the move.
 */
@Composable
private fun DragHandle(
    contentDescription: String,
    state: SectionDragState,
    section: BoardListSection,
    sections: () -> List<BoardListSection>,
    onMove: (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val currentOnMove by rememberUpdatedState(onMove)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .pointerInput(section.id) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        state.start(section.id)
                    },
                    onDragEnd = { state.stop() },
                    onDragCancel = { state.stop() },
                    onDrag = { change, amount ->
                        change.consume()
                        val step = state.drag(amount.y, sections())
                        if (step != 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            currentOnMove(step)
                        }
                    }
                )
            }
    ) {
        Icon(
            Icons.Outlined.DragHandle,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** The row being dragged by its handle, and how far it has moved from its place in the list. */
@Stable
private class SectionDragState(private val listState: LazyListState) {
    var draggedID by mutableStateOf<String?>(null)
        private set
    var offset by mutableFloatStateOf(0f)
        private set

    fun start(id: String) {
        draggedID = id
        offset = 0f
    }

    fun stop() {
        draggedID = null
        offset = 0f
    }

    /**
     * Adds [delta] to the drag and returns -1 or 1 once the row's middle passes the middle of the
     * row above or below, after taking that row's height back off the offset, since the list
     * moves the dragged row there; 0 otherwise.
     */
    fun drag(delta: Float, sections: List<BoardListSection>): Int {
        val id = draggedID ?: return 0
        offset += delta
        val index = sections.indexOfFirst { it.id == id }
        val direction = if (offset > 0) 1 else if (offset < 0) -1 else return 0
        val neighbour = sections.getOrNull(index + direction) ?: return 0
        val items = listState.layoutInfo.visibleItemsInfo
        val current = items.firstOrNull { it.key == shownKey(sections[index]) } ?: return 0
        val other = items.firstOrNull { it.key == shownKey(neighbour) } ?: return 0
        val middle = current.offset + current.size / 2 + offset
        val passed = if (direction > 0) middle > other.offset + other.size / 2
        else middle < other.offset + other.size / 2
        if (!passed) return 0
        offset -= direction * other.size
        return direction
    }
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
