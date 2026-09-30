package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.gradeLetter
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme

enum class LectureListRowDetail { STANDARD, GRADING }

@Composable
fun LectureListRow(
    lecture: Lecture,
    modifier: Modifier = Modifier,
    detail: LectureListRowDetail = LectureListRowDetail.STANDARD,
    badge: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(LocalTimetableTheme.current.colorFor(lecture.courseID))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lecture.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (badge != null) {
                Text(badge, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (detail == LectureListRowDetail.GRADING) {
                Text("${lecture.code} · ${stringResource(lecture.type.labelRes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.credit_lecture_units, lecture.credit, lecture.creditAU),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Label(
                    text = lecture.code,
                    iconPainter = painterResource(id = R.drawable.round_school)
                )
                Label(
                    text = lecture.professors.firstOrNull()?.name ?: stringResource(id = R.string.unknown),
                    iconImageVector = Icons.Default.Person
                )
            }
        }

        if (detail == LectureListRowDetail.STANDARD) {
            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (lecture.credit > 0) {
                    CreditLabel(credits = lecture.credit, label = stringResource(R.string.cr))
                }
                if (lecture.creditAU > 0) {
                    CreditLabel(credits = lecture.creditAU, label = stringResource(R.string.au))
                }
                GradeLabel(grade = lecture.gradeLetter)
            }
        }
    }
}

@Composable
private fun Label(
    text: String,
    iconPainter: Painter? = null,
    iconImageVector: ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        } else if (iconImageVector != null) {
            Icon(
                imageVector = iconImageVector,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CreditLabel(credits: Int, label: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = credits.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun GradeLabel(grade: String) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = grade,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureListRowPreview() {
    Theme {
        LectureListRow(lecture = Lecture.mock())
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureListGradingRowPreview() {
    Theme {
        LectureListRow(Lecture.mock(), detail = LectureListRowDetail.GRADING,
            badge = stringResource(R.string.credit_retaken))
    }
}
