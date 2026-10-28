package org.sparcs.soap.app.features.taxiReport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiReport
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.usecases.MockUserUseCase
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.features.navigationBar.components.CloseButton
import org.sparcs.soap.app.features.taxiChat.TaxiChatViewModel
import org.sparcs.soap.app.features.taxiReport.components.TaxiReportUser
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.viewModelMocks.taxi.MockTaxiReportViewModel
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiReportView(
    viewModel: TaxiReportViewModelProtocol = hiltViewModel<TaxiReportViewModel>(),
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    var taxiUser by remember { mutableStateOf<TaxiUser?>(null) }

    val isPreview = LocalInspectionMode.current
    val userUseCase: UserUseCaseProtocol =
        if (!isPreview) hiltViewModel<TaxiChatViewModel>().userUseCase else MockUserUseCase()
    val room by viewModel.room.collectAsState()

    LaunchedEffect(Unit) {
        taxiUser = userUseCase.taxiUser
    }

    val selectedUser by viewModel.selectedUser.collectAsState()
    val selectedReason by viewModel.selectedReason.collectAsState()
    val isValid = selectedUser != null && selectedReason != null &&
            (selectedReason != TaxiReport.Reason.ETC_REASON ||
                    (viewModel.etcDetails.length in 1..viewModel.maxEtcDetailsLength))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report)) },
                navigationIcon = {
                    CloseButton { navController.popBackStack() }
                }
            )
        },
        modifier = Modifier.analyticsScreen("Taxi Report")
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(stringResource(R.string.who))
            ParticipantsCard(
                participants = room.participants.filter { it.id != taxiUser?.oid },
                selectedUser = selectedUser,
                onUserSelected = { viewModel.setSelectedUser(it) }
            )

            SectionTitle(stringResource(R.string.why))

            ReasonCard(
                room = room,
                selectedReason = selectedReason,
                onReasonSelected = { viewModel.setSelectedReason(it) },
                etcText = viewModel.etcDetails,
                maxEtcLength = viewModel.maxEtcDetailsLength,
                onEtcChanged = { viewModel.etcDetails = it }
            )

            InfoTexts(room = room, selectedReason = selectedReason)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val success = viewModel.createReport(room.id)
                            if (success) {
                                navController.popBackStack()
                            }
                        }
                    },
                    enabled = isValid
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null
                    )
                    Text(stringResource(R.string.done))
                }
            }
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = {
            val isSuccess = viewModel.alertState?.titleResId == R.string.report_submitted

            viewModel.isAlertPresented = false

            if (isSuccess) {
                navController.popBackStack()
            }
        }
    )
}

/** A heading in the form, in the style of the settings section headers. */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics { heading() }
    )
}

/** The other people in the room, one radio row each. */
@Composable
fun ParticipantsCard(
    participants: List<TaxiParticipant>,
    selectedUser: TaxiParticipant?,
    onUserSelected: (TaxiParticipant) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .selectableGroup()
        ) {
            participants.forEach { participant ->
                TaxiReportUser(
                    user = participant,
                    isChecked = participant.id == selectedUser?.id,
                    onClick = { onUserSelected(participant) }
                )
            }
        }
    }
}

/** The reason as a Material dropdown field; "Other" adds a field for the details. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReasonCard(
    room: TaxiRoom,
    selectedReason: TaxiReport.Reason?,
    onReasonSelected: (TaxiReport.Reason) -> Unit,
    etcText: String,
    maxEtcLength: Int,
    onEtcChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedReason?.let { stringResource(it.text) }.orEmpty(),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text(stringResource(R.string.reason)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf(
                    TaxiReport.Reason.NO_SETTLEMENT to room.isDeparted,
                    TaxiReport.Reason.NO_SHOW to room.isDeparted,
                    TaxiReport.Reason.ETC_REASON to true,
                ).forEach { (reason, enabled) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(reason.text)) },
                        enabled = enabled,
                        onClick = {
                            onReasonSelected(reason)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        if (selectedReason == TaxiReport.Reason.ETC_REASON) {
            EtcTextField(
                text = etcText,
                maxLength = maxEtcLength,
                onTextChange = onEtcChanged
            )
        }
    }
}

/** Details for "Other", with a character count that turns red at the limit. */
@Composable
fun EtcTextField(
    text: String,
    maxLength: Int,
    onTextChange: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = text,
        onValueChange = { if (it.length <= maxLength) onTextChange(it) },
        label = { Text(stringResource(R.string.report_details)) },
        supportingText = {
            Text("${text.length}/$maxLength", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        },
        isError = text.length >= maxLength,
        minLines = 3,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun InfoTexts(room: TaxiRoom, selectedReason: TaxiReport.Reason?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        if (!room.isDeparted) {
            Text(
                stringResource(R.string.report_after_departure),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (selectedReason == TaxiReport.Reason.NO_SETTLEMENT) {
            Text(
                stringResource(R.string.email_request_payment),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        TaxiReportView(
            viewModel = MockTaxiReportViewModel(),
            navController = rememberNavController()
        )
    }
}