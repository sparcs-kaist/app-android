package org.sparcs.soap.app.features.postCompose.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.CloseButton
import org.sparcs.soap.app.features.navigationBar.components.TopBarTextAction
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComposeNavigationBar(
    onClose: () -> Unit,
    isDoneEnabled: Boolean,
    onDoneClick: () -> Unit,
    isUploading: Boolean,
    isEditing: Boolean = false,
) {
    TopAppBar(
        navigationIcon = { CloseButton(onClose) },
        title = { Text(text = stringResource(if (isEditing) R.string.edit else R.string.write)) },
        actions = {
            TopBarTextAction(
                text = stringResource(R.string.submit),
                onClick = onDoneClick,
                enabled = isDoneEnabled,
                isBusy = isUploading
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { PostComposeNavigationBar(onClose = {}, isDoneEnabled = false, onDoneClick = {}, isUploading = false) }
}
