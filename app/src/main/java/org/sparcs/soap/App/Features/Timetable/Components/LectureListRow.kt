package org.sparcs.soap.App.Features.Timetable.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.backgroundColor
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun LectureListRow(lecture: Lecture) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(lecture.backgroundColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lecture.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
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
                Label(
                    text = lecture.classes.firstOrNull()?.location ?: stringResource(id = R.string.unknown),
                    iconPainter = painterResource(id = R.drawable.round_location_on)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (lecture.credit > 0) {
                CreditLabel(credits = lecture.credit, label = "CR")
            }
            if (lecture.creditAU > 0) {
                CreditLabel(credits = lecture.creditAU, label = "AU")
            }
        }
    }
}

@Composable
private fun Label(
    text: String,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
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
                tint = MaterialTheme.colorScheme.secondary
            )
        } else if (iconImageVector != null) {
            Icon(
                imageVector = iconImageVector,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
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
                fontFamily = FontFamily.SansSerif, // Close to iOS fontDesign(.rounded)
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

@Preview(showBackground = true)
@Composable
private fun LectureListRowPreview() {
    Theme {
        LectureListRow(lecture = Lecture.mock())
    }
}
