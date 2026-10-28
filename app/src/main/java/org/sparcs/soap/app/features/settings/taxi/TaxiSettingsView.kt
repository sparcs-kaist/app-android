package org.sparcs.soap.app.features.settings.taxi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.settings.components.InfoTooltip
import org.sparcs.soap.app.features.settings.components.RowElementView
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.PhoneNumberVisualTransformation
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.toPhoneNumberFormat
import org.sparcs.soap.app.shared.extensions.toggle
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.viewModelMocks.taxi.MockTaxiSettingsViewModel
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiSettingsView(
    viewModel: TaxiSettingsViewModelProtocol = hiltViewModel<TaxiSettingsViewModel>(),
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
        derivedStateOf { viewModel.phoneNumber.isEmpty() || viewModel.phoneNumber.length == Constants.FORMATTED_PHONE_NUMBER_LENGTH }
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

    val hasNicknameChanged by remember {
        derivedStateOf { viewModel.user?.nickname != viewModel.nickname }
    }

    val isValid by remember {
        derivedStateOf {
            val basicValid = isBankAccountValid && isPhoneNumberValid
            val dataChanged =
                hasNumberChanged || hasBankAccountChanged || (hasNumberRegistered && hasBadgeChanged) || hasResidenceChanged || hasNicknameChanged

            basicValid && dataChanged
        }
    }

    val isChanged by remember {
        derivedStateOf {
            hasNumberChanged || hasBankAccountChanged || (hasNumberRegistered && hasBadgeChanged) || hasResidenceChanged || hasNicknameChanged
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    var showAlert by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUser()
    }

    val topBarScrollBehavior = topAppBarScrollBehavior()

    fun saveChanges() {
        coroutineScope.launch {
            isSaving = true
            val success = viewModel.editInformation()
            isSaving = false
            if (success) {
                navController.popBackStack()
            }
        }
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
                isDoneEnabled = isValid && !isSaving,
                isSaving = isSaving,
                onClickDone = {
                    if (hasNumberChanged) {
                        showAlert = true
                    } else {
                        saveChanges()
                    }
                },
                scrollBehavior = topBarScrollBehavior,
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Taxi Settings")
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
                when (state) {
                    TaxiSettingsViewModel.ViewState.Loading -> item { LoadingView() }
                    TaxiSettingsViewModel.ViewState.Loaded -> item {
                        LoadedView(
                            viewModel,
                            navController,
                            hasNumberRegistered
                        )
                    }

                    is TaxiSettingsViewModel.ViewState.Error -> {
                        item {
                            ErrorView(
                                defaultMessageResId = (state as TaxiSettingsViewModel.ViewState.Error).resId,
                                error = (state as TaxiSettingsViewModel.ViewState.Error).error,
                                onRetry = { coroutineScope.launch { viewModel.fetchUser() } }
                            )
                        }
                    }
                }
            }
        }
    }
    GlobalAlertDialog(
        state = viewModel.alertState,
        isPresented = viewModel.isAlertPresented
    ) { viewModel.isAlertPresented = false }

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
                    showAlert = false
                    saveChanges()
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
    Column {
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
) {
    val focusManager = LocalFocusManager.current

    Column {
        SettingsSectionHeader(
            text = stringResource(R.string.profile),
            trailing = if (viewModel.showBadge) {
                {
                    InfoTooltip(
                        tooltipText = stringResource(R.string.members_with_this_badge),
                        imageVector = Icons.Filled.VerifiedUser,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else null
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Nickname
            OutlinedTextField(
                value = viewModel.nickname ?: "",
                onValueChange = { viewModel.nickname = it },
                label = { Text(stringResource(R.string.nickname)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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
                label = { Text(stringResource(R.string.enter_bank_number)) },
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
                },
                label = { Text(stringResource(R.string.enter_phone_number)) },
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

        }

        if (hasNumberRegistered) {
            BadgeToggle(viewModel)
        }

        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = if (hasNumberRegistered) 0.dp else 16.dp)
        ) {
            // Residence
            OutlinedTextField(
                value = viewModel.residence,
                onValueChange = { viewModel.residence = it },
                label = { Text(stringResource(R.string.enter_residence)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsSectionHeader(stringResource(R.string.services))

        SettingsNavigationItem(
            text = stringResource(R.string.report_details),
            onClick = { navController.navigate(Channel.TaxiReportSettings.name) },
            leadingContent = { Icon(Icons.Outlined.SmsFailed, contentDescription = null) }
        )
    }
}

@Composable
private fun BadgeToggle(
    viewModel: TaxiSettingsViewModelProtocol,
) {
    val haptic = LocalHapticFeedback.current
    SettingsSwitchItem(
        text = stringResource(R.string.show_badge),
        checked = viewModel.showBadge,
        onCheckedChange = {
            haptic.toggle(it)
            viewModel.showBadge = it
        },
        headlineTrailing = {
            InfoTooltip(
                tooltipText = stringResource(R.string.members_with_this_badge),
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankPicker(
    selected: String?,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.bank_name)) },
            placeholder = { Text(stringResource(R.string.select_bank)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
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