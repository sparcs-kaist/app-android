package org.sparcs.App.Features.ReviewCompose.Components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.sparcs.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewComposeNavigationBar(
    navController: NavController,
    onDoneClick: () -> Unit,
    isDoneEnabled: Boolean,
    isUploading: Boolean
){
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.write_a_review),
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
        ),
        navigationIcon = {
            DismissButton { navController.popBackStack() }
        }
    )
}


@Composable
private fun DoneButton(
    isDoneEnabled: Boolean,
    onDoneClick: () -> Unit,
    isUploading: Boolean
){
    TextButton(
        onClick = {
            if (isDoneEnabled) {
                onDoneClick()
            }
        },
        enabled = isDoneEnabled,
        modifier = Modifier.semantics { contentDescription = "Post Review Button" }
    ){
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