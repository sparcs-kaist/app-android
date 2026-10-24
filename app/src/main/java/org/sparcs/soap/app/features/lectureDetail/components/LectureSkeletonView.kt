package org.sparcs.soap.app.features.lectureDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

// Placeholders for the lecture detail and course screens. Each mirrors the section it stands in
// for, using the same text styles and paddings, so nothing jumps when it loads.

/** Mirrors the row of [LectureSummaryRow]s with separators between them. */
@Composable
fun LectureSummarySkeleton() {
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SkeletonLine(typography.titleLarge, Modifier.width(32.dp))
                SkeletonLine(typography.labelSmall, Modifier.width(48.dp))
            }
            if (index < 2) StatSeparator()
        }
    }
}

/** Mirrors [DetailSectionHeader], with an optional pill where its trailing button goes. */
@Composable
fun DetailSectionHeaderSkeleton(titleWidth: Dp = 96.dp, trailingWidth: Dp? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        SkeletonLine(MaterialTheme.typography.titleMedium, Modifier.width(titleWidth))
        Spacer(Modifier.weight(1f))
        trailingWidth?.let {
            SkeletonBlock(Modifier.size(width = it, height = 40.dp), CircleShape)
        }
    }
}

/** Mirrors [DetailInfoCard] with [rows] rows. */
@Composable
fun DetailInfoCardSkeleton(rows: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            repeat(rows) { index ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    SkeletonLine(MaterialTheme.typography.bodyMedium, Modifier.width(64.dp))
                    SkeletonLine(MaterialTheme.typography.bodyMedium, Modifier.width(120.dp))
                }
            }
        }
    }
}

/** Mirrors [ReviewStatsCard]. */
@Composable
fun ReviewStatsCardSkeleton() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(vertical = 16.dp)) { LectureSummarySkeleton() }
    }
}

/** Mirrors the reviews section: header, stats card and a few reviews. */
@Composable
fun LectureReviewsSkeleton(showsHeader: Boolean = true, showsWriteButton: Boolean = true, reviews: Int = 3) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showsHeader) {
            DetailSectionHeaderSkeleton(titleWidth = 72.dp, trailingWidth = if (showsWriteButton) 144.dp else null)
        }
        ReviewStatsCardSkeleton()
        Column(modifier = Modifier.padding(top = 6.dp)) {
            repeat(reviews) { LectureReviewSkeletonCell() }
        }
    }
}

/** A bar as tall as the font, centered in a box one line of [style] tall. */
@Composable
internal fun SkeletonLine(style: TextStyle, modifier: Modifier) {
    val density = LocalDensity.current
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier.height(with(density) { style.lineHeight.toDp() })
    ) {
        SkeletonBlock(
            Modifier
                .fillMaxWidth()
                .height(with(density) { style.fontSize.toDp() })
        )
    }
}

@Composable
internal fun SkeletonBlock(modifier: Modifier, shape: Shape = RoundedCornerShape(4.dp)) {
    // Tinted rather than a container color, so it shows both on the page and on a card.
    Box(modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), shape))
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            LectureSummarySkeleton()
            DetailSectionHeaderSkeleton()
            DetailInfoCardSkeleton(rows = 4)
            LectureReviewsSkeleton()
        }
    }
}
