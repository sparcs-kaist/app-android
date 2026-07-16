package org.sparcs.soap.buddyTestSupport.repository

import org.sparcs.soap.app.domain.enums.taxi.TaxiReports
import org.sparcs.soap.app.domain.models.taxi.TaxiCreateReport
import org.sparcs.soap.app.domain.repositories.taxi.TaxiReportRepositoryProtocol

class MockTaxiReportRepository : TaxiReportRepositoryProtocol {

    var fetchMyReportsResult: Result<TaxiReports> =
        Result.success(TaxiReports(emptyList(), emptyList()))
    var createReportResult: Result<Unit> = Result.success(Unit)

    override suspend fun fetchMyReports(): TaxiReports = fetchMyReportsResult.getOrThrow()

    override suspend fun createReport(report: TaxiCreateReport) {
        createReportResult.getOrThrow()
    }
}
