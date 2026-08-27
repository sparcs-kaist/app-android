package org.sparcs.soap.app.features.settings.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.FeatureType
import org.sparcs.soap.app.domain.usecases.MockFCMUseCase
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.landscapeHideOnScrollBehavior
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsView(
    navController: NavController,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val isAlertPresented by viewModel.isAlertPresented.collectAsState()
    val alertTitle by viewModel.alertTitle.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    val topBarScrollBehavior = landscapeHideOnScrollBehavior()

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.notifications_title),
                onDismiss = { navController.popBackStack() },
                scrollBehavior = topBarScrollBehavior
            )
        },
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Notification Settings")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.services),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(FeatureType.entries) { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(type.iconRes), null, tint = Color.Unspecified)

                    Spacer(Modifier.width(8.dp))

                    Text(stringResource(type.prettyStringRes))

                    Spacer(Modifier.weight(1f))

                    Switch(
                        checked = viewModel.toggleState[type] ?: true,
                        onCheckedChange = { isActive ->
                            viewModel.toggle(type, isActive)
                        }
                    )
                }
            }
        }
    }

    if (isAlertPresented) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text(alertTitle) },
            text = { Text(alertMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAlert() }) {
                    Text(stringResource(R.string.ok))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        val context = LocalContext.current

        val mockViewModel = remember {
            NotificationSettingsViewModel(
                context = context,
                fcmUseCase = MockFCMUseCase()
            ).apply {
                loadSettings()
            }
        }

        NotificationSettingsView(rememberNavController(), mockViewModel)
    }
}