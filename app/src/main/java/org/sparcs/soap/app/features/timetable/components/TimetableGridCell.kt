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
        val layout = CellLayout.of(maxWidth, maxHeight)
        val title = lectureItem.lecture.name
        val subtitle = lectureItem.lecture.subtitle.subtitleTopic
        val detail = lectureItem.detailText(placement, layout.isPacked)
        val detailMaxLines = rememberDetailMaxLines(layout, title, subtitle, detail, maxWidth, maxHeight)

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
                    .padding(horizontal = layout.horizontalPadding, vertical = layout.verticalPadding),
                verticalArrangement = Arrangement.spacedBy(LINE_SPACING)
            ) {
                Text(
                    text = title,
                    color = contentColor,
                    style = layout.titleStyle,
                    maxLines = layout.titleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )

                if (layout.showsDetails) {
                    subtitle?.let {
                        Text(
                            text = it,
                            color = contentColor,
                            style = layout.detailStyle,
                            maxLines = layout.subtitleMaxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = detail,
                        color = contentColor.copy(alpha = 0.8f),
                        style = layout.detailStyle,
                        maxLines = detailMaxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private class CellLayout(
    val isPacked: Boolean,
    val showsDetails: Boolean,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val titleMaxLines: Int,
    val subtitleMaxLines: Int,
    val titleStyle: TextStyle,
    val detailStyle: TextStyle,
) {
    companion object {
        @Composable
        fun of(width: Dp, height: Dp): CellLayout {
            val isPacked = width < PACKED_WIDTH || height < PACKED_HEIGHT
            return CellLayout(
                isPacked = isPacked,
                showsDetails = height > DETAILS_MIN_HEIGHT,
                horizontalPadding = if (isPacked) 3.dp else 6.dp,
                verticalPadding = if (isPacked) 4.dp else 6.dp,
                titleMaxLines = if (isPacked && height < PACKED_HEIGHT) 2 else 3,
                subtitleMaxLines = if (height > SUBTITLE_FULL_HEIGHT) 3 else 1,
                titleStyle = titleStyle(isPacked),
                detailStyle = detailStyle(isPacked),
            )
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
    }
}

@Composable
private fun rememberDetailMaxLines(
    layout: CellLayout,
    title: String,
    subtitle: String?,
    detail: String,
    width: Dp,
    height: Dp,
): Int {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(layout.isPacked, title, subtitle, detail, width, height, density) {
        textMeasurer.detailMaxLines(layout, title, subtitle, detail, width, height, density)
    }
}

private fun TextMeasurer.detailMaxLines(
    layout: CellLayout,
    title: String,
    subtitle: String?,
    detail: String,
    width: Dp,
    height: Dp,
    density: Density,
): Int = with(density) {
    val constraints = Constraints(maxWidth = (width - layout.horizontalPadding * 2).roundToPx().coerceAtLeast(1))
    val spacing = LINE_SPACING.roundToPx()

    val titleHeight = measure(title, layout.titleStyle, maxLines = layout.titleMaxLines, constraints = constraints).size.height
    val subtitleHeight = subtitle?.let {
        measure(it, layout.detailStyle, maxLines = layout.subtitleMaxLines, constraints = constraints).size.height + spacing
    } ?: 0
    val detailTop = titleHeight + subtitleHeight + spacing
    val availableHeight = (height - layout.verticalPadding * 2).roundToPx()

    val detailLayout = measure(detail, layout.detailStyle, constraints = constraints)
    val fittingLines = (0 until detailLayout.lineCount).count { line ->
        detailTop + detailLayout.getLineBottom(line) <= availableHeight
    }
    fittingLines.coerceAtLeast(1)
}

internal fun LectureItem.detailText(placement: TimetablePlacement, isPacked: Boolean): String {
    val professor = lecture.professors.firstOrNull()?.name?.takeIf { it.isNotBlank() }
    return if (placement == TimetablePlacement.Render && professor != null) professor else lectureClass.roomLabel(isPacked)
}

private fun LectureClass.roomLabel(isPacked: Boolean): String {
    val room = if (isPacked) roomNumber(roomName) ?: roomName else roomName
    return "($buildingCode) $room"
}

internal fun roomNumber(roomName: String): String? =
    ROOM_NUMBER.find(roomName)?.groupValues?.get(1)

private val CellShape = RoundedCornerShape(4.dp)
private val LINE_SPACING = 2.dp

private val PACKED_WIDTH = 72.dp
private val PACKED_HEIGHT = 60.dp
private val DETAILS_MIN_HEIGHT = 40.dp
private val SUBTITLE_FULL_HEIGHT = 90.dp

private const val PACKED_TEXT_SCALE = 0.9f
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
