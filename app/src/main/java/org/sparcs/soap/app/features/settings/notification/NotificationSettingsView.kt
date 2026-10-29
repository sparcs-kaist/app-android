package org.sparcs.soap.app.features.settings.notification

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.NotificationCategory
import org.sparcs.soap.app.domain.usecases.MockFCMUseCase
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsView(
    navController: NavController,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Notifications may have been allowed in Android's settings while Buddy was in the background.
    LifecycleResumeEffect(Unit) {
        viewModel.updateSystemPermission(NotificationManagerCompat.from(context).areNotificationsEnabled())
        viewModel.load()
        onPauseOrDispose { }
    }

    val topBarScrollBehavior = topAppBarScrollBehavior()

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.notifications_title),
                onDismiss = { navController.popBackStack() },
                scrollBehavior = topBarScrollBehavior,
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Notification Settings")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val current = state) {
                is NotificationSettingsViewModel.ViewState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is NotificationSettingsViewModel.ViewState.Error -> {
                    ErrorView(error = current.error, onRetry = viewModel::load)
                }

                is NotificationSettingsViewModel.ViewState.Loaded -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 600.dp)
                    ) {
                        if (!viewModel.isAllowedBySystem) {
                            item {
                                NotificationsBlockedNotice(
                                    onTurnOn = {
                                        context.startActivity(
                                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        )
                                    }
                                )
                            }
                        }

                        item { SettingsSectionHeader(stringResource(R.string.feed)) }
                        item { PreferenceSwitch(viewModel, NotificationCategory.FEED_COMMENT, R.string.notification_settings_feed_comments) }
                        item { PreferenceSwitch(viewModel, NotificationCategory.FEED_REPLY, R.string.notification_settings_feed_replies) }

                        item { SettingsSectionHeader(stringResource(R.string.taxi)) }
                        item { PreferenceSwitch(viewModel, NotificationCategory.TAXI_CHAT, R.string.notification_settings_taxi_chats) }
                        item {
                            PreferenceSwitch(
                                viewModel,
                                NotificationCategory.TAXI_EVENT,
                                R.string.notification_settings_ride_updates,
                                supportingText = stringResource(R.string.notification_settings_ride_updates_description)
                            )
                        }

                        item { SettingsSectionHeader(stringResource(R.string.notification_settings_other_services)) }
                        item { PreferenceSwitch(viewModel, NotificationCategory.ARA, R.string.ara) }
                        item { PreferenceSwitch(viewModel, NotificationCategory.TIMETABLE, R.string.timetable) }

                        item {
                            Text(
                                text = stringResource(R.string.notification_settings_this_device_only),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = viewModel::dismissAlert,
    )
}

/** Says why the switches below are off, with a button to where notifications can be turned on. */
@Composable
private fun NotificationsBlockedNotice(onTurnOn: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.NotificationsOff, contentDescription = null, modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.notification_settings_blocked_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.notification_settings_blocked_by_system),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onTurnOn, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.notification_settings_turn_on_in_settings))
                }
            }
        }
    }
}

@Composable
private fun PreferenceSwitch(
    viewModel: NotificationSettingsViewModel,
    category: NotificationCategory,
    titleResId: Int,
    supportingText: String? = null,
) {
    SettingsSwitchItem(
        text = stringResource(titleResId),
        checked = viewModel.preferences[category] ?: true,
        onCheckedChange = { enabled -> viewModel.setNotifications(category, enabled) },
        // None of these arrive while Android blocks Buddy's notifications, which is changed in
        // the system settings, through the row above.
        enabled = viewModel.isAllowedBySystem,
        supportingText = supportingText,
    )
}

@Preview
@Composable
private fun Preview() {
    Theme {
        val mockViewModel = remember {
            NotificationSettingsViewModel(fcmUseCase = MockFCMUseCase())
        }
        LaunchedEffect(Unit) { mockViewModel.load() }
        NotificationSettingsView(rememberNavController(), mockViewModel)
    }
}
