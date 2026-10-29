package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.theme.ui.Theme

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
    var showsOptions by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ComposeFormatBar(editor)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onAddPhoto, enabled = !isUploading) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = stringResource(R.string.add_photo))
            }
            IconButton(onClick = onAttachFile, enabled = !isUploading) {
                Icon(Icons.Outlined.AttachFile, contentDescription = stringResource(R.string.attach_file))
            }
            Box {
                IconButton(onClick = { showsOptions = true }) {
                    Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.post_options))
                }
                DropdownMenu(expanded = showsOptions, onDismissRequest = { showsOptions = false }) {
                    if (viewModel.canChooseAnonymity && viewModel.editingPost == null) {
                        PostOptionItem(stringResource(R.string.post_option_anonymous), viewModel.writeAsAnonymous) {
                            viewModel.writeAsAnonymous = !viewModel.writeAsAnonymous
                        }
                    }
                    PostOptionItem(stringResource(R.string.nsfw), viewModel.isNSFW) { viewModel.isNSFW = !viewModel.isNSFW }
                    PostOptionItem(stringResource(R.string.political), viewModel.isPolitical) { viewModel.isPolitical = !viewModel.isPolitical }
                }
            }
            Spacer(Modifier.weight(1f))
            if (editor.isEditing) {
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
