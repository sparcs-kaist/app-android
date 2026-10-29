package org.sparcs.soap.app.features.postList.components.postListRow

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.theme.ui.Theme
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun MarketListingLabel(listing: AraMarketListing, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = marketPriceText(listing),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (listing.price == null || listing.state == AraMarketListing.State.SOLD_OUT) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface
        )
        if (listing.state != AraMarketListing.State.ON_SALE) {
            Text(
                text = stringResource(
                    if (listing.state == AraMarketListing.State.RESERVED) R.string.market_state_reserved
                    else R.string.market_state_sold_out
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun marketPriceText(listing: AraMarketListing): String =
    listing.priceLabel(LocalContext.current.resources, LocalConfiguration.current.locales[0])

fun AraMarketListing.priceLabel(resources: Resources, locale: Locale): String = when (val amount = price) {
    null -> resources.getString(R.string.market_price_none)
    0L -> resources.getString(R.string.market_price_free)
    else -> formatPrice(amount, currency, locale)
}

fun AraMarketListing.formattedPrice(locale: Locale = Locale.getDefault()): String? =
    price?.let { formatPrice(it, currency, locale) }

private fun formatPrice(amount: Long, currency: String, locale: Locale): String {
    val number = NumberFormat.getIntegerInstance(locale).format(amount)
    if (currency == AraMarketListing.DEFAULT_CURRENCY) {
        return if (locale.language == Locale.KOREAN.language) "${number}원" else "₩$number"
    }
    val code = runCatching { Currency.getInstance(currency) }.getOrNull() ?: return "$number $currency"
    return NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = code
        maximumFractionDigits = 0
    }.format(amount)
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Column(Modifier.padding(16.dp)) {
            MarketListingLabel(AraMarketListing(price = 15_000))
            MarketListingLabel(AraMarketListing(price = 2_500_000, state = AraMarketListing.State.RESERVED))
            MarketListingLabel(AraMarketListing(price = 8_000, state = AraMarketListing.State.SOLD_OUT))
            MarketListingLabel(AraMarketListing(price = 0))
            MarketListingLabel(AraMarketListing(price = null))
        }
    }
}
