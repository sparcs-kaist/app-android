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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.soap.R
import com.example.soap.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
//    viewModel: SettingsViewModel = viewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Miscellaneous", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
                AppSettings(context)
            }

            item {
                Text("Services", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))

                ServiceNavButton("Ara") { /*navigate to Ara Setting*/ }
                ServiceNavButton("Taxi") { /*navigate to Taxi Setting*/ }
                ServiceNavButton("OTL") { /*navigate to OTL Setting*/ }
            }
        }
    }
}

@Composable
fun AppSettings(context: Context){
    Button(
        onClick = {
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
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(painter = painterResource(R.drawable.round_public), contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Change Language")
    }
}



@Composable
fun ServiceNavButton(
    text: String,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ){
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

