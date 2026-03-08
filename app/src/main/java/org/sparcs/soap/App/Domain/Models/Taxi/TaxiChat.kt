package org.sparcs.soap.App.Domain.Models.Taxi

import java.util.Date
import java.util.UUID

data class TaxiChat(
    val id: UUID = UUID.randomUUID(),
    val roomID: String,
    val type: ChatType,
    val authorID: String?,
    val authorName: String?,
    val authorProfileURL: String?,
    val authorIsWithdrew: Boolean?,
    val content: String,
    val time: Date,
    val isValid: Boolean,
    val inOutNames: List<String>?
) {
    enum class ChatType(val type: String) {
        // User sent type
        TEXT("text"),               // normal message
        S3IMG("s3img"),             // S3 uploaded image
        SETTLEMENT("settlement"),  // settlement message
        PAYMENT("payment"),        // payment message
        ACCOUNT("account"),        // account message

        // General type
        ENTRANCE("in"),            // entrance message
        EXIT("out"),               // exit message

        // Bot sent type
        SHARE("share"),
        DEPARTURE("departure"),
        ARRIVAL("arrival"),

        UNKNOWN("unknown");

    }

    companion object {}
}
