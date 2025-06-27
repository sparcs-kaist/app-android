package com.example.soap.Features.PostList

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soap.Features.NavigationBar.Components.selectedColor
import com.example.soap.Features.PostList.Components.PostListRow.PostListRow
import com.example.soap.ui.theme.SoapTheme


@Composable
fun PostListView(postListViewModel: PostListViewModel= viewModel()){
    val scrollState = rememberScrollState()
    var selectedFlair by remember { mutableStateOf("All") }

    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp)
                .fillMaxWidth()
        ) {
            Spacer(Modifier.padding(4.dp))

            Card(
                Modifier
                    .padding(4.dp)
                    .clickable { selectedFlair = "All" },
                colors = CardDefaults.cardColors(selectedColor(selectedFlair == "All").first)
            ) {
                Text(
                    text = "All",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(4.dp),
                    color = selectedColor(selectedFlair == "All").second
                )
            }
            postListViewModel.flairList.forEach { flair ->
                Card(
                    Modifier
                        .padding(4.dp)
                        .clickable { selectedFlair = flair },
                    colors = CardDefaults.cardColors(selectedColor(selectedFlair == flair).first)
                ) {
                    Text(
                        text = flair,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(4.dp),
                        color = selectedColor(selectedFlair == flair).second
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp)
            ) {
                items(postListViewModel.postList) { post ->
                    PostListRow(post)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}



@Composable
@Preview
private fun Preview(){
    SoapTheme { PostListView(viewModel()) }
}