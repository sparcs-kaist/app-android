package org.sparcs.App.Features.Settings.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.Domain.Enums.Taxi.TaxiReportType
import org.sparcs.App.Domain.Models.Taxi.TaxiReport
import org.sparcs.App.Shared.Extensions.formattedString
import org.sparcs.App.Shared.Mocks.mock
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.R

@Composable
fun TaxiReportDetailRow(
    report: TaxiReport,
    reportType: TaxiReportType
) {
    Column(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        when (report.reason) {
            TaxiReport.Reason.ETC_REASON -> RowElementView(title = stringResource(R.string.report_reason), content = stringResource(R.string.other_reasons))
            TaxiReport.Reason.NO_SHOW -> RowElementView(title = stringResource(R.string.report_reason), content = stringResource(R.string.not_showing_up))
            TaxiReport.Reason.NO_SETTLEMENT -> RowElementView(title = stringResource(R.string.report_reason), content = stringResource(R.string.no_settlement))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        if (reportType == TaxiReportType.OUTGOING) {
            RowElementView(title = stringResource(R.string.nickname), content = report.reportedUser.nickname)
            Spacer(modifier = Modifier.height(4.dp))
        }

        RowElementView(title = stringResource(R.string.date), content = report.time.formattedString())
        Spacer(modifier = Modifier.height(4.dp))

        if (report.reason == TaxiReport.Reason.ETC_REASON) {
            RowElementView(title = stringResource(R.string.other_reasons), content = report.etcDetails)
        }
    }
}

@Composable
fun TaxiReportDetailSkeletonRow() {
    val row = @Composable{
        Row{
            Box(
                modifier = Modifier
                    .width((80..120).random().dp)
                    .padding(8.dp)
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.grayBB.copy(0.5f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width((140..200).random().dp)
                    .padding(8.dp)
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.grayBB.copy(0.5f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
    Column(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat((2..3).random()){ row() }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
