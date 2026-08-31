package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
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
    val textMeasurer = rememberTextMeasurer()

    val themeTitleStyle = MaterialTheme.typography.bodySmall.copy(
        lineBreak = LineBreak.Heading
    )
    val themeLocationStyle = MaterialTheme.typography.labelSmall.copy(
        lineBreak = LineBreak.Heading
    )

    val compactScale = 0.85f
    val compactTitleStyle = themeTitleStyle.copy(
        fontSize = themeTitleStyle.fontSize * compactScale,
        lineHeight = if (themeTitleStyle.lineHeight.isSpecified) {
            themeTitleStyle.lineHeight * compactScale
        } else {
            themeTitleStyle.lineHeight
        }
    )
    val compactLocationStyle = themeLocationStyle.copy(
        fontSize = themeLocationStyle.fontSize * compactScale,
        lineHeight = if (themeLocationStyle.lineHeight.isSpecified) {
            themeLocationStyle.lineHeight * compactScale
        } else {
            themeLocationStyle.lineHeight
        }
    )

    val verticalPadding = 12.dp
    val spacing = 2.dp

    BoxWithConstraints(
        modifier = modifier
            .height(cellHeight)
    ) {
        val availableTextWidthPx = with(density) {
            (maxWidth - 10.dp).toPx().toInt().coerceAtLeast(0)
        }

        val titleText = lectureItem.lecture.name + lectureItem.lecture.subtitle
        val locationText = "(" + lectureItem.lectureClass.buildingCode + ") " + lectureItem.lectureClass.roomName

        val layoutConfig = remember(
            titleText,
            locationText,
            availableTextWidthPx,
            cellHeight,
            themeTitleStyle,
            themeLocationStyle,
            compactTitleStyle,
            compactLocationStyle
        ) {
            pickBestLayout(
                textMeasurer = textMeasurer,
                density = density,
                textWidthPx = availableTextWidthPx,
                titleText = titleText,
                locationText = locationText,
                cellHeight = cellHeight,
                verticalPadding = verticalPadding,
                spacing = spacing,
                normalTitleStyle = themeTitleStyle,
                normalLocationStyle = themeLocationStyle,
                compactTitleStyle = compactTitleStyle,
                compactLocationStyle = compactLocationStyle
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    text = titleText,
                    color = if (isCandidate) MaterialTheme.colorScheme.onPrimary else textColor,
                    style = layoutConfig.titleStyle,
                    fontWeight = FontWeight.Medium,
                    maxLines = layoutConfig.titleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )

                if (layoutConfig.showLocation && layoutConfig.locationStyle != null) {
                    Text(
                        text = locationText,
                        color = if (isCandidate) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.8f),
                        style = layoutConfig.locationStyle,
                        maxLines = layoutConfig.locationMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

private data class CellLayoutConfig(
    val titleStyle: TextStyle,
    val titleMaxLines: Int,
    val locationStyle: TextStyle?,
    val locationMaxLines: Int,
    val showLocation: Boolean = locationStyle != null
)

private fun pickBestLayout(
    textMeasurer: TextMeasurer,
    density: Density,
    textWidthPx: Int,
    titleText: String,
    locationText: String,
    cellHeight: Dp,
    verticalPadding: Dp,
    spacing: Dp,
    normalTitleStyle: TextStyle,
    normalLocationStyle: TextStyle,
    compactTitleStyle: TextStyle,
    compactLocationStyle: TextStyle,
): CellLayoutConfig {
    val normalTitleLineCount = measureLineCount(textMeasurer, titleText, normalTitleStyle, textWidthPx)
    val normalLocationLineCount = measureLineCount(textMeasurer, locationText, normalLocationStyle, textWidthPx)
    val compactTitleLineCount = measureLineCount(textMeasurer, titleText, compactTitleStyle, textWidthPx)
    val compactLocationLineCount = measureLineCount(textMeasurer, locationText, compactLocationStyle, textWidthPx)

    val fullNormalLayout = CellLayoutConfig(normalTitleStyle, normalTitleLineCount, normalLocationStyle, normalLocationLineCount)
    if (cellHeight >= requiredHeight(
            textMeasurer = textMeasurer,
            density = density,
            textWidthPx = textWidthPx,
            titleText = titleText,
            locationText = locationText,
            titleStyle = fullNormalLayout.titleStyle,
            titleLines = fullNormalLayout.titleMaxLines,
            locationStyle = fullNormalLayout.locationStyle,
            locationLines = fullNormalLayout.locationMaxLines,
            verticalPadding = verticalPadding,
            spacing = spacing
        )) {
        return fullNormalLayout
    }

    val fullCompactLayout = CellLayoutConfig(compactTitleStyle, compactTitleLineCount, compactLocationStyle, compactLocationLineCount)
    if (cellHeight >= requiredHeight(
            textMeasurer = textMeasurer,
            density = density,
            textWidthPx = textWidthPx,
            titleText = titleText,
            locationText = locationText,
            titleStyle = fullCompactLayout.titleStyle,
            titleLines = fullCompactLayout.titleMaxLines,
            locationStyle = fullCompactLayout.locationStyle,
            locationLines = fullCompactLayout.locationMaxLines,
            verticalPadding = verticalPadding,
            spacing = spacing
        )) {
        return fullCompactLayout
    }

    val candidates = buildList {
        add(CellLayoutConfig(normalTitleStyle, minOf(normalTitleLineCount, 2), normalLocationStyle, minOf(normalLocationLineCount, 2)))
        add(CellLayoutConfig(normalTitleStyle, minOf(normalTitleLineCount, 2), normalLocationStyle, 1))
        add(CellLayoutConfig(normalTitleStyle, 1, normalLocationStyle, 1))
        add(CellLayoutConfig(compactTitleStyle, minOf(compactTitleLineCount, 2), compactLocationStyle, minOf(compactLocationLineCount, 2)))
        add(CellLayoutConfig(compactTitleStyle, minOf(compactTitleLineCount, 2), compactLocationStyle, 1))
        add(CellLayoutConfig(compactTitleStyle, 1, compactLocationStyle, 1))
        add(CellLayoutConfig(normalTitleStyle, 2, null, 0))
        add(CellLayoutConfig(normalTitleStyle, 1, null, 0))
        add(CellLayoutConfig(compactTitleStyle, 2, null, 0))
        add(CellLayoutConfig(compactTitleStyle, 1, null, 0))
    }

    return candidates.firstOrNull { option ->
        cellHeight >= requiredHeight(
            textMeasurer = textMeasurer,
            density = density,
            textWidthPx = textWidthPx,
            titleText = titleText,
            locationText = locationText,
            titleStyle = option.titleStyle,
            titleLines = option.titleMaxLines,
            locationStyle = option.locationStyle,
            locationLines = option.locationMaxLines,
            verticalPadding = verticalPadding,
            spacing = spacing
        )
    } ?: CellLayoutConfig(compactTitleStyle, 1, null, 0)
}

private fun requiredHeight(
    textMeasurer: TextMeasurer,
    density: Density,
    textWidthPx: Int,
    titleText: String,
    locationText: String,
    titleStyle: TextStyle,
    titleLines: Int,
    locationStyle: TextStyle?,
    locationLines: Int,
    verticalPadding: Dp,
    spacing: Dp,
): Dp {
    val titleHeight = measureTextHeight(textMeasurer, titleText, titleStyle, titleLines, textWidthPx, density)
    val locationHeight = locationStyle?.let {
        measureTextHeight(textMeasurer, locationText, it, locationLines, textWidthPx, density)
    } ?: 0.dp
    val locationGap = if (locationStyle != null && locationLines > 0) spacing else 0.dp

    return verticalPadding + titleHeight + locationGap + locationHeight
}

private fun measureLineCount(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    widthPx: Int,
): Int {
    if (widthPx <= 0) return 1

    return textMeasurer.measure(
        text = AnnotatedString(text),
        style = style,
        maxLines = Int.MAX_VALUE,
        overflow = TextOverflow.Clip,
        constraints = Constraints(maxWidth = widthPx)
    ).lineCount.coerceAtLeast(1)
}

private fun measureTextHeight(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    maxLines: Int,
    widthPx: Int,
    density: Density,
): Dp {
    if (widthPx <= 0) return 0.dp

    val result = textMeasurer.measure(
        text = AnnotatedString(text),
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        constraints = Constraints(maxWidth = widthPx)
    )

    return with(density) { result.size.height.toDp() }
}

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
