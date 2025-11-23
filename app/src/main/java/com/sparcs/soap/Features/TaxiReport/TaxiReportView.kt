package com.sparcs.soap.Features.TaxiReport

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sparcs.soap.Domain.Models.Taxi.TaxiParticipant
import com.sparcs.soap.Domain.Models.Taxi.TaxiReport
import com.sparcs.soap.Domain.Models.Taxi.TaxiRoom
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Domain.Usecases.UserUseCaseProtocol
import com.sparcs.soap.Features.TaxiChat.TaxiChatViewModel
import com.sparcs.soap.Features.TaxiReport.Components.TaxiReportUser
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme
import com.sparcs.soap.ui.theme.grayBB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiReportView(
    viewModel: TaxiReportViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var taxiUser by remember { mutableStateOf<TaxiUser?>(null) }

    val userUseCase: UserUseCaseProtocol = hiltViewModel<TaxiChatViewModel>().userUseCase
    val room by viewModel.room.collectAsState()

    val reportSubmitted = stringResource(R.string.reported_successfully)
    LaunchedEffect(Unit) {
        taxiUser = userUseCase.taxiUser
    }

    val selectedUser by viewModel.selectedUser.collectAsState()
    val selectedReason by viewModel.selectedReason.collectAsState()
    val context = LocalContext.current
    val isValid = selectedUser != null && selectedReason != null &&
            (selectedReason != TaxiReport.Reason.ETC_REASON ||
                    (viewModel.etcDetails.length in 1..viewModel.maxEtcDetailsLength))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Report") },
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
                .padding(16.dp).padding(innerPadding)
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
                    try {
                        scope.launch { viewModel.createReport(room.id) }
                        Toast.makeText(context, reportSubmitted, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } catch (e: Exception) {
                        viewModel.handleException(e)
                        Toast.makeText(context, "Failed to send report: ${e.message}", Toast.LENGTH_SHORT).show()
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
        onUserSelected: (TaxiParticipant) -> Unit
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
        onEtcChanged: (String) -> Unit
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
                    text = selectedReason?.text ?: stringResource(R.string.didnot_send_the_money),
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
        onTextChange: (String) -> Unit
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
private fun Preview(){
    Theme {
        TaxiReportView(
            viewModel = viewModel(),
            navController = rememberNavController()
        )
    }
}