package org.sparcs.soap.App.Features.Settings.Feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Features.Settings.Components.SettingsViewNavigationBar
import org.sparcs.soap.App.Features.Settings.Feed.ViewState.FeedProfileImageState
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.BuddyPreviewSupport.Feed.PreviewFeedSettingsViewModel
import org.sparcs.soap.R

@Composable
fun FeedSettingsView(
    viewModel: FeedSettingsViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUser()
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.feed_settings),
                onDismiss = { navController.popBackStack() }
            )
        },
        modifier = Modifier.analyticsScreen("Feed Settings")
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
                .then(if (viewModel.isUpdatingProfile) Modifier.alpha(0.5f) else Modifier)
        ) {
            when (state) {
                is FeedSettingsViewModel.ViewState.Loading -> LoadingView()
                is FeedSettingsViewModel.ViewState.Loaded -> LoadedView(
                    viewModel
                )

                is FeedSettingsViewModel.ViewState.Error -> {
                    val message = (state as FeedSettingsViewModel.ViewState.Error).message
                    ErrorView(
                        icon = Icons.Default.Warning,
                        message = message,
                        onRetry = { scope.launch { viewModel.fetchUser() } }
                    )
                }
            }
        }

        if (viewModel.isUpdatingProfile) {
            Box(
                modifier = Modifier.fillMaxSize().clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (viewModel.isAlertPresented) {
            AlertDialog(
                onDismissRequest = { viewModel.isAlertPresented = false },
                confirmButton = {
                    TextButton(onClick = { viewModel.isAlertPresented = false }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                title = {
                    viewModel.alertState?.titleResId?.let { Text(stringResource(it)) }
                },
                text = {
                    viewModel.alertState?.let { state ->
                        Text(
                            state.message ?: stringResource(
                                state.messageResId ?: R.string.unexpected_error
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Column {
        Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun LoadedView(
    viewModel: FeedSettingsViewModelProtocol = hiltViewModel(),
) {
    val userState by viewModel.user.collectAsState()
    val context = LocalContext.current
    val profileImageState by viewModel.profileImageState.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfileImage(it, context)
        }
    }

    Column {
        //profile
        Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (profileImageState) {
                    is FeedProfileImageState.NoChange -> {
                        AsyncImage(
                            model = userState?.profileImageURL,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    is FeedProfileImageState.Updated -> {
                        AsyncImage(
                            model = (profileImageState as FeedProfileImageState.Updated).image ?: userState?.profileImageURL,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> CircularProgressIndicator(modifier = Modifier.size(50.dp))
                }
            }
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.edit),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        launcher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                )
                Text(
                    text = stringResource(R.string.reset),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { viewModel.updateProfileImage(null, context) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.nickname,
            onValueChange = {
                viewModel.nickname = it
                viewModel.nicknameError = null
            },
            supportingText = {
                viewModel.nicknameError?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            label = {
                Text(
                    stringResource(R.string.enter_nickname),
                    color = MaterialTheme.colorScheme.grayBB
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.updateNickname()
                    keyboardController?.hide()
                }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.karma))
            Text(
                text = viewModel.karma.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
@Preview(name = "Loaded State", showBackground = true)
@Composable
private fun LoadedPreview() {
    Theme {
        val viewModel = PreviewFeedSettingsViewModel()
        FeedSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun LoadingPreview() {
    Theme {
        val viewModel = PreviewFeedSettingsViewModel(FeedSettingsViewModel.ViewState.Loading)
        FeedSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun ErrorPreview() {
    Theme {
        val viewModel = PreviewFeedSettingsViewModel(FeedSettingsViewModel.ViewState.Error("ERROR"))
        FeedSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(name = "Nickname Conflict State", showBackground = true)
@Composable
private fun NicknameConflictPreview() {
    Theme {
        val viewModel = PreviewFeedSettingsViewModel().apply {
            nickname = "DuplicateName"
            nicknameError = R.string.nickname_error_conflict
        }
        FeedSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(name = "Alert Dialog State", showBackground = true)
@Composable
private fun AlertPreview() {
    Theme {
        val viewModel = PreviewFeedSettingsViewModel().apply {
            alertState = AlertState(
                titleResId = R.string.error,
                message = "Failed to upload settings."
            )
            isAlertPresented = true
        }
        FeedSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}