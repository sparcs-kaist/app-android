package com.example.soap.Features.Settings.Taxi

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.Settings.Components.RowElementView
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.ViewModelMocks.MockTaxiSettingsViewModel
import com.example.soap.Shared.Views.ContentViews.ErrorView
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import kotlinx.coroutines.launch

@Composable
fun TaxiSettingsView(
    viewModel: TaxiSettingsViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    var safariURL by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUser()
    }

    Scaffold(
        topBar = {
        SettingsViewNavigationBar(
            title = "Taxi Settings",
            onDismiss = { navController.navigate(Channel.Settings.name )}
        )
    }){ innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {

            when (viewModel.state.collectAsState().value) {
                TaxiSettingsViewModel.ViewState.Loading -> LoadingView()
                TaxiSettingsViewModel.ViewState.Loaded -> LoadedView(
                    viewModel,
                    navController,
                    onOpenUrl = { safariURL = it })
                is TaxiSettingsViewModel.ViewState.Error -> {
                    val message = (state as TaxiSettingsViewModel.ViewState.Error).message
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = message,
                        onRetry = { coroutineScope.launch { viewModel.fetchUser() } }
                    )
                    Log.e("TaxiSettingsView", message)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(Modifier.align(Alignment.End)){
                Button(
                    onClick = {
                        val bankName = viewModel.bankName ?: return@Button
                        coroutineScope.launch {
                            viewModel.editBankAccount("$bankName ${viewModel.bankNumber}")
                        }
                        navController.popBackStack()
                    },
                    enabled = isValid(viewModel)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_check),
                        contentDescription = null
                    )
                    Text("Done")
                }
            }
        }

        safariURL?.let { url ->
            WebViewDialog(url) { safariURL = null }
        }
    }
}
@Composable
private fun LoadingView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RowElementView(title = "Nickname", content = "Unknown")
        RowElementView(title = "Bank Account", content = "Unknown")
    }
}

@Composable
private fun LoadedView(viewModel: TaxiSettingsViewModelProtocol, navController: NavController, onOpenUrl: (String) -> Unit) {
    Column {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)){
            Text("Profile", style = MaterialTheme.typography.titleMedium)

            RowElementView(title = "Nickname", content = viewModel.user?.nickname ?: "Unknown")

            BankPicker(
                selected = viewModel.bankName,
                options = Constants.taxiBankNameList,
                onSelected = { viewModel.bankName = it }
            )

            OutlinedTextField(
                value = viewModel.bankNumber,
                onValueChange = { viewModel.bankNumber = it },
                label = { Text("Enter Bank Number", color = MaterialTheme.colorScheme.grayBB) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.bankNumber,
                onValueChange = { viewModel.bankNumber = it },
                label = { Text("Enter Phone Number", color = MaterialTheme.colorScheme.grayBB) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Service", style = MaterialTheme.typography.titleMedium)

        NavigationLinkWithIcon({ navController.navigate(Channel.TaxiReportSettings.name) }, "Report Details", painterResource(R.drawable.outline_sms_failed))
        NavigationLinkWithIcon({ onOpenUrl("https://sparcs.org") }, "Terms of Service", painterResource(R.drawable.round_format_list_bulleted))
        NavigationLinkWithIcon({ onOpenUrl("https://sparcs.org") }, "Privacy Policy", painterResource(R.drawable.round_format_list_bulleted))
    }//Todo - ? ScrollableTextView("taxi_privacy_policy")?
}

@Composable
fun NavigationLinkWithIcon(onClick: () -> Unit, text: String, icon: Painter) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ){

        Icon(icon, contentDescription = null)

        Spacer(modifier = Modifier.width(8.dp))

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

fun isValid(viewModel: TaxiSettingsViewModelProtocol): Boolean {
    val bankName = viewModel.bankName
    return bankName != null &&
            viewModel.bankNumber.isNotEmpty() &&
            (viewModel.user?.account != "$bankName ${viewModel.bankNumber}")
}

@Composable
private fun WebViewDialog(url: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Close")
            }
        },
        text = {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}//Todo - SafariViewer
@Composable
fun BankPicker(
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Bank Name")
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Row{
                Text(
                    text = selected ?: "Select Bank",
                    color = if (selected == null)
                        MaterialTheme.colorScheme.grayBB
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Icon(painterResource(R.drawable.baseline_arrow_drop_down), contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun PreviewTaxiSettingsLoading() {
    val viewModel = MockTaxiSettingsViewModel(TaxiSettingsViewModel.ViewState.Loading)

    Theme {
        TaxiSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview
@Composable
private fun PreviewTaxiSettingsLoaded() {
    val viewModel = MockTaxiSettingsViewModel(TaxiSettingsViewModel.ViewState.Loaded).apply {
        user = TaxiUser.mock()
        bankName = user?.account?.split(" ")?.firstOrNull()
        bankNumber = user?.account?.split(" ")?.getOrNull(1) ?: ""
    }

    Theme {
        TaxiSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}