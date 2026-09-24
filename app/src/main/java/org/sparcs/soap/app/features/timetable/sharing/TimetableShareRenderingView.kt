package org.sparcs.soap.app.features.timetable.sharing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.features.timetable.components.TimetablePlacement
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme

@Composable
fun TimetableShareRenderingView(semester: Semester, timetable: Timetable, theme: TimetableTheme) {
    TimetableShareCard(theme, timetable, stringResource(R.string.timetable), semester.description,
        stringResource(R.string.timetable_share_credits), timetable.credits.toString())
}

@Composable
internal fun TimetableShareCard(
    theme: TimetableTheme, timetable: Timetable, label: String, title: String,
    detailLabel: String, detail: String, isCode: Boolean = false,
) {
    CompositionLocalProvider(LocalTimetableTheme provides theme) {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(
                    Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(36.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(Modifier.fillMaxWidth().weight(1f)
                        .then(if (theme.backgroundColor == null) Modifier
                            .dropShadow(RoundedCornerShape(28.dp), Shadow(
                                radius = 16.dp, color = Color.Black.copy(alpha = .12f), offset = DpOffset(0.dp, 8.dp)))
                            .dropShadow(RoundedCornerShape(28.dp), Shadow(
                                radius = 2.dp, color = Color.Black.copy(alpha = .06f), offset = DpOffset(0.dp, 1.dp)))
                            else Modifier)
                        .background(theme.backgroundColor ?: Color.White, RoundedCornerShape(28.dp)).padding(12.dp)) {
                        TimetableGrid(timetable = timetable, placement = TimetablePlacement.Render)
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(painterResource(R.drawable.ic_buddy_icon), stringResource(R.string.share_brand), Modifier.size(32.dp))
                        Column(Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(detailLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            Text(detail, fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default, style = MaterialTheme.typography.titleMedium, color = Color(0xFF0F172A))
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Preview(widthDp = 390, heightDp = 844, locale = "ko")
@Composable
private fun TimetableShareRenderingPreview() {
    TimetableShareRenderingView(Semester.mockList().first(), Timetable.mock(), TimetableTheme.Default)
}
