package org.sparcs.soap.app.features.settings.notification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.FeatureType
import org.sparcs.soap.app.domain.usecases.MockFCMUseCase
import org.sparcs.soap.app.features.settings.components.ServiceLogo
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsView(
    navController: NavController,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { SettingsSectionHeader(stringResource(R.string.services)) }

            items(FeatureType.entries) { type ->
                SettingsSwitchItem(
                    text = stringResource(type.prettyStringRes),
                    checked = viewModel.toggleState[type] ?: true,
                    onCheckedChange = { isActive -> viewModel.toggle(type, isActive) },
                    leadingContent = { ServiceLogo(type.iconRes) }
                )
            }
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = viewModel::dismissAlert,
    )
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