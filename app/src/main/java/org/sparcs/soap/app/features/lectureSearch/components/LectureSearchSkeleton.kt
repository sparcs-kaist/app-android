package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.features.lectureDetail.components.SkeletonLine
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun LectureSearchSkeleton() {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(4) { LectureSearchSkeletonCard() }
    }
}

/** Placeholder mirroring a course card: header with name and details, then one lecture row. */
@Composable
fun LectureSearchSkeletonCard() {
    val typography = MaterialTheme.typography
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(16.dp, 12.dp)) {
            SkeletonLine(typography.titleMedium, Modifier.fillMaxWidth(0.55f))
            Spacer(Modifier.height(2.dp))
            SkeletonLine(typography.bodySmall, Modifier.fillMaxWidth(0.4f))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(Modifier.padding(16.dp, 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonLine(typography.titleMedium, Modifier.fillMaxWidth(0.3f))
            SkeletonLine(typography.bodySmall, Modifier.fillMaxWidth(0.25f))
            SkeletonLine(typography.bodyMedium, Modifier.fillMaxWidth(0.5f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SkeletonLine(typography.labelLarge, Modifier.fillMaxWidth(0.5f))
                SkeletonLine(typography.labelLarge, Modifier.width(56.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureSearchSkeletonPreview() {
    Theme { LectureSearchSkeleton() }
}
