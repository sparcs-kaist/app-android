package org.sparcs.soap.app.features.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.R
import org.sparcs.soap.app.ChannelManager
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.settings.components.ServiceLogo
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.extensions.toggle
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipStore
import org.sparcs.soap.app.shared.viewModelMocks.MockSettingsViewModel
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavController,
    settingsViewModel: SettingsViewModelProtocol = hiltViewModel<SettingsViewModel>(),
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val isPreview = LocalInspectionMode.current
    val haptic = LocalHapticFeedback.current

    var isCrashlyticsEnabled by remember {
        mutableStateOf(if (!isPreview) FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled else false)
    }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isCrashlyticsEnabled) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = isCrashlyticsEnabled
    }

    val topBarScrollBehavior = topAppBarScrollBehavior()

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.settings),
                onDismiss = { navController.popBackStack() },
                scrollBehavior = topBarScrollBehavior,
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Settings")
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
                item { SettingsSectionHeader(stringResource(R.string.widget_miscellaneous)) }
                item { LanguageItem(context) }
                item {
                    NavigationItem(
                        text = stringResource(R.string.notifications_title),
                        icon = Icons.Outlined.Notifications
                    ) { navController.navigate(Channel.NotificationSettings.name) }
                }
                item { ThemeSwitcherItem(settingsViewModel) }
                item {
                    SendCrashReportsItem(isCrashlyticsEnabled) {
                        haptic.toggle(it)
                        isCrashlyticsEnabled = it
                    }
                }
                item {
                    NavigationItem(
                        text = stringResource(R.string.chat_with_us),
                        icon = Icons.Outlined.Feedback
                    ) { activity?.let { ChannelManager.showMessenger(it) } }
                }

                item { SettingsSectionHeader(stringResource(R.string.services)) }
                item {
                    ServiceItem(stringResource(R.string.feed), R.drawable.sparcs_logo) {
                        navController.navigate(Channel.FeedSettings.name)
                    }
                }
                item {
                    ServiceItem(stringResource(R.string.ara), R.drawable.ara_logo) {
                        navController.navigate(Channel.AraSettings.name)
                    }
                }
                item {
                    ServiceItem(stringResource(R.string.otl), R.drawable.otl_logo) {
                        navController.navigate(Channel.TimetableSettings.name)
                    }
                }
                item {
                    ServiceItem(stringResource(R.string.taxi), R.drawable.taxi_logo) {
                        navController.navigate(Channel.TaxiSettings.name)
                    }
                }

                item { SettingsSectionHeader(stringResource(R.string.screen_style)) }
                item {
                    NavigationItem(
                        text = stringResource(R.string.timetable_theme),
                        icon = Icons.Outlined.Palette
                    ) { navController.navigate(Channel.TimetableThemeSettings.name) }
                }

                item { SettingsSectionHeader(stringResource(R.string.information)) }
                item {
                    NavigationItem(
                        text = stringResource(R.string.privacy_policy),
                        icon = Icons.Outlined.Policy
                    ) { context.startActivity(Intent(Intent.ACTION_VIEW, Constants.PRIVACY_POLICY_URL.toUri())) }
                }
                item {
                    NavigationItem(
                        text = stringResource(R.string.terms_of_use),
                        icon = Icons.Outlined.Description
                    ) { context.startActivity(Intent(Intent.ACTION_VIEW, Constants.TERMS_OF_USE_URL.toUri())) }
                }
                item {
                    NavigationItem(
                        text = stringResource(R.string.acknowledgements),
                        icon = Icons.Outlined.VolunteerActivism
                    ) { navController.navigate(Channel.CreditView.name) }
                }
                item { VersionItem() }

                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    SignOutItem { showSignOutDialog = true }
                }

                if (BuildConfig.DEBUG) {
                    item { SettingsSectionHeader("Debug Menu") }
                    item {
                        NavigationItem(text = "Force Crash", icon = Icons.Outlined.BugReport) {
                            error("DEBUG: User forced a crash")
                        }
                    }
                    item {
                        NavigationItem(text = "Invoke Exception", icon = Icons.Outlined.BugReport) {
                            settingsViewModel.handleException(Exception("Test exception from debug"))
                        }
                    }
                    item { LocalTipStore.current?.let { TipsDebugSection(it) } }
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.sign_out_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        settingsViewModel.signOut()
                    }
                ) { Text(stringResource(R.string.sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    GlobalAlertDialog(
        state = settingsViewModel.alertState,
        isPresented = settingsViewModel.isAlertPresented,
    ) {
        settingsViewModel.isAlertPresented = false
    }
}

@Composable
private fun TipsDebugSection(store: TipStore) {
    var isResetPending by remember { mutableStateOf(store.isResetPending) }
    var showsImmediately by remember { mutableStateOf(store.showsTipsImmediately) }
    Column {
        SettingsSectionHeader(stringResource(R.string.tips_debug_title))
        ListItem(
            headlineContent = { Text(stringResource(R.string.tips_debug_reset)) },
            supportingContent = if (isResetPending) {
                { Text(stringResource(R.string.tips_debug_reset_pending)) }
            } else null,
            modifier = Modifier.clickable(enabled = !isResetPending) {
                store.requestReset()
                isResetPending = true
            }
        )
        SettingsSwitchItem(
            text = stringResource(R.string.tips_debug_show_immediately),
            checked = showsImmediately,
            onCheckedChange = {
                store.showsTipsImmediately = it
                showsImmediately = it
            },
            supportingText = stringResource(R.string.tips_debug_footer)
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun NavigationItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    SettingsNavigationItem(
        text = text,
        onClick = onClick,
        leadingContent = { Icon(icon, contentDescription = null) }
    )
}

@Composable
private fun ServiceItem(
    text: String,
    @DrawableRes logo: Int,
    onClick: () -> Unit,
) {
    SettingsNavigationItem(text = text, onClick = onClick, leadingContent = { ServiceLogo(logo) })
}

@Composable
private fun LanguageItem(context: Context) {
    val languageDisplayName = context.resources.configuration.locales[0].displayLanguage
    val onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                // Android 13 이상: 앱 언어 변경 화면
                val action =
                    Settings::class.java.getField("ACTION_APP_LOCALE_SETTINGS").get(null) as String
                val intent = Intent(action).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } else {
            // Android 12 이하: 시스템 언어 변경 화면
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.change_language)) },
        supportingContent = { Text(languageDisplayName) },
        leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun SendCrashReportsItem(
    isCrashlyticsEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsSwitchItem(
        text = stringResource(R.string.send_crash_reports),
        checked = isCrashlyticsEnabled,
        onCheckedChange = onCheckedChange,
        leadingContent = { Icon(Icons.Outlined.Lightbulb, contentDescription = null) }
    )
}

@Composable
private fun SignOutItem(onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.sign_out)) },
        leadingContent = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
        colors = ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.error,
            leadingIconColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun ThemeSwitcherItem(
    settingsViewModel: SettingsViewModelProtocol,
) {
    var showDialog by remember { mutableStateOf(false) }
    val darkMode by settingsViewModel.darkModeSetting.collectAsState(initial = null)
    // (theme key, selected, label)
    val options = listOf(
        Triple("system", darkMode == null, stringResource(R.string.widget_system_default)),
        Triple("light", darkMode == false, stringResource(R.string.widget_white_mode)),
        Triple("dark", darkMode == true, stringResource(R.string.widget_dark_mode))
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.widget_dark_mode)) },
        supportingContent = { Text(options.first { it.second }.third) },
        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.widget_dark_mode)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    options.forEach { (key, selected, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        settingsViewModel.setTheme(key)
                                        showDialog = false
                                    }
                                )
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Text(text = label, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun VersionItem() {
    val context = LocalContext.current
    val versionName = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.0"
    } catch (_: Exception) {
        "0.0"
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.app_version)) },
        supportingContent = { Text(versionName) },
        leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) }
    )
}

@Composable
@Preview
private fun Preview() {
    Theme {
        SettingsView(rememberNavController(), MockSettingsViewModel())
    }
}

@Composable
@Preview(showBackground = true)
private fun TipsDebugSectionPreview() {
    val context = LocalContext.current
    Theme {
        TipsDebugSection(remember(context) { TipStore(context) })
    }
}
