package org.sparcs.soap.App.Domain.Enums.Taxi

import androidx.compose.ui.graphics.vector.ImageVector

sealed class TaxiInfoItem {
    abstract val label: String
    val id: String get() = label

    data class Plain(
        override val label: String,
        val value: String
    ) : TaxiInfoItem()

    data class WithIcon(
        override val label: String,
        val value: String,
        val systemImage: ImageVector
    ) : TaxiInfoItem()
}
