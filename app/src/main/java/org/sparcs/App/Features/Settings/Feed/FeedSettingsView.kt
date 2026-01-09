package org.sparcs.App.Features.Settings.Feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import org.sparcs.App.Shared.ViewModelMocks.Feed.MockFeedSettingsViewModel
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.lightGray0
import org.sparcs.R

@Composable
fun FeedSettingsView(
    viewModel: FeedSettingsViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {

}

@Composable
private fun EditableProfileImage(
    imageUrl: String,
    onImageClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.lightGray0, CircleShape),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onImageClick,
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_photo_camera),
                contentDescription = "Edit",
                tint = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        FeedSettingsView(
            MockFeedSettingsViewModel(initialState = FeedSettingsViewModel.ViewState.Loaded),
            rememberNavController()
        )
    }
}