@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.soap.Features.PostList

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostCompose.Components.getLocalizedFlair
import com.example.soap.Features.PostList.Components.PostListRow.BoardNavigationBar
import com.example.soap.Features.PostList.Components.PostListRow.PostListRow
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.lightGray0

@Composable
fun PostListView(
    postListViewModel: PostListViewModel = viewModel(),
    navController: NavController
) {
    var selectedFlair by remember { mutableStateOf("All") }

    val filteredPosts = remember(postListViewModel.postList) {
        postListViewModel.postList
    }

    Scaffold(
        topBar = { BoardNavigationBar(navController = navController) },

        floatingActionButton = {
            ComposeButton(onClick = { navController.navigate(Channel.PostCompose.name) })
        },

        floatingActionButtonPosition = FabPosition.End

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    PostListRow(post, navController)
                    HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0)
                }
            }
        }
    }
}


@Composable
private fun ComposeButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 15.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_edit),
            contentDescription = "Write Button",
            tint = MaterialTheme.colorScheme.surface
        )
    }
}



@Composable
fun selectedColor(
    isSelected : Boolean
): Pair<Color, Color> {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainer
    val textColor =  if (isSelected) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.onSurface

    return Pair(backgroundColor, textColor)
}

@Composable
@Preview
private fun Preview(){
    Theme { PostListView(viewModel(), rememberNavController()) }

}