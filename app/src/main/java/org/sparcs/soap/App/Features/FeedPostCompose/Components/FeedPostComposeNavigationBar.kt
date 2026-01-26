package org.sparcs.soap.App.Features.FeedPostCompose.Components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostComposeNavigationBar(
    navController: NavController,
    isDoneEnabled: Boolean,
    onDoneClick: () -> Unit,
    isUploading: Boolean,
) {

    CenterAlignedTopAppBar(
        navigationIcon = {
            DismissButton { navController.popBackStack() }
        },
        title = {
            Text(
                text = stringResource(R.string.write),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            DoneButton(
                isDoneEnabled = isDoneEnabled,
                onDoneClick = onDoneClick,
                isUploading = isUploading
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun DoneButton(
    isDoneEnabled: Boolean,
    onDoneClick: () -> Unit,
    isUploading: Boolean,
) {
    TextButton(
        onClick = {
            if (isDoneEnabled) {
                onDoneClick()
            }
        },
        enabled = isDoneEnabled,
        modifier = Modifier.semantics { contentDescription = "Post Button" }
    ) {
        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = stringResource(R.string.submit),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = if (isDoneEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.grayBB
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { FeedPostComposeNavigationBar(rememberNavController(), false, {}, false) }
}
