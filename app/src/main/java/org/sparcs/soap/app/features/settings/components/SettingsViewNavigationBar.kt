package org.sparcs.soap.app.features.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.DismissButton
import org.sparcs.soap.app.features.navigationBar.components.SearchButton
import org.sparcs.soap.app.theme.ui.grayBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsViewNavigationBar(
    title: String,
    onDismiss: () -> Unit,
    isSearchEnabled: Boolean? = false,
    onClickSearch: () -> Unit = {},
    isSelected: Boolean = false,
    isEditable: Boolean? = false,
    isDoneEnabled: Boolean? = false,
    isSaving: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.background,
    onClickDone: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = { onDismiss() }) },
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = containerColor
        ),
        actions = {
            if (isSearchEnabled == true) {
                SearchButton(
                    onClick = { onClickSearch() },
                    isSelected = isSelected
                )
            } else if (isEditable == true) {
                DoneButton(
                    onDoneClick = { onClickDone() },
                    isDoneEnabled = isDoneEnabled ?: false,
                    isSaving = isSaving
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}


@Composable
private fun DoneButton(
    isDoneEnabled: Boolean,
    isSaving: Boolean,
    onDoneClick: () -> Unit,
) {
    TextButton(
        onClick = {
            onDoneClick()
        },
        enabled = isDoneEnabled,
        modifier = Modifier.semantics { contentDescription = "Setting Button" }
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = stringResource(R.string.done),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = if (isDoneEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.grayBB
            )
        }
    }
}
