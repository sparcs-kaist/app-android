package org.sparcs.soap.app.features.settings.ara

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.toggle
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockAraSettingsViewModel
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AraSettingsView(
    viewModel: AraSettingsViewModelProtocol = hiltViewModel<AraSettingsViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    var showNicknameDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchUser()
    }

    LaunchedEffect(viewModel.allowNSFW, viewModel.allowPolitical) {
        viewModel.updateContentPreference()
    }

    val topBarScrollBehavior = topAppBarScrollBehavior()

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.ara_settings),
                onDismiss = { navController.popBackStack() },
                scrollBehavior = topBarScrollBehavior,
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Ara Settings")
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                when (state) {
                    is AraSettingsViewModel.ViewState.Loading -> item { LoadingView() }
                    is AraSettingsViewModel.ViewState.Loaded -> item {
                        LoadedView(
                            viewModel,
                            navController
                        ) { showNicknameDialog = true }
                    }

                    is AraSettingsViewModel.ViewState.Error -> {
                        item {
                            val errorState = state as AraSettingsViewModel.ViewState.Error
                            ErrorView(
                                error = errorState.error,
                                defaultMessageResId = errorState.resId,
                                onRetry = { scope.launch { viewModel.fetchUser() } }
                            )
                        }
                    }
                }
            }
        }

        if (showNicknameDialog) {
            AlertDialog(
                onDismissRequest = { showNicknameDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showNicknameDialog = false
                        viewModel.updateNickname()
                    }) { Text(stringResource(R.string.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showNicknameDialog = false
                    }) { Text(stringResource(R.string.cancel)) }
                },
                title = { Text(stringResource(R.string.warning)) },
                text = {
                    Text(
                        stringResource(
                            R.string.nickname_change_confirmation,
                            viewModel.nickname
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Column {
        SettingsSectionHeader(stringResource(R.string.profile))
        OutlinedTextField(
            value = stringResource(R.string.unknown),
            onValueChange = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        SettingsSectionHeader(stringResource(R.string.content_preferences))
        SettingsSwitchItem(
            text = stringResource(R.string.allow_nsfw),
            checked = true,
            onCheckedChange = {},
            enabled = false
        )
        SettingsSwitchItem(
            text = stringResource(R.string.allow_political),
            checked = true,
            onCheckedChange = {},
            enabled = false
        )
    }
}

@Composable
private fun LoadedView(
    viewModel: AraSettingsViewModelProtocol,
    navController: NavController,
    onNicknameClick: () -> Unit,
) {
    val nicknameUpdatable = viewModel.nicknameUpdatable
    val nicknameUpdatableFrom = viewModel.nicknameUpdatableFrom
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val haptic = LocalHapticFeedback.current

    Column {
        SettingsSectionHeader(stringResource(R.string.profile))

        OutlinedTextField(
            value = viewModel.nickname,
            onValueChange = { viewModel.nickname = it },
            enabled = nicknameUpdatable,
            label = { Text(stringResource(R.string.enter_nickname)) },
            supportingText = {
                Column {
                    if (!nicknameUpdatable && nicknameUpdatableFrom != null) {
                        Text(
                            stringResource(
                                R.string.you_cant_change_nickname_until,
                                formatter.format(nicknameUpdatableFrom)
                            )
                        )
                    }
                    Text(stringResource(R.string.nicknames_can_only_be_changed_every_3_months))
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onNicknameClick() }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        SettingsSectionHeader(stringResource(R.string.content_preferences))

        SettingsSwitchItem(
            text = stringResource(R.string.allow_nsfw),
            checked = viewModel.allowNSFW,
            onCheckedChange = {
                haptic.toggle(it)
                viewModel.allowNSFW = it
            }
        )

        SettingsSwitchItem(
            text = stringResource(R.string.allow_political),
            checked = viewModel.allowPolitical,
            onCheckedChange = {
                haptic.toggle(it)
                viewModel.allowPolitical = it
            }
        )

        SettingsSectionHeader(stringResource(R.string.posts))

        SettingsNavigationItem(
            text = stringResource(R.string.my_posts),
            onClick = {
                val json = Uri.encode(Gson().toJson(AraMyPostViewModel.PostType.ALL))
                navController.navigate(Channel.AraMyPostSettings.name + "?type_json=$json")
            },
            leadingContent = {
                Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = null)
            }
        )

        SettingsNavigationItem(
            text = stringResource(R.string.bookmarked_posts),
            onClick = {
                val json = Uri.encode(Gson().toJson(AraMyPostViewModel.PostType.BOOKMARK))
                navController.navigate(Channel.AraMyPostSettings.name + "?type_json=$json")
            },
            leadingContent = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        AraSettingsView(
            MockAraSettingsViewModel(AraSettingsViewModel.ViewState.Loaded),
            rememberNavController()
        )
    }
}
