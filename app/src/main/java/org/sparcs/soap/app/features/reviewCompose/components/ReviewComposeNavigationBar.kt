package org.sparcs.soap.app.features.reviewCompose.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.components.CloseButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewComposeNavigationBar(
    navController: NavController,
    onDoneClick: () -> Unit,
    isDoneEnabled: Boolean,
    isUploading: Boolean,
    isEditing: Boolean,
){

    TopAppBar(
        title = {
            Text(text = stringResource(if (isEditing) R.string.edit else R.string.write_a_review))
        },
        actions = {
            DoneButton(
                isDoneEnabled = isDoneEnabled,
                onDoneClick = onDoneClick,
                isUploading = isUploading
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        navigationIcon = {
            CloseButton { navController.popBackStack() }
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
            Text(text = stringResource(R.string.submit))
        }
    }
}
