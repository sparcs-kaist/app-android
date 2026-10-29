package org.sparcs.soap.app.features.postList.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TopicFilterBar(
    topics: List<AraBoardTopic>,
    selection: AraBoardTopic?,
    onSelect: (AraBoardTopic?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    if (topics.isEmpty()) return
    val haptic = LocalHapticFeedback.current
    fun select(topic: AraBoardTopic?) {
        if (topic == selection) return
        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
        onSelect(topic)
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        item(key = "all") {
            FilterChip(selected = selection == null, onClick = { select(null) }, label = { Text(stringResource(R.string.all)) })
        }
        items(topics, key = { it.id }) { topic ->
            FilterChip(selected = selection == topic, onClick = { select(topic) }, label = { Text(topic.name.localized()) })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val topics = listOf("판매", "구매", "나눔").mapIndexed { index, name ->
        AraBoardTopic(index, name, LocalizedString(mapOf("ko" to name, "en" to name)))
    }
    Theme { TopicFilterBar(topics, selection = topics[1], onSelect = {}) }
}
