package org.sparcs.soap.app.features.timetable.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal class TimetableCellMetrics private constructor(
    val isPacked: Boolean,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val titleMaxLines: Int,
    val subtitleMaxLines: Int,
    val roomMaxLines: Int,
) {
    companion object {
        fun of(width: Dp, height: Dp): TimetableCellMetrics {
            val isPacked = width < PACKED_WIDTH || height < PACKED_HEIGHT
            return TimetableCellMetrics(
                isPacked = isPacked,
                horizontalPadding = if (isPacked) 3.dp else 6.dp,
                verticalPadding = if (isPacked) 4.dp else 6.dp,
                titleMaxLines = if (isPacked && height < PACKED_HEIGHT) 2 else 3,
                subtitleMaxLines = if (height > SUBTITLE_FULL_HEIGHT) 3 else 1,
                roomMaxLines = if (isPacked) 1 else 2,
            )
        }
    }
}

internal class TimetableCellLines private constructor(
    val title: Int,
    val subtitle: Int,
    val room: Int,
    val spacing: Dp,
) {
    companion object {
        fun of(
            height: Dp,
            titleLineHeight: Dp,
            detailLineHeight: Dp,
            spacing: Dp,
            maxTitleLines: Int,
            maxSubtitleLines: Int,
            maxRoomLines: Int,
        ): TimetableCellLines {
            var spare = height - titleLineHeight

            fun titleLines(): Int {
                if (titleLineHeight <= 0.dp) return 1
                return (1 + (spare / titleLineHeight).toInt()).coerceIn(1, maxTitleLines.coerceAtLeast(1))
            }

            if (maxRoomLines <= 0 || detailLineHeight <= 0.dp || spare < detailLineHeight) {
                return TimetableCellLines(title = titleLines(), subtitle = 0, room = 0, spacing = spacing)
            }

            val roomSpacing = minOf(spacing, spare - detailLineHeight)
            spare -= roomSpacing + detailLineHeight

            fun take(maxLines: Int, gap: Dp): Int {
                val count = ((spare - gap) / detailLineHeight).toInt().coerceIn(0, maxLines.coerceAtLeast(0))
                if (count > 0) spare -= gap + detailLineHeight * count
                return count
            }

            val subtitle = take(maxSubtitleLines, roomSpacing)
            val room = 1 + take(maxRoomLines - 1, 0.dp)
            return TimetableCellLines(title = titleLines(), subtitle = subtitle, room = room, spacing = roomSpacing)
        }
    }
}

internal const val PACKED_TEXT_SCALE = 0.9f

private val PACKED_WIDTH = 72.dp
private val PACKED_HEIGHT = 60.dp
private val SUBTITLE_FULL_HEIGHT = 90.dp
