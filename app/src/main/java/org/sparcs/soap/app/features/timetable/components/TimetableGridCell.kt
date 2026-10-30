package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.sp
import org.sparcs.soap.app.domain.models.otl.LectureClass
import org.sparcs.soap.app.domain.models.otl.LectureItem
import org.sparcs.soap.app.domain.models.otl.subtitleTopic
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme

@Composable
fun TimetableGridCell(
    lectureItem: LectureItem,
    isCandidate: Boolean,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
    isConflict: Boolean = false,
    placement: TimetablePlacement = TimetablePlacement.View,
) {
    val theme = LocalTimetableTheme.current
    val courseColor = theme.colorFor(lectureItem.lecture.courseID)
    val backgroundColor = when {
        isCandidate -> Color.Transparent
        isConflict -> Color.Black.copy(alpha = 0.8f)
        else -> courseColor
    }
    val contentColor = when {
        isCandidate -> MaterialTheme.colorScheme.onSurface
        isConflict -> Color.White
        else -> theme.textColor
    }

    BoxWithConstraints(modifier = modifier.height(cellHeight)) {
        val metrics = TimetableCellMetrics.of(maxWidth, maxHeight)
        val titleStyle = titleStyle(metrics.isPacked)
        val detailStyle = detailStyle(metrics.isPacked)

        val title = lectureItem.lecture.name
        val subtitle = lectureItem.lecture.subtitle.subtitleTopic
        val detail = lectureItem.detailText(placement, metrics.isPacked)
        val lines = rememberCellLines(metrics, titleStyle, detailStyle, title, subtitle, detail, maxWidth, maxHeight)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor, CellShape)
                .clip(CellShape)
        ) {
            if (isCandidate) TentativeBlock(courseColor, Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding),
                verticalArrangement = Arrangement.spacedBy(lines.spacing)
            ) {
                Text(
                    text = title,
                    color = contentColor,
                    style = titleStyle,
                    maxLines = lines.title,
                    overflow = TextOverflow.Ellipsis
                )

                if (lines.room > 0) {
                    if (subtitle != null && lines.subtitle > 0) {
                        Text(
                            text = subtitle,
                            color = contentColor,
                            style = detailStyle,
                            maxLines = lines.subtitle,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = detail,
                        color = contentColor.copy(alpha = 0.8f),
                        style = detailStyle,
                        maxLines = lines.room,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun titleStyle(isPacked: Boolean): TextStyle {
    val base = MaterialTheme.typography.bodySmall.copy(lineBreak = LineBreak.Heading, fontWeight = FontWeight.Medium)
    if (!isPacked) return base
    return base.copy(fontSize = base.fontSize * PACKED_TEXT_SCALE, lineHeight = base.lineHeight * PACKED_TEXT_SCALE)
}

@Composable
private fun detailStyle(isPacked: Boolean): TextStyle {
    val base = MaterialTheme.typography.labelSmall.copy(lineBreak = LineBreak.Simple, fontWeight = FontWeight.Light)
    return if (isPacked) base.copy(letterSpacing = PACKED_LETTER_SPACING) else base
}

@Composable
private fun rememberCellLines(
    metrics: TimetableCellMetrics,
    titleStyle: TextStyle,
    detailStyle: TextStyle,
    title: String,
    subtitle: String?,
    detail: String,
    width: Dp,
    height: Dp,
): TimetableCellLines {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(metrics.isPacked, titleStyle, detailStyle, title, subtitle, detail, width, height, density) {
        textMeasurer.cellLines(metrics, titleStyle, detailStyle, title, subtitle, detail, width, height, density)
    }
}

private fun TextMeasurer.cellLines(
    metrics: TimetableCellMetrics,
    titleStyle: TextStyle,
    detailStyle: TextStyle,
    title: String,
    subtitle: String?,
    detail: String,
    width: Dp,
    height: Dp,
    density: Density,
): TimetableCellLines = with(density) {
    val constraints = Constraints(maxWidth = (width - metrics.horizontalPadding * 2).roundToPx().coerceAtLeast(1))

    val titleLineHeight = measure(title, titleStyle, maxLines = 1, constraints = constraints).size.height
    val detailLayout = measure(detail, detailStyle, constraints = constraints)
    val detailLineHeight = detailLayout.size.height / detailLayout.lineCount
    val subtitleLineCount = subtitle?.let { measure(it, detailStyle, constraints = constraints).lineCount } ?: 0

    TimetableCellLines.of(
        height = height - metrics.verticalPadding * 2,
        titleLineHeight = titleLineHeight.toDp(),
        detailLineHeight = detailLineHeight.toDp(),
        spacing = LINE_SPACING,
        maxTitleLines = metrics.titleMaxLines,
        maxSubtitleLines = minOf(metrics.subtitleMaxLines, subtitleLineCount),
        maxRoomLines = minOf(metrics.roomMaxLines, detailLayout.lineCount),
    )
}

internal fun LectureItem.detailText(placement: TimetablePlacement, isPacked: Boolean): String {
    val professor = lecture.professors.firstOrNull()?.name?.takeIf { it.isNotBlank() }
    return if (placement == TimetablePlacement.Render && professor != null) professor else lectureClass.roomLabel(isPacked)
}

internal fun LectureClass.roomLabel(isPacked: Boolean): String {
    val room = if (isPacked) roomNumber(roomName) ?: roomName else roomName
    return "($buildingCode) $room"
}

internal fun roomNumber(roomName: String): String? =
    ROOM_NUMBER.find(roomName)?.groupValues?.get(1)

private val CellShape = RoundedCornerShape(4.dp)
private val LINE_SPACING = 2.dp
private val PACKED_LETTER_SPACING = (-0.3).sp

private val ROOM_NUMBER = Regex("""^\s*\(\s*([^)]+?)\s*호\s*\)""")

@Preview(name = "Large Space", showBackground = true)
@Composable
private fun PreviewIdeal() {
    TimetableGridCell(LectureItem.mockList()[1], false, 120.dp, Modifier.width(100.dp))
}

@Preview(name = "Standard", showBackground = true)
@Composable
private fun PreviewStandard() {
    TimetableGridCell(LectureItem.mockList()[1], false, 80.dp, Modifier.width(100.dp))
}

@Preview(name = "Packed by width", showBackground = true)
@Composable
private fun PreviewPackedWidth() {
    TimetableGridCell(LectureItem.mockList()[1], false, 100.dp, Modifier.width(56.dp))
}

@Preview(name = "Tight", showBackground = true)
@Composable
private fun PreviewTight() {
    TimetableGridCell(LectureItem.mockList()[1], false, 45.dp, Modifier.width(100.dp))
}

@Preview(name = "Long room name", showBackground = true)
@Composable
private fun PreviewLongRoom() {
    val item = LectureItem.mockList()[1]
    val longRoom = item.lectureClass.copy(roomName = "(1501호)강의실-제1공동강의실")
    TimetableGridCell(item.copy(lectureClass = longRoom), false, 140.dp, Modifier.width(80.dp))
}

@Preview(name = "Shared timetable cell", showBackground = true)
@Composable
private fun PreviewRender() {
    TimetableGridCell(LectureItem.mockList()[1], false, 100.dp, Modifier.width(100.dp), placement = TimetablePlacement.Render)
}

@Preview(name = "Subtitle", showBackground = true)
@Composable
private fun PreviewSubtitle() {
    val item = LectureItem.mockList()[1]
    TimetableGridCell(item.copy(lecture = item.lecture.copy(subtitle = "<리더십 강좌>")), false, 100.dp, Modifier.width(100.dp))
}
