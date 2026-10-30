package org.sparcs.soap.app.domain.models.ara

data class AraMarketListing(
    val price: Long?,
    val currency: String = DEFAULT_CURRENCY,
    val state: State = State.ON_SALE,
) {
    enum class State(val value: String) {
        ON_SALE("onsale"),
        RESERVED("reserved"),
        SOLD_OUT("soldout");

        companion object {
            fun fromValue(value: String?): State? = entries.firstOrNull { it.value == value }
        }
    }

    companion object {
        const val DEFAULT_CURRENCY = "KRW"
        const val MAX_PRICE = 999_999_999_999L
    }
}
