package com.example.soap.Features.Settings.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.Taxi.TaxiReport
import com.example.soap.Shared.Extensions.formattedString
import com.example.soap.ui.theme.Theme
import java.util.Date
import java.util.UUID

@Composable
fun TaxiReportDetailRow(report: TaxiReport) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        when (report.reason) {
            TaxiReport.ReportReason.ETC -> RowElementView(title = "Reason", content = "Other reasons")
            TaxiReport.ReportReason.NO_SHOW -> RowElementView(title = "Reason", content = "Not showing up")
            TaxiReport.ReportReason.NO_SETTLEMENT -> RowElementView(title = "Reason", content = "No settlement")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        if (report.nickname != null && report.reportType == TaxiReport.ReportType.REPORTING) {
            RowElementView(title = "Nickname", content = report.nickname)
            Spacer(modifier = Modifier.height(4.dp))
        }

        RowElementView(title = "Date", content = report.reportedAt.formattedString())
        Spacer(modifier = Modifier.height(4.dp))

        if (report.reason == TaxiReport.ReportReason.ETC) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "Other reasons")
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = report.etcDetail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            TaxiReportDetailRow(
                report = TaxiReport(
                    id = UUID.randomUUID().toString(),
                    nickname = "자신감 있는 유체역학_8c249",
                    reportType = TaxiReport.ReportType.REPORTING,
                    reason = TaxiReport.ReportReason.ETC,
                    etcDetail = "Not showing up at the scheduled time",
                    reportedAt = Date()
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            TaxiReportDetailRow(
                report = TaxiReport(
                    id = UUID.randomUUID().toString(),
                    nickname = "자신감 있는 유체역학_8c249",
                    reportType = TaxiReport.ReportType.REPORTED,
                    reason = TaxiReport.ReportReason.NO_SHOW,
                    etcDetail = "Not showing up at the scheduled time",
                    reportedAt = Date()
                )
            )
        }
    }
}
