package org.sparcs.soap.App.Features.Search.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.OTL.SearchCourse
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB

@Composable
fun CourseCell(
    searchCourse: SearchCourse,
    onClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = searchCourse.name,
                maxLines = 2,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = searchCourse.code,
                    color = MaterialTheme.colorScheme.grayBB,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(searchCourse.type.displayName),
                    color = MaterialTheme.colorScheme.grayBB,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }

        if (searchCourse.summary.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = searchCourse.summary,
                maxLines = 3,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.grayBB
            )
        }
    }
}

@Composable
fun CourseSkeletonCell() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(100.dp))

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )

                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(60.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        CourseCell(
            SearchCourse.mock().copy(name = "Hello World"), {})
    }
}

@Composable
@Preview
private fun SkeletonPreview() {
    Theme {
        CourseSkeletonCell()
    }
}
