package org.sparcs.soap.Widgets.AraPortalWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.sparcs.soap.Widgets.AraPortalWidget.WidgetNoticeEntry
import org.sparcs.soap.Widgets.theme.ui.TimetableWidgetTheme.grayBB

@Composable
fun NoticeRow(
    entry: WidgetNoticeEntry,
    onClick: Action,
) {
    val context = LocalContext.current
    Column {
        Spacer(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GlanceTheme.colors.grayBB.getColor(context).copy(alpha = 0.5f))
        )

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clickable(onClick)
        ) {
            Text(
                text = entry.title,
                maxLines = 1,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            ) {
                Image(
                    provider = ImageProvider(entry.iconResId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(grayBB),
                    modifier = GlanceModifier
                        .size(16.dp)
                        .padding(end = 6.dp)
                )

                Text(
                    text = entry.displayBoardName,
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = grayBB
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = entry.author,
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = grayBB
                    ),
                )
            }
        }
    }
}