package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import java.lang.reflect.Type
import kotlin.math.truncate

@JsonAdapter(AraMarketMetadataDTO.Deserializer::class)
data class AraMarketMetadataDTO(
    @SerializedName("price")
    val price: Long?,

    @SerializedName("price_currency")
    val priceCurrency: String?,

    @SerializedName("state")
    val state: String?,
) {
    fun toModel(): AraMarketListing? {
        if (price == null && state == null) return null
        return AraMarketListing(
            price = price,
            currency = priceCurrency ?: AraMarketListing.DEFAULT_CURRENCY,
            state = AraMarketListing.State.fromValue(state) ?: AraMarketListing.State.ON_SALE,
        )
    }

    class Deserializer : JsonDeserializer<AraMarketMetadataDTO?> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AraMarketMetadataDTO? {
            if (!json.isJsonObject) return null
            val obj = json.asJsonObject
            return AraMarketMetadataDTO(
                price = parsePrice(obj["price"]),
                priceCurrency = obj["price_currency"].primitiveString(),
                state = obj["state"].primitiveString(),
            )
        }

        private fun parsePrice(element: JsonElement?): Long? {
            val primitive = element?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return null
            return when {
                primitive.isNumber -> primitive.asDouble.takeIf { it.isFinite() }?.let(::truncate)
                    ?.takeIf { it >= Long.MIN_VALUE && it <= Long.MAX_VALUE }?.toLong()
                primitive.isString -> primitive.asString.filter(Char::isDigit).takeIf { it.isNotEmpty() }?.toLongOrNull()
                else -> null
            }
        }

        private fun JsonElement?.primitiveString(): String? = this?.takeIf { it.isJsonPrimitive }?.asString
    }

    companion object {
        fun fromModel(listing: AraMarketListing) = AraMarketMetadataDTO(
            price = listing.price,
            priceCurrency = listing.currency,
            state = listing.state.value,
        )
    }
}
