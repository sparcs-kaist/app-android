package com.example.soap.Features.Settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.round_public), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Change Language")
                }
            }

            item {
                Text("Services", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))

                ServiceNavButton("Ara") { navController.navigate("ara_settings") }
                ServiceNavButton("Taxi") { navController.navigate("taxi_settings") }
                ServiceNavButton("OTL") { navController.navigate("otl_settings") }
            }
        }
    }
}

@Composable
fun ServiceNavButton(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(name)
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        SettingsView(rememberNavController())
    }
}

