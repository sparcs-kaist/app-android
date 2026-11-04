package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraContentReportType
import org.sparcs.soap.app.features.post.PostTranslator
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Locale

data class PostMenuState(
    val isMine: Boolean?,
    val canEdit: Boolean = false,
    val isAuthorBlocked: Boolean? = null,
    val canSummarise: Boolean = false,
    val isSummaryShown: Boolean = false,
    val translationTargets: List<String> = emptyList(),
    val suggestedTargets: List<String> = emptyList(),
    val translationTarget: String? = null,
)

data class PostMenuActions(
    val onEdit: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onReport: (AraContentReportType) -> Unit = {},
    val onSetAuthorBlocked: (Boolean) -> Unit = {},
    val onTranslate: (String) -> Unit = {},
    val onShowOriginal: () -> Unit = {},
    val onSummarise: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PostActionsMenu(state: PostMenuState, actions: PostMenuActions) {
    var expanded by remember { mutableStateOf(false) }
    var isReportExpanded by remember { mutableStateOf(false) }
    var isPickingLanguage by remember { mutableStateOf(false) }
    val dismiss = {
        expanded = false
        isReportExpanded = false
    }

    if (isPickingLanguage) {
        LanguagePickerSheet(
            targets = state.translationTargets,
            suggested = state.suggestedTargets,
            current = state.translationTarget,
            onSelect = {
                isPickingLanguage = false
                actions.onTranslate(it)
            },
            onDismiss = { isPickingLanguage = false },
        )
    }

    // The groups follow where the old menu drew its dividers: reporting the author, reading
    // tools, then the author's own edit and delete.
    val showsModeration = state.isMine == false
    val showsOwnActions = state.isMine == true
    val groupCount = 1 + (if (showsModeration) 1 else 0) + (if (showsOwnActions) 1 else 0)
    var group = 0

    Box {
        IconButton(onClick = { expanded = true }, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
        }

        ExpressiveMenu(expanded = expanded, onDismissRequest = dismiss) {
            if (showsModeration) {
                MenuGroup(index = group++, count = groupCount) {
                    val count = if (state.isAuthorBlocked != null) 2 else 1
                    ExpandableItem(
                        label = stringResource(R.string.report),
                        icon = { Icon(Icons.Outlined.SmsFailed, contentDescription = null) },
                        // Open, its reasons sit below it, before Block author if shown.
                        shape = menuItemShape(0, count + if (isReportExpanded) 1 else 0),
                        isExpanded = isReportExpanded,
                        onToggle = { isReportExpanded = !isReportExpanded },
                    ) {
                        val types = AraContentReportType.entries
                        types.forEachIndexed { i, type ->
                            DropdownMenuItem(
                                text = { Text(stringResource(type.labelRes)) },
                                onClick = { dismiss(); actions.onReport(type) },
                                shape = menuItemShape(i + 1, types.size + count),
                            )
                        }
                    }
                    state.isAuthorBlocked?.let { isBlocked ->
                        BlockItem(isBlocked, menuItemShape(1, 2)) { dismiss(); actions.onSetAuthorBlocked(!isBlocked) }
                    }
                }
            }

            MenuGroup(index = group++, count = groupCount) {
                val translateCount = translateItemCount(state)
                val count = translateCount + if (state.canSummarise) 1 else 0
                TranslateItems(
                    state = state,
                    count = count,
                    onTranslate = { dismiss(); actions.onTranslate(it) },
                    onShowOriginal = { dismiss(); actions.onShowOriginal() },
                    onPickLanguage = { dismiss(); isPickingLanguage = true },
                )

                if (state.canSummarise) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.summarise)) },
                        onClick = { dismiss(); actions.onSummarise() },
                        shape = menuItemShape(count - 1, count),
                        enabled = !state.isSummaryShown,
                        leadingIcon = { Icon(Icons.Outlined.Summarize, contentDescription = null) },
                    )
                }
            }

            if (showsOwnActions) {
                MenuGroup(index = group, count = groupCount) {
                    val count = if (state.canEdit) 2 else 1
                    if (state.canEdit) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            onClick = { dismiss(); actions.onEdit() },
                            shape = menuItemShape(0, count),
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        onClick = { dismiss(); actions.onDelete() },
                        shape = menuItemShape(count - 1, count),
                        leadingIcon = {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                    )
                }
            }
        }
    }
}

/** Blocking or unblocking the author, as a menu item of the given [shape]. */
@Composable
fun BlockItem(isBlocked: Boolean, shape: Shape, onClick: () -> Unit) {
    if (isBlocked) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.unblock_author)) },
            onClick = onClick,
            shape = shape,
            leadingIcon = { Icon(Icons.Outlined.HowToReg, contentDescription = null) },
        )
    } else {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.block_author), color = MaterialTheme.colorScheme.error) },
            onClick = onClick,
            shape = shape,
            leadingIcon = { Icon(Icons.Outlined.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        )
    }
}

/** How many items [TranslateItems] shows for [state]: one or two. */
private fun translateItemCount(state: PostMenuState): Int {
    val quickTarget = PostTranslator.quickTarget(state.suggestedTargets, state.translationTargets, state.translationTarget)
    return when {
        state.translationTargets.isEmpty() -> 1
        state.translationTarget != null || quickTarget != null -> 2
        else -> 1
    }
}

/** The translate items, first in a group of [count] items. */
@Composable
private fun TranslateItems(
    state: PostMenuState,
    count: Int,
    onTranslate: (String) -> Unit,
    onShowOriginal: () -> Unit,
    onPickLanguage: () -> Unit,
) {
    val quickTarget = PostTranslator.quickTarget(state.suggestedTargets, state.translationTargets, state.translationTarget)
    when {
        state.translationTargets.isEmpty() -> DropdownMenuItem(
            text = { Text(stringResource(R.string.translate)) },
            onClick = {},
            shape = menuItemShape(0, count),
            enabled = false,
            leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null) },
        )

        state.translationTarget != null -> {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.show_original)) },
                onClick = onShowOriginal,
                shape = menuItemShape(0, count),
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = null) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.translate_change_language)) },
                onClick = onPickLanguage,
                shape = menuItemShape(1, count),
                leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null) },
            )
        }

        quickTarget != null -> {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.translate_to_language, languageName(quickTarget))) },
                onClick = { onTranslate(quickTarget) },
                shape = menuItemShape(0, count),
                leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.translate_other_languages)) },
                onClick = onPickLanguage,
                shape = menuItemShape(1, count),
                leadingIcon = { Icon(Icons.Outlined.Language, contentDescription = null) },
            )
        }

        else -> DropdownMenuItem(
            text = { Text(stringResource(R.string.translate)) },
            onClick = onPickLanguage,
            shape = menuItemShape(0, count),
            leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null) },
        )
    }
}

/** A menu item that opens [content] below it, such as the report reasons. */
@Composable
internal fun ExpandableItem(
    label: String,
    icon: @Composable () -> Unit,
    shape: Shape,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onToggle,
        shape = shape,
        leadingIcon = icon,
        trailingContent = {
            Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
        },
    )
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column { content() }
    }
}

fun languageName(code: String): String =
    Locale.forLanguageTag(code).getDisplayLanguage(Locale.getDefault()).ifBlank { code }

@Preview(showBackground = true)
@Composable
private fun PostActionsMenuPreview() {
    Theme {
        PostActionsMenu(
            state = PostMenuState(
                isMine = false,
                isAuthorBlocked = false,
                canSummarise = true,
                translationTargets = listOf("en", "ja", "zh"),
                suggestedTargets = listOf("en"),
            ),
            actions = PostMenuActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostActionsMenuTranslatedPreview() {
    Theme {
        PostActionsMenu(
            state = PostMenuState(
                isMine = true,
                canEdit = true,
                translationTargets = listOf("en", "ja", "zh"),
                suggestedTargets = listOf("en"),
                translationTarget = "en",
            ),
            actions = PostMenuActions(),
        )
    }
}
