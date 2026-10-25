package org.sparcs.soap.app.features.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.taxi.TaxiReportType
import org.sparcs.soap.app.domain.models.taxi.TaxiReport
import org.sparcs.soap.app.features.lectureDetail.components.SkeletonLine
import org.sparcs.soap.app.shared.extensions.formattedString
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TaxiReportDetailRow(
    report: TaxiReport,
    reportType: TaxiReportType,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            when (report.reason) {
                TaxiReport.Reason.ETC_REASON -> RowElementView(
                    title = stringResource(R.string.report_reason),
                    content = stringResource(R.string.other_reasons)
                )

                TaxiReport.Reason.NO_SHOW -> RowElementView(
                    title = stringResource(R.string.report_reason),
                    content = stringResource(R.string.not_showing_up)
                )

                TaxiReport.Reason.NO_SETTLEMENT -> RowElementView(
                    title = stringResource(R.string.report_reason),
                    content = stringResource(R.string.no_settlement)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            if (reportType == TaxiReportType.OUTGOING) {
                RowElementView(
                    title = stringResource(R.string.nickname),
                    content = report.reportedUser.nickname
                )
            }

            RowElementView(
                title = stringResource(R.string.date),
                content = report.time.formattedString()
            )

            if (report.reason == TaxiReport.Reason.ETC_REASON) {
                RowElementView(
                    title = stringResource(R.string.other_reasons),
                    content = report.etcDetails
                )
            }
        }
    }
}

@Composable
fun TaxiReportDetailSkeletonRow() {
    val rows = remember { (2..3).random() }
    Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(rows) {
                Row {
                    SkeletonLine(MaterialTheme.typography.bodyMedium, Modifier.width(96.dp))
                    Spacer(Modifier.weight(1f))
                    SkeletonLine(MaterialTheme.typography.bodyMedium, Modifier.width(160.dp))
                }
            }
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TaxiReportDetailRow(
                report = TaxiReport.mock(),
                reportType = TaxiReportType.INCOMING
            )
            Spacer(modifier = Modifier.height(16.dp))
            TaxiReportDetailRow(
                report = TaxiReport.mock(),
                reportType = TaxiReportType.OUTGOING
            )
            Spacer(modifier = Modifier.height(16.dp))
            TaxiReportDetailSkeletonRow()
        }
    }
}
