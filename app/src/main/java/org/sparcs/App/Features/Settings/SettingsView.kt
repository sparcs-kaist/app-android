package org.sparcs.App.Features.Settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.sparcs.App.Domain.Helpers.Constants
import org.sparcs.App.Features.NavigationBar.Channel
import org.sparcs.App.Features.Settings.Components.SettingsViewNavigationBar
import org.sparcs.App.Shared.Extensions.toggle
import org.sparcs.App.Shared.ViewModelMocks.MockSettingsViewModel
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.BuildConfig
import org.sparcs.R

@Composable
fun SettingsView(
    navController: NavHostController,
    settingsViewModel: SettingsViewModelProtocol = hiltViewModel(),
) {
    val context = LocalContext.current
    var showLogoutError by remember { mutableStateOf(false) }
    val isPreview = LocalInspectionMode.current
    val haptic = LocalHapticFeedback.current

    var isCrashlyticsEnabled by remember {
        mutableStateOf(if (!isPreview) FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled else false)
    }

    LaunchedEffect(isCrashlyticsEnabled) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = isCrashlyticsEnabled
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.settings),
                onDismiss = { navController.popBackStack() }
            )
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            item {
                Text(
                    text = stringResource(R.string.miscellaneous),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )
                AppSettings(context)
                ThemeSwitcherButton(settingsViewModel)
                FeedbackButton(context)
                SendCrashReportsButton(isCrashlyticsEnabled) {
                    haptic.toggle(it)
                    isCrashlyticsEnabled = it
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.services),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )

                ServiceNavButton(
                    stringResource(R.string.feed),
                    painterResource(R.drawable.sparcs_logo)
                ) { navController.navigate(Channel.FeedSettings.name) }

                ServiceNavButton(
                    stringResource(R.string.ara),
                    painterResource(R.drawable.ara_logo)
                ) { navController.navigate(Channel.AraSettings.name) }

                ServiceNavButton(
                    stringResource(R.string.taxi),
                    painterResource(R.drawable.taxi_logo)
                ) { navController.navigate(Channel.TaxiSettings.name) }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )

                Term(
                    context = context,
                    navController = navController
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.sign_out),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )
                SignOutButton {
                    try {
                        settingsViewModel.signOut()
                    } catch (e: Exception) {
                        showLogoutError = true
                    } finally {
                        navController.navigate(Channel.SignOut.name)
                    }
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Text("Debug Menu", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Button(onClick = { error("DEBUG: User forced a crash") }) {
                Text("Force Crash")
            }

            Button(
                onClick = {
                    settingsViewModel.handleException(
                        Exception("Test exception from debug")
                    )
                }
            ) {
                Text("Invoke Exception")
            }
        }
    }

    if (showLogoutError) {
        AlertDialog(
            onDismissRequest = { showLogoutError = false },
            confirmButton = {
                TextButton(onClick = { showLogoutError = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.error)) },
            text = {
                Text(stringResource(R.string.unexpected_error_signing_out))
            }
        )
    }
}

@Composable
private fun AppSettings(context: Context) {
    val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale
    }

    val languageDisplayName = currentLocale.displayLanguage
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
            } catch (e: Exception) {
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_language),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.change_language),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = languageDisplayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
        }
    }
}

@Composable
private fun FeedbackButton(context: Context) {
    val sendFeedBack = stringResource(R.string.send_feedback)
    val onClick = {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:app@sparcs.org")
        }

        val chooser = Intent.createChooser(emailIntent, sendFeedBack)

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("SettingsView", "Error launching email app")
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_feedback),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.send_feedback),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun SendCrashReportsButton(
    isCrashlyticsEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.round_lightbulb_outline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.send_crash_reports),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.weight(1f))
        Switch(
            checked = isCrashlyticsEnabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun Term(context: Context, navController: NavHostController){
    ServiceNavButton(
        stringResource(R.string.privacy_policy),
        painterResource(R.drawable.outline_policy),
        color = MaterialTheme.colorScheme.onSurface
    ) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(Constants.privacyPolicyURL)
        ); context.startActivity(intent)
    }

    ServiceNavButton(
        stringResource(R.string.terms_of_use),
        painterResource(R.drawable.outline_description),
        color = MaterialTheme.colorScheme.onSurface
    ) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(Constants.termsOfUseURL)
        ); context.startActivity(intent)
    }

    ServiceNavButton(
        stringResource(R.string.legal_notices),
        painterResource(R.drawable.outline_balance),
        color = MaterialTheme.colorScheme.onSurface
    ) {
        navController.navigate(Channel.LicenseView.name)
       }

    ServiceNavButton(
        stringResource(R.string.acknowledgements),
        painterResource(R.drawable.outline_volunteer_activism),
        color = MaterialTheme.colorScheme.onSurface
    ) {
        navController.navigate(Channel.CreditView.name)
    }

    VersionRow()
}

@Composable
private fun ServiceNavButton(
    text: String,
    painter: Painter,
    color: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = color
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.arrow_forward_ios),
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.round_logout),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.sign_out),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun ThemeSwitcherButton(
    settingsViewModel: SettingsViewModelProtocol,
) {
    var showDialog by remember { mutableStateOf(false) }
    val darkMode by settingsViewModel.darkModeSetting.collectAsState(initial = null)
    val currentModeText = when (darkMode) {
        true -> stringResource(R.string.dark_mode)
        false -> stringResource(R.string.white_mode)
        null -> stringResource(R.string.system_default)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { showDialog = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_dark_mode),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.dark_mode),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = currentModeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(R.string.dark_mode)) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            settingsViewModel.setTheme("system"); showDialog = false
                        }
                    ) {
                        RadioButton(
                            selected = darkMode == null,
                            onClick = { settingsViewModel.setTheme("system"); showDialog = false }
                        )
                        Text(stringResource(R.string.system_default))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            settingsViewModel.setTheme("light"); showDialog = false
                        }
                    ) {
                        RadioButton(
                            selected = darkMode == false,
                            onClick = { settingsViewModel.setTheme("light"); showDialog = false }
                        )
                        Text(stringResource(R.string.white_mode))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            settingsViewModel.setTheme("dark"); showDialog = false
                        }
                    ) {
                        RadioButton(
                            selected = darkMode == true,
                            onClick = { settingsViewModel.setTheme("dark"); showDialog = false })
                        Text(stringResource(R.string.dark_mode))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
private fun VersionRow() {
    val context = LocalContext.current
    val versionName = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.0"
    } catch (e: Exception) {
        "0.0"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.round_error_outline),
            contentDescription = null
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.app_version),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = versionName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.grayBB
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        SettingsView(rememberNavController(), MockSettingsViewModel())
    }
}

