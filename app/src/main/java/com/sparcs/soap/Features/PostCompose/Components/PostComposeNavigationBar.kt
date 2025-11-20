package com.sparcs.soap.Features.PostCompose.Components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.darkGray
import com.sparcs.soap.ui.theme.grayBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComposeNavigationBar(
    navController: NavController,
    isDoneEnabled: Boolean,
    onDoneClick: () -> Unit,
    isUploading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    expanded = true
                }) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.darkGray
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.discard_this_post),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        expanded = false
                        navController.popBackStack()
                    }
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
@Preview
private fun Preview(){
    Theme{ PostComposeNavigationBar(rememberNavController(), false, {}, false) }
}
