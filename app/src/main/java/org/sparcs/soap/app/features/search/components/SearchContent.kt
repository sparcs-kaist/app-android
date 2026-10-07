package org.sparcs.soap.app.features.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.UnavailableView
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomCell
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun <T : Any> SearchContent(
    results: List<T>,
    key: (T) -> Any,
    onLoadMore: (() -> Unit)? = null,
    cell: @Composable (T) -> Unit,
) {
    if (onLoadMore != null && results.isNotEmpty()) {
        LaunchedEffect(results.size) { onLoadMore() }
    }

    if (results.isEmpty()) {
        UnavailableView(
            icon = Icons.Rounded.ReportProblem,
            title = stringResource(R.string.no_results),
            description = stringResource(R.string.no_results),
        )
    } else {
        Column {
            results.forEachIndexed { index, item ->
                key(key(item)) {
                    Column {
                        cell(item)
                        if (index != results.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        SearchContent(
            results = TaxiRoom.mockList(),
            key = { it.id },
            cell = {
                TaxiRoomCell(
                    room = it,
                    onClick = {}
                )
            }
        )
    }
}