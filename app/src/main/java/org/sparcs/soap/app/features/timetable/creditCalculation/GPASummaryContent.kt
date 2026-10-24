package org.sparcs.soap.app.features.timetable.creditCalculation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.formatters.creditProgress
import org.sparcs.soap.app.shared.formatters.formatGPA
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.creditCompleteColor

@Composable
internal fun GPASummaryContent(
    gpa: Double?,
    earnedCredits: Int,
    graduationCredits: Int,
    showArrow: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryValue(stringResource(R.string.credit_gpa_label), formatGPA(gpa), "4.3")
            Spacer(Modifier.weight(1f))
            SummaryValue(
                stringResource(R.string.credit_units_label),
                "$earnedCredits",
                "$graduationCredits",
                Alignment.End
            )
            if (earnedCredits >= graduationCredits) {
                Icon(
                    Icons.Rounded.CheckCircle, stringResource(R.string.credit_requirement_met),
                    tint = creditCompleteColor, modifier = Modifier.size(18.dp)
                )
            }
            if (showArrow) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        CreditProgressBar(earnedCredits, graduationCredits)
    }
}

@Composable
private fun SummaryValue(
    title: String,
    value: String,
    total: String,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = alignment, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "/$total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Credits taken toward [minimum] as a Material progress bar, green once the minimum is met. */
@Composable
internal fun CreditProgressBar(
    taken: Int,
    minimum: Int,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { creditProgress(taken, minimum) },
        color = if (taken >= minimum) creditCompleteColor else MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun GPASummaryPreview() {
    Theme { CreditCard { GPASummaryContent(3.73, 96, 138) } }
}

@Preview(showBackground = true)
@Composable
private fun CompletedGPASummaryPreview() {
    Theme { CreditCard { GPASummaryContent(4.3, 140, 138) } }
}
