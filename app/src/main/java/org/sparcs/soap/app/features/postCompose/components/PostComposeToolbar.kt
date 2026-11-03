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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.window.core.layout.WindowWidthSizeClass
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalLayoutApi::class)
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
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        AnimatedVisibility(
            visible = editor.isFormatting && !isExpanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            Column {
                ComposeFormatPanel(editor, Modifier.fillMaxWidth())
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            AnimatedVisibility(
                visible = editor.isEditing,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FormatButton(editor, showsPopup = isExpanded)
                    BulletListToggle(editor)
                    Spacer(Modifier.width(8.dp))
                }
            }
            IconButton(onClick = onAddPhoto, enabled = !isUploading) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = stringResource(R.string.add_photo))
            }
            IconButton(onClick = onAttachFile, enabled = !isUploading) {
                Icon(Icons.Outlined.AttachFile, contentDescription = stringResource(R.string.attach_file))
            }
            PostOptionsMenu(viewModel)
            Spacer(Modifier.weight(1f))
            if (editor.isEditing || isKeyboardVisible) {
                IconButton(
                    onClick = {
                        keyboard?.hide()
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(Icons.Filled.KeyboardHide, contentDescription = stringResource(R.string.compose_hide_keyboard))
                }
            }
        }
    }
}

@Composable
private fun FormatButton(editor: ComposeEditorState, showsPopup: Boolean) {
    Box {
        IconToggleButton(
            checked = editor.isFormatting,
            onCheckedChange = { editor.toggleFormatting() },
            colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Icon(Icons.Outlined.TextFormat, contentDescription = stringResource(R.string.text_formatting))
        }
        DropdownMenu(
            expanded = showsPopup && editor.isFormatting,
            onDismissRequest = editor::closeFormatting,
            properties = PopupProperties(focusable = false, dismissOnClickOutside = false)
        ) {
            ComposeFormatPanel(editor)
        }
    }
}

@Composable
private fun PostOptionsMenu(viewModel: PostComposeViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.post_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (viewModel.canChooseAnonymity && viewModel.editingPost == null) {
                PostOptionItem(stringResource(R.string.post_option_anonymous), viewModel.writeAsAnonymous) {
                    viewModel.writeAsAnonymous = !viewModel.writeAsAnonymous
                }
            }
            PostOptionItem(stringResource(R.string.nsfw), viewModel.isNSFW) { viewModel.isNSFW = !viewModel.isNSFW }
            PostOptionItem(stringResource(R.string.political), viewModel.isPolitical) { viewModel.isPolitical = !viewModel.isPolitical }
        }
    }
}

@Composable
private fun PostOptionItem(label: String, checked: Boolean, onToggle: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        trailingIcon = { Checkbox(checked = checked, onCheckedChange = null) },
        onClick = onToggle,
        modifier = Modifier.semantics { role = Role.Checkbox; toggleableState = ToggleableState(checked) }
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
