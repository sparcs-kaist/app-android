package org.sparcs.soap.app.domain.helpers

import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot

interface CreditSummaryPublisher {
    val revision: Long
    suspend fun publish(snapshot: CreditSummarySnapshot, expectedRevision: Long)
}
