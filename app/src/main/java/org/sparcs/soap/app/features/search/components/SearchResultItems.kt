package org.sparcs.soap.app.features.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.lectureDetail.components.SkeletonLine
import org.sparcs.soap.app.features.postList.components.postListRow.title
import org.sparcs.soap.app.shared.extensions.relativeTimeString
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote

// Rows of the search results. Each is a ListItem on the section's OutlinedCard, so the container
// is transparent.

private val itemColors
    @Composable get() = ListItemDefaults.colors(containerColor = Color.Transparent)

/** Code and type above the course name, then its summary. */
@Composable
fun CourseCell(course: CourseSummary, onClick: () -> Unit) {
    ListItem(
        overlineContent = { Text("${course.code} · ${stringResource(course.type.labelRes)}") },
        headlineContent = { Text(course.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = if (course.summary.isNotEmpty()) {
            { Text(course.summary, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        } else null,
        trailingContent = if (course.completed) {
            {
                Text(
                    text = stringResource(R.string.course_taken),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        colors = itemColors,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/** Title with the topic in front, then author, views and age; votes and comments at the end. */
@Composable
fun SearchPostItem(post: AraPost, onClick: () -> Unit) {
    val voteCount = post.upVotes - post.downVotes
    val primary = MaterialTheme.colorScheme.primary
    val postTitle = title(post) { stringResource(it) }
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildAnnotatedString {
                        post.topic?.name?.localized()?.let { topic ->
                            withStyle(SpanStyle(color = primary)) { append("[$topic] ") }
                        }
                        append(postTitle)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (post.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (post.attachmentType == AraPost.AttachmentType.IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                    AttachmentIcon(Icons.Outlined.Image)
                }
                if (post.attachmentType == AraPost.AttachmentType.NON_IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                    AttachmentIcon(Icons.Outlined.AttachFile)
                }
            }
        },
        supportingContent = {
            Text(
                text = listOf(
                    post.author.profile.nickname,
                    stringResource(R.string.views, post.views),
                    post.createdAt.timeAgoDisplay()
                ).joinToString(" · "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = if (voteCount != 0 || post.commentCount > 0) {
            {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (voteCount != 0) {
                        CountLabel(
                            icon = if (voteCount > 0) Icons.Outlined.ThumbUp else Icons.Outlined.ThumbDown,
                            text = voteCount.toString().replace('-', '−'),
                            color = if (voteCount > 0) upvote else downvote
                        )
                    }
                    if (post.commentCount > 0) {
                        CountLabel(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            text = post.commentCount.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else null,
        colors = itemColors,
        modifier = Modifier.clickable(enabled = !post.isHidden, onClick = onClick)
    )
}

/** Route, departure and room name, with the number of riders at the end. */
@Composable
fun SearchTaxiRoomItem(room: TaxiRoom, onClick: () -> Unit) {
    ListItem(
        leadingContent = { Icon(Icons.Outlined.LocalTaxi, contentDescription = null) },
        headlineContent = {
            Text(
                text = "${room.source.title.localized()} → ${room.destination.title.localized()}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${room.departAt.relativeTimeString()} · ${room.title}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Outlined.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("${room.participants.size}/${room.capacity}", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        colors = itemColors,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * Placeholder with the same text styles and paddings as the rows above: an optional overline,
 * the headline, [supportingLines] lines of supporting text and an optional leading icon.
 */
@Composable
fun SearchItemSkeleton(overline: Boolean = false, supportingLines: Int = 1, leading: Boolean = false) {
    val typography = MaterialTheme.typography
    ListItem(
        leadingContent = if (leading) {
            {
                Box(
                    Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                )
            }
        } else null,
        overlineContent = if (overline) {
            { SkeletonLine(typography.labelSmall, Modifier.width(96.dp)) }
        } else null,
        headlineContent = { SkeletonLine(typography.bodyLarge, Modifier.fillMaxWidth(0.6f)) },
        supportingContent = if (supportingLines > 0) {
            {
                Column {
                    repeat(supportingLines) { line ->
                        SkeletonLine(
                            typography.bodyMedium,
                            Modifier.fillMaxWidth(if (line == supportingLines - 1) 0.5f else 1f)
                        )
                    }
                }
            }
        } else null,
        colors = itemColors
    )
}

@Composable
private fun AttachmentIcon(icon: ImageVector) {
    Spacer(Modifier.width(4.dp))
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
}

@Composable
private fun CountLabel(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        OutlinedCard(Modifier.padding(16.dp)) {
            CourseCell(CourseSummary.mockList().first()) {}
            HorizontalDivider()
            SearchPostItem(AraPost.mockList().first()) {}
            HorizontalDivider()
            SearchTaxiRoomItem(TaxiRoom.mockList().first()) {}
            HorizontalDivider()
            SearchItemSkeleton(overline = true, supportingLines = 2)
            HorizontalDivider()
            SearchItemSkeleton(leading = true)
        }
    }
}
