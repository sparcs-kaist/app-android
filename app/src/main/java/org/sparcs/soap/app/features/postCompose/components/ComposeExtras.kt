package org.sparcs.soap.app.features.postCompose.components

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import org.sparcs.soap.app.shared.extensions.fileTypeIcon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.features.postCompose.ComposeFile
import org.sparcs.soap.app.features.postCompose.PriceInput
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun MarketPriceSection(
    price: Long?,
    onPriceChange: (Long?) -> Unit,
    saleState: AraMarketListing.State,
    onSaleStateChange: (AraMarketListing.State) -> Unit,
    showsSaleState: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onNext: () -> Unit = {},
) {
    val input = remember { PriceInput() }
    var field by remember { mutableStateOf(input.field(price)) }
    if (input.text(price) != field.text) field = input.field(price)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = field,
            onValueChange = { edited ->
                val (value, formatted) = input.edit(field, edited)
                field = formatted
                if (value != price) onPriceChange(value)
            },
            label = { Text(stringResource(R.string.market_price)) },
            suffix = { Text("₩") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        if (showsSaleState) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SALE_STATES.forEach { (state, label) ->
                    FilterChip(
                        selected = saleState == state,
                        onClick = { onSaleStateChange(state) },
                        label = { Text(stringResource(label)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ComposeFileList(files: List<ComposeFile>, onRemove: (ComposeFile) -> Unit, modifier: Modifier = Modifier) {
    if (files.isEmpty()) return
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        files.forEach { file ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                ListItem(
                    leadingContent = { Icon(fileTypeIcon(file.mimeType, file.name), contentDescription = null) },
                    headlineContent = { Text(file.name, maxLines = 1, overflow = TextOverflow.MiddleEllipsis) },
                    supportingContent = if (file.size > 0) { { Text(Formatter.formatShortFileSize(context, file.size)) } } else null,
                    trailingContent = {
                        IconButton(onClick = { onRemove(file) }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

private val SALE_STATES = listOf(
    AraMarketListing.State.ON_SALE to R.string.market_state_on_sale,
    AraMarketListing.State.RESERVED to R.string.market_state_reserved,
    AraMarketListing.State.SOLD_OUT to R.string.market_state_sold_out,
)

@Preview(showBackground = true)
@Composable
private fun PricePreview() {
    Theme {
        MarketPriceSection(
            price = 15000,
            onPriceChange = {},
            saleState = AraMarketListing.State.RESERVED,
            onSaleStateChange = {},
            showsSaleState = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilesPreview() {
    Theme {
        ComposeFileList(
            files = listOf(ComposeFile("계약서.pdf", 482_000, "application/pdf", ComposeFile.Source.Existing(1))),
            onRemove = {}
        )
    }
}
