package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun LectureSearchSkeleton() {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        userScrollEnabled = false,
    ) {
        items(4) { LectureSearchSkeletonCard() }
    }
}

@Composable
fun LectureSearchSkeletonCard() {
    val placeholder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = LectureSearchChrome.card,
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth(0.65f).height(22.dp).background(placeholder, RoundedCornerShape(4.dp)))
            Box(Modifier.fillMaxWidth(0.4f).height(14.dp).background(placeholder, RoundedCornerShape(4.dp)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).padding(end = 32.dp).height(18.dp).background(placeholder, RoundedCornerShape(4.dp)))
                Box(Modifier.size(24.dp).background(placeholder, RoundedCornerShape(6.dp)))
            }
            Box(Modifier.fillMaxWidth(0.7f).height(16.dp).background(placeholder, RoundedCornerShape(4.dp)))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.fillMaxWidth(0.45f).height(14.dp).background(placeholder, RoundedCornerShape(4.dp)))
                Box(Modifier.size(52.dp, 14.dp).background(placeholder, RoundedCornerShape(4.dp)))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureSearchSkeletonPreview() {
    Theme { LectureSearchSkeleton() }
}
