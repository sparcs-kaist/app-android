package org.sparcs.soap.app.features.timetable.creditCalculation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun CreditsSummaryCard(state: CreditCalculationViewState, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .glassBorder(MaterialTheme.shapes.large),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.credit_calculation), modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
            }
            when {
                state.isLoading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                state.error != null -> Text(
                    stringResource(R.string.credit_load_error),
                    style = MaterialTheme.typography.bodySmall
                )

                else -> {
                    val summary = state.overallSummary
                    GPASummaryContent(
                        summary.gpa,
                        summary.earnedCredits,
                        state.requirements.graduation,
                        showArrow = false
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreditsSummaryCardPreview() {
    Theme { CreditsSummaryCard(creditPreviewState(), {}) }
}

@Preview(showBackground = true)
@Composable
private fun CreditsSummaryLoadingPreview() {
    Theme { CreditsSummaryCard(CreditCalculationViewState(), {}) }
}
