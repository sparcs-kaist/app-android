package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.otl.LectureItem
import org.sparcs.soap.app.domain.models.otl.backgroundColor
import org.sparcs.soap.app.domain.models.otl.textColor
import org.sparcs.soap.app.shared.mocks.otl.mockList

@Composable
fun TimetableGridCell(
    lectureItem: LectureItem,
    isCandidate: Boolean,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    
    val themeTitleStyle = MaterialTheme.typography.bodySmall.copy(
        lineBreak = LineBreak.Heading
    )
    val themeLocationStyle = MaterialTheme.typography.labelSmall.copy(
        lineBreak = LineBreak.Heading
    )
    
    val compactScale = 0.85f
    val compactTitleStyle = themeTitleStyle.copy(
        fontSize = themeTitleStyle.fontSize * compactScale,
        lineHeight = themeTitleStyle.lineHeight * compactScale
    )
    val compactLocationStyle = themeLocationStyle.copy(
        fontSize = themeLocationStyle.fontSize * compactScale,
        lineHeight = themeLocationStyle.lineHeight * compactScale
    )

    val verticalOverhead = 14.dp 
    val spacing = 2.dp

    val unitHTitle = with(density) { themeTitleStyle.fontSize.toDp() }
    val unitHLocation = with(density) { themeLocationStyle.fontSize.toDp() }
    val unitHTitleCompact = with(density) { compactTitleStyle.fontSize.toDp() }
    val unitHLocationCompact = with(density) { compactLocationStyle.fontSize.toDp() }

    val thresholdIdeal = (unitHTitle * 3) + (unitHLocation * 2) + spacing + verticalOverhead
    val thresholdStandard = (unitHTitle * 2) + unitHLocation + spacing + verticalOverhead
    val thresholdCompact = unitHTitleCompact + unitHLocationCompact + spacing + verticalOverhead

    val (titleStyle, titleMaxLines, locationStyle, locationMaxLines, showLocation) = when {
        cellHeight >= thresholdIdeal -> {
            CellLayoutConfig(themeTitleStyle, 3, themeLocationStyle, 3, true)
        }
        cellHeight >= thresholdStandard -> {
            CellLayoutConfig(themeTitleStyle, 2, themeLocationStyle, 1, true)
        }
        cellHeight >= thresholdCompact -> {
            CellLayoutConfig(compactTitleStyle, 1, compactLocationStyle, 1, true)
        }
        else -> {
            CellLayoutConfig(compactTitleStyle, 2, null, 0, false)
        }
    }

    Box(
        modifier = modifier
            .height(cellHeight)
            .background(
                if (isCandidate) MaterialTheme.colorScheme.primary else lectureItem.lecture.backgroundColor,
                RoundedCornerShape(4.dp)
            )
            .clip(RoundedCornerShape(4.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Text(
                text = lectureItem.lecture.name + lectureItem.lecture.subtitle,
                color = if (isCandidate) MaterialTheme.colorScheme.onPrimary else textColor,
                style = titleStyle,
                fontWeight = FontWeight.Medium,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis
            )

            if (showLocation && locationStyle != null) {
                Text(
                    text = "(" + lectureItem.lectureClass.buildingCode + ") " + lectureItem.lectureClass.roomName,
                    color = if (isCandidate) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.8f),
                    style = locationStyle,
                    maxLines = locationMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

private data class CellLayoutConfig(
    val titleStyle: TextStyle,
    val titleMaxLines: Int,
    val locationStyle: TextStyle?,
    val locationMaxLines: Int,
    val showLocation: Boolean
)

@Preview(name = "Large Space", showBackground = true)
@Composable
private fun PreviewIdeal() {
    TimetableGridCell(LectureItem.mockList()[1], false, 120.dp)
}

@Preview(name = "Standard", showBackground = true)
@Composable
private fun PreviewStandard() {
    TimetableGridCell(LectureItem.mockList()[1], false, 80.dp)
}

@Preview(name = "Tight", showBackground = true)
@Composable
private fun PreviewTight() {
    TimetableGridCell(LectureItem.mockList()[1], false, 45.dp)
}
