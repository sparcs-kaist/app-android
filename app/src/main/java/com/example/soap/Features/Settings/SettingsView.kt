package com.example.soap.Features.Settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.R
import com.example.soap.ui.theme.Theme

@Composable
fun SettingsView(
//    viewModel: SettingsViewModelProtocol = HiltViewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SettingsViewNavigationBar (
                title = "Settings",
                onDismiss = { navController.navigate(Channel.Start.name )}
            )
    }){ innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
            }

            item {
                Text(
                    text = "Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )

                ServiceNavButton("Ara", painterResource(R.drawable.baseline_electric_bolt)) { navController.navigate(Channel.AraSettings.name) }
                ServiceNavButton("Taxi", painterResource(R.drawable.baseline_electric_bolt)) { navController.navigate(Channel.TaxiSettings.name) }
                ServiceNavButton("OTL", painterResource(R.drawable.baseline_electric_bolt)) { navController.navigate(Channel.OTLSettings.name) }
            }
        }
    }
}


@Composable
fun AppSettings(context: Context){
    val onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                // Android 13 이상: 앱 언어 변경 화면
                val action = Settings::class.java.getField("ACTION_APP_LOCALE_SETTINGS").get(null) as String
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
    ){
        Icon(
            painter = painterResource(R.drawable.round_public),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "Change Language",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ServiceNavButton(
    text: String,
    painter: Painter,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            painter = painter,
            contentDescription = null,
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
@Preview
private fun Preview(){
    Theme {
        SettingsView(rememberNavController())
    }
}

