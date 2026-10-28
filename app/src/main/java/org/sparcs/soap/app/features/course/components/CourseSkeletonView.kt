package org.sparcs.soap.app.features.course.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.features.lectureDetail.components.DetailInfoCardSkeleton
import org.sparcs.soap.app.features.lectureDetail.components.DetailSectionHeaderSkeleton
import org.sparcs.soap.app.features.lectureDetail.components.LectureReviewsSkeleton
import org.sparcs.soap.app.features.lectureDetail.components.LectureSummarySkeleton
import org.sparcs.soap.app.shared.extensions.bleedHorizontally
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
import org.sparcs.soap.app.theme.ui.Theme


/** Mirrors [org.sparcs.soap.app.features.course.CourseSummary]: stats, header and info card. */
@Composable
fun CourseSummarySkeleton() {
    Column(modifier = Modifier.fillMaxWidth()) {
        LectureSummarySkeleton()
        Spacer(modifier = Modifier.height(8.dp))
        DetailSectionHeaderSkeleton()
        DetailInfoCardSkeleton(rows = 4)
    }
}

/** The reviews below the course's reviews header, which stays visible while they load. */
@Composable
fun CourseReviewSectionSkeleton() {
    LectureReviewsSkeleton(showsHeader = false, reviews = 2)
}

@Composable
@Preview
private fun Preview() {
    Theme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CourseSummarySkeleton()
            CourseReviewSectionSkeleton()
        }
    }
}
/** Mirrors [CourseHistorySection]: header, then a row of semester cards. */
@Composable
fun CourseHistorySkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailSectionHeaderSkeleton(titleWidth = 72.dp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .bleedHorizontally(16.dp)
                .horizontalScroll(rememberScrollState(), enabled = false)
                .padding(horizontal = 16.dp)
        ) {
            repeat(3) {
                Card(modifier = Modifier.width(190.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        SkeletonLine(MaterialTheme.typography.titleSmall, Modifier.width(96.dp))
                        Spacer(Modifier.height(8.dp))
                        repeat(2) {
                            Box(Modifier.height(48.dp), contentAlignment = Alignment.CenterStart) {
                                SkeletonLine(MaterialTheme.typography.bodyMedium, Modifier.width(120.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseHistorySkeletonPreview() {
    Theme { CourseHistorySkeleton() }
}
