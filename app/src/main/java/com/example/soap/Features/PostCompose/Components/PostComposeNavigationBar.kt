package com.example.soap.Features.PostCompose.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.darkGray
import com.example.soap.ui.theme.grayBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComposeNavigationBar(
    navController: NavController,
    isDoneEnabled: Boolean,
    isBackEnabled: Boolean,
    onDoneClick: () -> Unit,
    isUploading: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDialog = false },
            onConfirmationButtonRequest = {
                showDialog = false
                navController.navigate(Channel.TrendingBoard.name)
            },
            onSaveDraftRequest = {
                showDialog = false
                //임시 저장
            }
        )
    }

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    if(!isBackEnabled){
                        showDialog = true
                    } else {
                        navController.navigate(Channel.TrendingBoard.name)
                    }
                }) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.darkGray
                )
            }
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
            ) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
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
        modifier = Modifier.semantics { contentDescription = "Post Button" }
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

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmationButtonRequest: () -> Unit,
    onSaveDraftRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.discard_this_post)) },

        confirmButton = {
            TextButton(onClick = onConfirmationButtonRequest) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = onSaveDraftRequest) {
                    Text(stringResource(R.string.save_in_drafts))
                }
            }
        }
    )
}

@Composable
@Preview
private fun Preview(){
    Theme{ PostComposeNavigationBar(rememberNavController(), false, true, {}, false) }
}
