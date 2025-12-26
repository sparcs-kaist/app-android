package org.sparcs.Domain.Enums.Taxi

sealed class TaxiInfoItem {
    abstract val label: String
    val id: String get() = label

    data class plain(
        override val label: String,
        val value: String
    ) : TaxiInfoItem()

    data class withIcon(
        override val label: String,
        val value: String,
        val systemImage: Int
    ) : TaxiInfoItem()
}
