package com.example.soap.Features.PostList

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Components.BoardNavigationBar
import com.example.soap.Features.NavigationBar.Components.selectedColor
import com.example.soap.Features.PostList.Components.PostListRow.PostListRow
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme

@Composable
fun PostListView(
    postListViewModel: PostListViewModel = viewModel(),
    navController: NavController
) {

    var selectedFlair by remember { mutableStateOf("All") }
    var showComposeView by remember { mutableStateOf(false) }
    val filteredPosts  = remember(selectedFlair, postListViewModel.postList) {
        if (selectedFlair == "All") postListViewModel.postList
        else {postListViewModel.postList}
//        else postListViewModel.postList.filter { it.flair == selectedFlair }
    }

    Scaffold(
        topBar = { BoardNavigationBar(navController = navController) },
        bottomBar = {},
        floatingActionButton = {
            ComposeButton( onClick = { showComposeView = true } )
                               },
        floatingActionButtonPosition = FabPosition.Center
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
                    val isSelected = selectedFlair == "All"
                    val (bg, fg) = selectedColor(isSelected)

                    Card(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { selectedFlair = "All" },
                        colors = CardDefaults.cardColors(bg)
                    ) {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.bodyLarge,
                            color = fg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                items(postListViewModel.flairList) { flair ->
                    val isSelected = selectedFlair == flair
                    val (bg, fg) = selectedColor(isSelected)

                    Card(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { selectedFlair = flair },
                        colors = CardDefaults.cardColors(bg)
                    ) {
                        Text(
                            text = flair,
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
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    PostListRow(post)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun ComposeButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(R.drawable.outline_edit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.padding(4.dp))

            Text(
                text = "Write",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}


@Composable
@Preview
private fun Preview(){
    SoapTheme { PostListView(viewModel(), rememberNavController()) }
}