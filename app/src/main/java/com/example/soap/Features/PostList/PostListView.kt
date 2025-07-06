@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.soap.Features.PostList

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Components.BoardNavigationBar
import com.example.soap.Features.PostCompose.Components.getLocalizedFlair
import com.example.soap.Features.PostCompose.PostComposeView
import com.example.soap.Features.PostList.Components.PostListRow.PostListRow
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListView(
    postListViewModel: PostListViewModel = viewModel(),
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDialogView by remember { mutableStateOf(false) }
    var sheetVisible by remember { mutableStateOf(false) }
    var selectedFlair by remember { mutableStateOf("All") }

    val openSheet = {
        scope.launch {
            sheetState.show()
            sheetVisible = true
        }
    }

    val closeSheet = {
        scope.launch {
            sheetState.hide()
            sheetVisible = false
        }
    }

    val filteredPosts = remember(postListViewModel.postList) {
        postListViewModel.postList
    }

    Scaffold(
        topBar = { BoardNavigationBar(navController = navController) },
        floatingActionButton = {
            ComposeButton(onClick = { openSheet() })
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.soapColors.background)
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item {
                    val isSelected = (selectedFlair == "All")
                    val (bg, fg) = selectedColor(isSelected)

                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { selectedFlair = "All" },
                        colors = CardDefaults.cardColors(bg)
                    ) {
                        Text(
                            text = getLocalizedFlair("All"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = fg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                items(postListViewModel.flairList) { flair ->
                    val isSelected = (selectedFlair == flair)
                    val (bg, fg) = selectedColor(isSelected)

                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { selectedFlair = flair },
                        colors = CardDefaults.cardColors(bg)
                    ) {
                        Text(
                            text = getLocalizedFlair(flair),
                            style = MaterialTheme.typography.bodyLarge,
                            color = fg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.padding(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.soapColors.surface)
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    PostListRow(post, navController)
                    HorizontalDivider(color = MaterialTheme.soapColors.gray0Border)
                }
            }
        }
    }

    if (sheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                showDialogView = true
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.soapColors.surface
        ) {
            PostComposeView(viewModel())
        }
    }

    if (showDialogView) {
        ConfirmationDialog(
            onDismissRequest = {
                showDialogView = false
            },
            onConfirmationButtonRequest = {
                showDialogView = false
                closeSheet()
            },
            onDismissButtonRequest = {
                showDialogView = false
                scope.launch { sheetState.show() }
            }
        )
    }
}


@Composable
private fun ComposeButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(MaterialTheme.soapColors.primary),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 15.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_edit),
            contentDescription = "Write Button",
            tint = MaterialTheme.soapColors.surface
        )
    }
}

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmationButtonRequest: ()-> Unit,
    onDismissButtonRequest: ()-> Unit
){
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.discard_this_post), style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onConfirmationButtonRequest) { Text(stringResource(R.string.ok)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDismissButtonRequest) { Text(stringResource(R.string.save_in_drafts)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDismissButtonRequest) { Text(stringResource(R.string.cancel)) }
                }
            }
        }
    }

}


@Composable
fun selectedColor(
    isSelected : Boolean
): Pair<Color, Color> {
    val backgroundColor = if (isSelected) MaterialTheme.soapColors.onSurface else MaterialTheme.soapColors.primaryContainer
    val textColor =  if (isSelected) MaterialTheme.soapColors.primaryContainer else MaterialTheme.colorScheme.onSurface

    return Pair(backgroundColor, textColor)
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { PostListView(viewModel(), rememberNavController()) }

}