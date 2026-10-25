package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/** Room size as a segmented button: 2, 3 or 4 people. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiCapacityPicker(
    capacity: Int,
    onCapacityChange: (Int) -> Unit,
) {
    val options = (2..4).toList()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.capacity),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() }
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, count ->
                SegmentedButton(
                    selected = capacity == count,
                    onClick = { onCapacityChange(count) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    label = { Text(pluralStringResource(R.plurals.people_count, count, count)) }
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { TaxiCapacityPicker(4) {} }
}