package com.sparcs.soap.Features.Settings.Taxi

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.Features.Settings.Components.RowElementView
import com.sparcs.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Extensions.PhoneNumberVisualTransformation
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.Shared.ViewModelMocks.Taxi.MockTaxiSettingsViewModel
import com.sparcs.soap.Shared.Views.ContentViews.ErrorView
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TaxiSettingsView(
    viewModel: TaxiSettingsViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUser()
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.taxi_settings),
                onDismiss = { navController.popBackStack() }
            )
        }) { innerPadding ->

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
                    coroutineScope
                )

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
        }
    }
}

@Composable
private fun LoadingView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RowElementView(
            title = stringResource(R.string.nickname),
            content = stringResource(R.string.unknown)
        )
        RowElementView(
            title = stringResource(R.string.bank_name),
            content = stringResource(R.string.unknown)
        )
    }
}

@Composable
private fun LoadedView(
    viewModel: TaxiSettingsViewModelProtocol,
    navController: NavController,
    coroutineScope: CoroutineScope,
) {
    val context = LocalContext.current
    val invalidBankNumber = stringResource(R.string.invalid_bank_number)
    val changeApplied = stringResource(R.string.change_applied)
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPopOver by remember { mutableStateOf(false) }

    Column {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleMedium)

                if (viewModel.user?.badge == true) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.phone_circle_fill),
                        contentDescription = "Badge",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(15.dp).clickable { showPopOver = true }
                    )
                }
            }

            RowElementView(
                title = stringResource(R.string.nickname),
                content = viewModel.user?.nickname ?: stringResource(R.string.unknown)
            )

            BankPicker(
                selected = viewModel.bankName,
                options = Constants.taxiBankNameList,
                onSelected = { viewModel.bankName = it }
            )

            //Bank Number
            OutlinedTextField(
                value = viewModel.bankNumber,
                onValueChange = { viewModel.bankNumber = it },
                label = {
                    Text(
                        stringResource(R.string.enter_bank_number),
                        color = MaterialTheme.colorScheme.grayBB
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        coroutineScope.launch {
                            try {
                                if (isValid(viewModel)) {
                                    viewModel.editBankAccount("${viewModel.bankName} ${viewModel.bankNumber}")
                                    Toast.makeText(context, changeApplied, Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Toast.makeText(context, invalidBankNumber, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Unknown error"
                                showErrorDialog = true
                            }
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Phone Number
            OutlinedTextField(
                value = viewModel.phoneNumber,
                onValueChange = { input ->
                    viewModel.phoneNumber = input.filter { it.isDigit() }
                },
                label = {
                    Text(
                        stringResource(R.string.enter_phone_number),
                        color = MaterialTheme.colorScheme.grayBB
                    )
                },
                visualTransformation = PhoneNumberVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        coroutineScope.launch {
                            try {
                                viewModel.registerPhoneNumber(viewModel.phoneNumber)
                                Toast.makeText(context, changeApplied, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Unknown error"
                                showErrorDialog = true
                            }
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Residence
            OutlinedTextField(
                value = viewModel.residence,
                onValueChange = { viewModel.residence = it },
                label = {
                    Text(
                        stringResource(R.string.enter_residence),
                        color = MaterialTheme.colorScheme.grayBB
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        coroutineScope.launch {
                            try {
                                viewModel.registerResidence(viewModel.residence)
                                Toast.makeText(context, changeApplied, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Unknown error"
                                showErrorDialog = true
                            }
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.services), style = MaterialTheme.typography.titleMedium)

        NavigationLinkWithIcon(
            { navController.navigate(Channel.TaxiReportSettings.name) },
            stringResource(R.string.report_details),
            painterResource(R.drawable.outline_sms_failed)
        )

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                title = { Text(stringResource(R.string.error)) },
                text = { Text(errorMessage) }
            )
        }

        if(showPopOver){
            AlertDialog(
                onDismissRequest = { showPopOver = false },
                title = null,
                text = {
                    Text(stringResource(R.string.members_with_this_badge))
                },
                confirmButton = {
                    TextButton(onClick = { showPopOver = false }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationLinkWithIcon(onClick: () -> Unit, text: String, icon: Painter) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

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
    val bankNumber = viewModel.bankNumber

    return bankName != null &&
            bankNumber.isNotEmpty() &&
            (viewModel.user?.account != "$bankName ${bankNumber}")
}

@Composable
fun BankPicker(
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.bank_name))
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Row {
                Text(
                    text = selected ?: stringResource(R.string.select_bank),
                    color = if (selected == null)
                        MaterialTheme.colorScheme.grayBB
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painterResource(R.drawable.baseline_arrow_drop_down),
                    contentDescription = null
                )
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