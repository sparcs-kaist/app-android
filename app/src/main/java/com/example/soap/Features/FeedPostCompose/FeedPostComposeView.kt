package com.example.soap.Features.FeedPostCompose
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.PickVisualMediaRequest
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.example.soap.Features.Feed.Components.ProfileImage
//import com.example.soap.R
//import com.example.soap.ui.theme.grayBB
//import kotlinx.coroutines.launch
//@Composable
//fun FeedPostComposeView(
//    viewModel: FeedPostComposeViewModelProtocol = hiltViewModel()
//) {
//    var showPhotosPicker by remember { mutableStateOf(false) }
//    var isUploading by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
//    ) { uri: List<Uri> ->
//        uri.let {
//            viewModel.selectedItems += it
//        }
//    }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        FeedPostHeader(viewModel, modifier = Modifier.padding(16.dp))
//        TextField(
//            value = viewModel.text,
//            onValueChange = { viewModel.text = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            placeholder = { Text("What's happening?") },
//            maxLines = 10
//        )
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            horizontalArrangement = Arrangement.End
//        ) {
//            Text("${viewModel.text.length}/280", style = MaterialTheme.typography.bodySmall)
//        }
//        if (viewModel.selectedImages.isNotEmpty()) {
//            LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
//                items(viewModel.selectedImages) { uri ->
//                    AsyncImage(
//                        model = uri,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(80.dp)
//                            .padding(end = 8.dp)
//                            .clip(RoundedCornerShape(8.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//                }
//            }
//        }
//        Spacer(modifier = Modifier.weight(1f))
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Button(onClick = { showPhotosPicker = true }) {
//                Text("Photo Library")
//            }
//            Button(
//                onClick = {
//                    coroutineScope.launch {
//                        isUploading = true
//                        viewModel.writePost()
//                        isUploading = false
//                    }
//                },
//                enabled = viewModel.text.isNotEmpty() && viewModel.text.length <= 280
//            ) {
//                Text("Done")
//            }
//        }
//    }
//    if (showPhotosPicker) {
//        PhotoPicker(selectedUris = viewModel.selectedImages) { uris ->
//            viewModel.selectedImages.clear()
//            viewModel.selectedImages.addAll(uris)
//            showPhotosPicker = false
//        }
//    }
//}
//
//@Composable
//fun FeedPostHeader(
//    viewModel: FeedPostComposeViewModel,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = modifier.fillMaxWidth()
//    ) {
//        ProfileImage(viewModel)
//        Spacer(modifier = Modifier.width(8.dp))
//        ComposeTypePicker(viewModel.selectedComposeType) {
//            viewModel.selectedComposeType = it
//        }
//        Spacer(modifier = Modifier.weight(1f))
//    }
//}
//
//@Composable
//fun ProfileImage(viewModel: FeedPostComposeViewModel) {
//    val placeholder = painterResource(R.drawable.add_photo_alternate)
//    val imageUrl = viewModel.feedUser?.profileImageURL
//
//    if (viewModel.selectedComposeType == FeedPostComposeViewModel.ComposeType.PUBLICLY && imageUrl != null) {
//        AsyncImage(
//            model = imageUrl,
//            contentDescription = null,
//            modifier = Modifier
//                .size(32.dp)
//                .clip(CircleShape),
//            contentScale = ContentScale.Crop,
//            placeholder = placeholder
//        )
//    } else {
//        Box(
//            modifier = Modifier
//                .size(32.dp)
//                .clip(CircleShape)
//                .background(MaterialTheme.colorScheme.grayBB),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("😀", fontSize = 14.sp)
//        }
//    }
//}
//
//@Composable
//fun ComposeTypePicker(
//    selectedType: FeedPostComposeViewModel.ComposeType,
//    onSelected: (FeedPostComposeViewModel.ComposeType) -> Unit
//) {
//    Row {
//        listOf(
//            FeedPostComposeViewModel.ComposeType.PUBLICLY to "Public",
//            FeedPostComposeViewModel.ComposeType.ANONYMOUSLY to "Anonymous"
//        ).forEach { (type, label) ->
//            Button(
//                onClick = { onSelected(type) },
//                colors = if (selectedType == type)
//                    ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
//                else
//                    ButtonDefaults.outlinedButtonColors()
//            ) {
//                Text(label)
//            }
//            Spacer(modifier = Modifier.width(4.dp))
//        }
//    }
//}
