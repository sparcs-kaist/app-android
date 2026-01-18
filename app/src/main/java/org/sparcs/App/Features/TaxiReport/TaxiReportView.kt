package org.sparcs.App.Features.TaxiReport

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.App.Domain.Models.Taxi.TaxiReport
import org.sparcs.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.App.Domain.Usecases.MockUserUseCase
import org.sparcs.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.App.Features.TaxiChat.TaxiChatViewModel
import org.sparcs.App.Features.TaxiReport.Components.TaxiReportUser
import org.sparcs.App.Shared.Extensions.isNetworkError
import org.sparcs.App.Shared.ViewModelMocks.Taxi.MockTaxiReportViewModel
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiReportView(
    viewModel: TaxiReportViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    var taxiUser by remember { mutableStateOf<TaxiUser?>(null) }

    val isPreview = LocalInspectionMode.current
    val userUseCase: UserUseCaseProtocol =
        if (!isPreview) hiltViewModel<TaxiChatViewModel>().userUseCase else MockUserUseCase()
    val room by viewModel.room.collectAsState()

    var showAlert by remember { mutableStateOf(false) }
    @StringRes var alertTitle: Int by remember { mutableStateOf(0) }
    @StringRes var alertMessage: Int by remember { mutableStateOf(0) }

    fun showAlert(@StringRes title: Int, @StringRes message: Int) {
        alertTitle = title
        alertMessage = message
        showAlert = true
    }

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
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.report)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painterResource(R.drawable.arrow_back_ios),
                            contentDescription = "Cancel"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(innerPadding)
                .fillMaxWidth(),
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

            // Report Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                viewModel.createReport(room.id)

                                showAlert(
                                    title = R.string.report_submitted,
                                    message = R.string.reported_successfully
                                )
                                navController.popBackStack()

                            } catch (e: Exception) {
                                val message = if (e.isNetworkError()) {
                                    R.string.network_connection_error
                                } else {
                                    R.string.unexpected_error_reporting_user
                                }

                                showAlert(
                                    title = R.string.error,
                                    message = message
                                )
                            }
                        }
                    },
                    enabled = isValid
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_check),
                        contentDescription = null
                    )
                    Text(stringResource(R.string.done))
                }
            }
        }
    }
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) { Text(stringResource(R.string.ok)) }
            },
            title = { Text(stringResource(alertTitle)) },
            text = { Text(stringResource(alertMessage)) }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.grayBB
    )
}

@Composable
fun ParticipantsCard(
    participants: List<TaxiParticipant>,
    selectedUser: TaxiParticipant?,
    onUserSelected: (TaxiParticipant) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.reason))
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(selectedReason?.text ?: R.string.didnot_send_the_money),
                    color = MaterialTheme.colorScheme.grayBB
                )
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_drop_down),
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.didnot_send_the_money)) },
                    enabled = room.isDeparted,
                    onClick = {
                        onReasonSelected(TaxiReport.Reason.NO_SETTLEMENT)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.didnot_come_on_time)) },
                    enabled = room.isDeparted,
                    onClick = {
                        onReasonSelected(TaxiReport.Reason.NO_SHOW)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.etc)) },
                    onClick = {
                        onReasonSelected(TaxiReport.Reason.ETC_REASON)
                        expanded = false
                    }
                )
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
}

@Composable
fun EtcTextField(
    text: String,
    maxLength: Int,
    onTextChange: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= maxLength) onTextChange(it) },
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.report_details),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${text.length}/$maxLength",
            style = MaterialTheme.typography.labelSmall,
            color = if (text.length >= maxLength) Color(0xFFEE8146) else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier.align(Alignment.End)
        )
    }
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