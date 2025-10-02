package com.example.soap.Features.Settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.R
import com.example.soap.ui.theme.Theme

@Composable
fun SettingsView(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = "Settings",
                onDismiss = { navController.navigate(Channel.Start.name) }
            )
        }) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                Text(
                    text = "Miscellaneous",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )
                AppSettings(context)
                ThemeSwitcherButton(settingsViewModel)
                FeedbackButton(context)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )

                ServiceNavButton(
                    "Ara",
                    painterResource(R.drawable.ara_logo)
                ) { navController.navigate(Channel.AraSettings.name) }

                ServiceNavButton(
                    "Taxi",
                    painterResource(R.drawable.taxi_logo)
                ) { navController.navigate(Channel.TaxiSettings.name) }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )
                SignOutButton { navController.navigate(Channel.SignOut.name) }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun AppSettings(context: Context) {
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

        Text(
            text = "Change Language",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun FeedbackButton(context: Context) {
    val onClick = {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:app@sparcs.org")
        }

        val chooser = Intent.createChooser(emailIntent, "Send Feedback")

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
            text = "Send Feedback",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun ServiceNavButton(
    text: String,
    painter: Painter,
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
            tint = Color.Unspecified
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
            text = "Sign Out",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun ThemeSwitcherButton(
    settingsViewModel: SettingsViewModel,
) {
    var showDialog by remember { mutableStateOf(false) }
    val darkMode by settingsViewModel.darkModeSetting.collectAsState(initial = null)

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

        Text(
            text = stringResource(R.string.dark_mode),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
                        modifier = Modifier.clickable { settingsViewModel.setTheme("system"); showDialog = false }
                    ){
                        RadioButton(
                            selected = darkMode == null,
                            onClick = { settingsViewModel.setTheme("system"); showDialog = false }
                        )
                        Text(stringResource(R.string.system_default))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { settingsViewModel.setTheme("light"); showDialog = false }
                    ) {
                        RadioButton(
                            selected = darkMode == false,
                            onClick = { settingsViewModel.setTheme("light"); showDialog = false }
                        )
                        Text(stringResource(R.string.white_mode))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { settingsViewModel.setTheme("dark"); showDialog = false }
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
@Preview
private fun Preview() {
    Theme {
        SettingsView(rememberNavController())
    }
}

