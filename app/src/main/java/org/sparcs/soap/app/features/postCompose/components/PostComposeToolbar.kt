package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CheckableDropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.window.core.layout.WindowWidthSizeClass
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.shared.views.contentViews.ComposeToolbar
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.selectableMenuItemColors
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PostComposeToolbar(
    viewModel: PostComposeViewModelProtocol,
    isUploading: Boolean,
    onAddPhoto: () -> Unit,
    onAttachFile: () -> Unit,
) {
    val editor = viewModel.editor
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    val isExpanded = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) editor.closeFormatting()
    }

    Column(Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = editor.isFormatting && !isExpanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            // Floats above the toolbar in the same container, so the two read as one control.
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FloatingToolbarDefaults.ScreenOffset)
                    .padding(top = FloatingToolbarDefaults.ScreenOffset)
            ) {
                ComposeFormatPanel(editor, Modifier.fillMaxWidth())
            }
        }
        ComposeToolbar(
            trailing = if (editor.isEditing || isKeyboardVisible) {
                {
                    IconButton(
                        onClick = {
                            keyboard?.hide()
                            focusManager.clearFocus()
                        },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(Icons.Filled.KeyboardHide, contentDescription = stringResource(R.string.compose_hide_keyboard))
                    }
                }
            } else null
        ) {
            AnimatedVisibility(
                visible = editor.isEditing,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FormatButton(editor, showsPopup = isExpanded)
                    BulletListToggle(editor)
                }
            }
            IconButton(onClick = onAddPhoto, enabled = !isUploading, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = stringResource(R.string.add_photo))
            }
            IconButton(onClick = onAttachFile, enabled = !isUploading, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Outlined.AttachFile, contentDescription = stringResource(R.string.attach_file))
            }
            PostOptionsMenu(viewModel)
        }
    }
}

@Composable
private fun FormatButton(editor: ComposeEditorState, showsPopup: Boolean) {
    Box {
        IconToggleButton(
            checked = editor.isFormatting,
            onCheckedChange = { editor.toggleFormatting() },
            // Clear while off, so only the open state fills inside the toolbar.
            colors = IconButtonDefaults.filledTonalIconToggleButtonColors(containerColor = Color.Transparent),
            shapes = IconButtonDefaults.toggleableShapes()
        ) {
            Icon(Icons.Outlined.TextFormat, contentDescription = stringResource(R.string.text_formatting))
        }
        ExpressiveMenu(
            expanded = showsPopup && editor.isFormatting,
            onDismissRequest = editor::closeFormatting,
            properties = PopupProperties(focusable = false, dismissOnClickOutside = false)
        ) {
            MenuGroup(index = 0, count = 1) { ComposeFormatPanel(editor) }
        }
    }
}

@Composable
private fun PostOptionsMenu(viewModel: PostComposeViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.post_options))
        }
        ExpressiveMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MenuGroup(index = 0, count = 1) {
                val choosesAnonymity = viewModel.canChooseAnonymity && viewModel.editingPost == null
                val count = if (choosesAnonymity) 3 else 2
                var index = 0
                if (choosesAnonymity) {
                    PostOptionItem(stringResource(R.string.post_option_anonymous), viewModel.writeAsAnonymous, index++, count) {
                        viewModel.writeAsAnonymous = !viewModel.writeAsAnonymous
                    }
                }
                PostOptionItem(stringResource(R.string.nsfw), viewModel.isNSFW, index++, count) { viewModel.isNSFW = !viewModel.isNSFW }
                PostOptionItem(stringResource(R.string.political), viewModel.isPolitical, index, count) {
                    viewModel.isPolitical = !viewModel.isPolitical
                }
            }
        }
    }
}

/** An option that stays on or off, marked with a check while on; the menu stays open to set several. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PostOptionItem(label: String, checked: Boolean, index: Int, count: Int, onToggle: () -> Unit) {
    CheckableDropdownMenuItem(
        checked = checked,
        onCheckedChange = { onToggle() },
        text = { Text(label) },
        shapes = MenuDefaults.itemShape(index, count),
        colors = selectableMenuItemColors(),
        checkedLeadingIcon = {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(MenuDefaults.LeadingIconSize))
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        PostComposeToolbar(MockPostComposeViewModel(), isUploading = false, onAddPhoto = {}, onAttachFile = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EditingPreview() {
    val viewModel = MockPostComposeViewModel().apply { editor.isEditing = true }
    Theme {
        PostComposeToolbar(viewModel, isUploading = false, onAddPhoto = {}, onAttachFile = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FormattingPreview() {
    val viewModel = MockPostComposeViewModel().apply {
        editor.isEditing = true
        editor.toggleFormatting()
    }
    Theme {
        PostComposeToolbar(viewModel, isUploading = false, onAddPhoto = {}, onAttachFile = {})
    }
}
