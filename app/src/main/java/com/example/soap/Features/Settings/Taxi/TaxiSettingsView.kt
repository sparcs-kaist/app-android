package com.example.soap.Features.Settings.Taxi

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Features.Settings.Components.RowElementView
import com.example.soap.Features.Settings.SettingsViewModel
import com.example.soap.Features.Settings.SettingsViewModelProtocol
import com.example.soap.R
import com.example.soap.Shared.ViewModelMocks.MockSettingsViewModel
import com.example.soap.ui.theme.Theme
import kotlinx.coroutines.launch

@Composable
fun TaxiSettingsView(
    viewModel: SettingsViewModelProtocol,
    navController: NavController
) {
    var safariURL by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchTaxiUser()
    }

    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {

            when (viewModel.taxiState.collectAsState().value) {
                SettingsViewModel.ViewState.Loading -> LoadingView()
                SettingsViewModel.ViewState.Loaded -> LoadedView(
                    viewModel,
                    navController,
                    onOpenUrl = { safariURL = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val bankName = viewModel.taxiBankName.value ?: return@Button
                    coroutineScope.launch {
                        viewModel.taxiEditBankAccount("${bankName} ${viewModel.taxiBankNumber.value}")
                    }
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

        safariURL?.let { url ->
            WebViewDialog(url) { safariURL = null }
        }
    }
}
@Composable
private fun LoadingView() {
    Column {
        RowElementView(title = "Nickname", content = "Unknown")
        RowElementView(title = "Bank Account", content = "Unknown")
    }
}

@Composable
private fun LoadedView(viewModel: SettingsViewModelProtocol, navController: NavController, onOpenUrl: (String) -> Unit) {
    Column {
        Text("Profile", style = MaterialTheme.typography.titleMedium)
        RowElementView(title = "Nickname", content = viewModel.taxiUser.value?.nickname ?: "Unknown")

        BankPicker(
            selected = viewModel.taxiBankName.value,
            options = Constants.taxiBankNameList,
            onSelected = { viewModel.taxiBankName.value = it }
        )

        OutlinedTextField(
            value = viewModel.taxiBankNumber.value,
            onValueChange = { viewModel.taxiBankNumber.value = it },
            label = { Text("Enter Bank Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Service", style = MaterialTheme.typography.titleMedium)

        NavigationLinkWithIcon({}, "Report Details", Icons.Default.Warning)
        NavigationLinkWithIcon({ onOpenUrl("https://sparcs.org") }, "Terms of Service", Icons.AutoMirrored.Filled.List)
        NavigationLinkWithIcon({ onOpenUrl("https://sparcs.org") }, "Privacy Policy", Icons.AutoMirrored.Filled.List)
    }//Todo - ? ScrollableTextView("taxi_privacy_policy")?
}

@Composable
private fun NavigationLinkWithIcon(onClick: () -> Unit, text: String, icon: ImageVector) {
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

fun isValid(viewModel: SettingsViewModelProtocol): Boolean {
    val bankName = viewModel.taxiBankName.value
    return bankName != null &&
            viewModel.taxiBankNumber.value.isNotEmpty() &&
            (viewModel.taxiUser.value?.account != "$bankName ${viewModel.taxiBankNumber.value}")
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
private fun BankPicker(
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected ?: "Select Bank",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            label = { Text("Bank Name") }
        )
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



@Preview
@Composable
private fun PreviewTaxiSettingsLoading() {
    val viewModel = MockSettingsViewModel(SettingsViewModel.ViewState.Loading)

    Theme {
        TaxiSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview
@Composable
private fun PreviewTaxiSettingsLoaded() {
    val viewModel = MockSettingsViewModel(SettingsViewModel.ViewState.Loaded).apply {
        taxiUser.value =null //Todo-TaxiUser.mock
        taxiBankName.value = taxiUser.value?.account?.split(" ")?.firstOrNull()
        taxiBankNumber.value = taxiUser.value?.account?.split(" ")?.getOrNull(1) ?: ""
    }

    Theme {
        TaxiSettingsView(viewModel = viewModel, navController = rememberNavController())
    }
}