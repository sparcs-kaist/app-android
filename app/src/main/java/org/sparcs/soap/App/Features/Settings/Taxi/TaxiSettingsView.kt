package org.sparcs.soap.App.Features.Settings.Taxi

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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.Settings.Components.InfoTooltip
import org.sparcs.soap.App.Features.Settings.Components.RowElementView
import org.sparcs.soap.App.Features.Settings.Components.SettingsViewNavigationBar
import org.sparcs.soap.App.Shared.Extensions.PhoneNumberVisualTransformation
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Extensions.toPhoneNumberFormat
import org.sparcs.soap.App.Shared.Extensions.toggle
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.Shared.ViewModelMocks.Taxi.MockTaxiSettingsViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R

@Composable
fun TaxiSettingsView(
    viewModel: TaxiSettingsViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val hasNumberRegistered by remember {
        derivedStateOf { viewModel.user?.phoneNumber?.isEmpty() == false }
    }

    val isBankAccountValid by remember {
        derivedStateOf {
            val nameNotEmpty = !viewModel.bankName.isNullOrEmpty()
            val numberNotEmpty = viewModel.bankNumber.isNotEmpty()

            val isFull = nameNotEmpty && numberNotEmpty
            val isEmpty = !nameNotEmpty && !numberNotEmpty

            isFull || isEmpty
        }
    }

    val isPhoneNumberValid by remember {
        derivedStateOf { viewModel.phoneNumber.isEmpty() || viewModel.phoneNumber.length == Constants.formattedPhoneNumberLength }
    }

    val hasNumberChanged by remember {
        derivedStateOf { viewModel.user?.phoneNumber == null && viewModel.phoneNumber.isNotEmpty() }
    }

    val hasBadgeChanged by remember {
        derivedStateOf { viewModel.user?.badge != viewModel.showBadge }
    }

    val hasBankAccountChanged by remember {
        derivedStateOf {
            val currentAccount = viewModel.user?.account

            if (currentAccount.isNullOrEmpty()) {
                !viewModel.bankName.isNullOrEmpty() || viewModel.bankNumber.isNotEmpty()
            } else {
                val parts = currentAccount.split(" ")
                val firstName = parts.getOrNull(0)
                val lastNumber = parts.getOrNull(1)

                (firstName != viewModel.bankName) || (lastNumber != viewModel.bankNumber)
            }
        }
    }
    val hasResidenceChanged by remember {
        derivedStateOf { viewModel.user?.residence != viewModel.residence }
    }

    val isValid by remember {
        derivedStateOf {
            val basicValid = isBankAccountValid && isPhoneNumberValid
            val dataChanged =
                hasNumberChanged || hasBankAccountChanged || (hasNumberRegistered && hasBadgeChanged) || hasResidenceChanged

            basicValid && dataChanged
        }
    }

    val isChanged by remember {
        derivedStateOf {
            hasNumberChanged || hasBankAccountChanged || (hasNumberRegistered && hasBadgeChanged) || hasResidenceChanged
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    var showAlert by remember { mutableStateOf(false) }
    var showToggle by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUser()
        showToggle = viewModel.phoneNumber.isNotEmpty()
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.taxi_settings),
                onDismiss = {
                    if (isChanged) {
                        showDiscardDialog = true
                    } else {
                        navController.popBackStack()
                    }
                },
                isEditable = true,
                isDoneEnabled = isValid,
                onClickDone = {
                    if (hasNumberChanged) {
                        showAlert = true
                    } else {
                        coroutineScope.launch {
                            viewModel.editInformation()
                            if (!viewModel.showAlert) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            )
        },
        modifier = Modifier.analyticsScreen("Taxi Settings")
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {

            when (state) {
                TaxiSettingsViewModel.ViewState.Loading -> LoadingView()
                TaxiSettingsViewModel.ViewState.Loaded -> LoadedView(
                    viewModel,
                    navController,
                    hasNumberRegistered,
                    onShowToggleChanged = { showToggle = it }
                )

                is TaxiSettingsViewModel.ViewState.Error -> {
                    val message = (state as TaxiSettingsViewModel.ViewState.Error).messageRes
                    ErrorView(
                        icon = Icons.Default.Warning,
                        message = stringResource(message),
                        onRetry = { coroutineScope.launch { viewModel.fetchUser() } }
                    )
                }
            }
        }
    }
    if (viewModel.showAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.showAlert = false },
            confirmButton = {
                TextButton(onClick = { viewModel.showAlert = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.error)) },
            text = {
                viewModel.alertMessageRes?.let { Text(stringResource(it)) }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; navController.popBackStack() }) {
                    Text(stringResource(R.string.exit))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.keep_editing))
                }
            },
            title = { Text(stringResource(R.string.are_you_sure)) },
            text = { Text(stringResource(R.string.discard_this_settings)) }
        )
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text(stringResource(R.string.warning)) },
            text = {
                Text("${stringResource(R.string.edit_message_phone_number)}\n\n${viewModel.phoneNumber.toPhoneNumberFormat()}")
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        viewModel.editInformation()
                        if (!viewModel.showAlert) {
                            navController.popBackStack()
                        }
                    }
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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
        RowElementView(
            title = stringResource(R.string.phone_number),
            content = stringResource(R.string.unknown)
        )
    }
}

@Composable
private fun LoadedView(
    viewModel: TaxiSettingsViewModelProtocol,
    navController: NavController,
    hasNumberRegistered: Boolean,
    onShowToggleChanged: (Boolean) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleMedium)

                if (viewModel.showBadge) {
                    Spacer(Modifier.width(4.dp))
                    InfoTooltip(
                        tooltipText = stringResource(R.string.members_with_this_badge),
                        icon = painterResource(R.drawable.phone_circle_fill),
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
                onValueChange = { input ->
                    val onlyNumbers = input.filter { it.isDigit() }
                    viewModel.bankNumber = onlyNumbers
                },
                label = {
                    Text(
                        stringResource(R.string.enter_bank_number),
                        color = MaterialTheme.colorScheme.grayBB
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Phone Number
            OutlinedTextField(
                value = viewModel.phoneNumber,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    viewModel.phoneNumber = filtered
                    onShowToggleChanged(filtered.isNotEmpty())
                },
                label = {
                    Text(
                        stringResource(R.string.enter_phone_number),
                        color = MaterialTheme.colorScheme.grayBB
                    )
                },
                enabled = !hasNumberRegistered,
                visualTransformation = PhoneNumberVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            //Badge
            if (hasNumberRegistered) {
                BadgeToggle(viewModel)
            }

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
                    onDone = { focusManager.clearFocus() }
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
            Icons.Outlined.SmsFailed
        )
    }
}

@Composable
fun NavigationLinkWithIcon(onClick: () -> Unit, text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun BadgeToggle(
    viewModel: TaxiSettingsViewModelProtocol,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.show_badge))
        Spacer(Modifier.width(4.dp))
        InfoTooltip(
            tooltipText = stringResource(R.string.members_with_this_badge),
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            tint = MaterialTheme.colorScheme.grayBB
        )
        Spacer(Modifier.weight(1f))
        Switch(
            checked = viewModel.showBadge,
            onCheckedChange = {
                haptic.toggle(it)
                viewModel.showBadge = it
            }
        )
    }
}

@Composable
private fun BankPicker(
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.bank_name))
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Row(Modifier.clickable { expanded = !expanded }) {
                Text(
                    text = selected ?: stringResource(R.string.select_bank),
                    color = if (selected == null)
                        MaterialTheme.colorScheme.grayBB
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Rounded.ArrowDropDown,
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