package org.sparcs.soap.app.features.taxiChat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R

internal fun validSettlementAmount(input: String, participantCount: Int): Int? =
    input.toIntOrNull()?.takeIf { it > 0 && participantCount > 0 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaxiSettlementAmountSheet(participantCount: Int, onDismiss: () -> Unit, onCommit: (Int) -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    val total = validSettlementAmount(input, participantCount)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.request_settlement), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = input,
                onValueChange = { value -> if (value.all { it in '0'..'9' }) input = value },
                label = { Text(stringResource(R.string.settlement_amount_hint)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text(stringResource(R.string.currency_unit)) },
            )
            Text(stringResource(R.string.taxi_settlement_participants, participantCount))
            if (total != null) {
                Text(stringResource(R.string.taxi_settlement_per_person, total / participantCount),
                    style = MaterialTheme.typography.titleMedium)
            }
            Text(stringResource(R.string.taxi_settlement_rounding),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                modifier = Modifier.fillMaxWidth(), enabled = total != null,
                onClick = { total?.let { onCommit(it); onDismiss() } },
            ) { Text(stringResource(R.string.taxi_settlement_request)) }
        }
    }
}
