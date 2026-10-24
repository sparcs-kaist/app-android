@file:OptIn(ExperimentalMaterial3Api::class)

package org.sparcs.soap.materialpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WhereToVote
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.feed.FeedVoteType
import org.sparcs.soap.app.domain.enums.otl.LectureType
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.SectionLabel
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.postList.components.postListRow.title
import org.sparcs.soap.app.features.timetable.TimetableLoadState
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditCalculationViewState
import org.sparcs.soap.app.features.timetable.creditCalculation.creditPreviewState
import org.sparcs.soap.app.shared.extensions.relativeTimeString
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.formatters.creditProgress
import org.sparcs.soap.app.shared.formatters.formatGPA
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.mocks.feed.mock
import org.sparcs.soap.app.shared.mocks.feed.mockList
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel
import java.text.DateFormat
import java.util.Date

// Material 3 "after" mockups for surfaces that currently use Modifier.glassBorder and custom grays.
// Each preview is paired with the original via the `// before:` comment above it.

// region Shared helpers

@Composable
private fun PreviewFrame(
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Theme {
        Surface {
            Column(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun transparentListItemColors() =
    ListItemDefaults.colors(containerColor = Color.Transparent)

/** Non-interactive tonal status label (M3 has no dedicated "status pill"; this mirrors the chip/badge shape). */
@Composable
private fun TonalLabel(
    text: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// endregion

// region Post actions

@Composable
private fun MaterialVoteGroup(
    myVote: Boolean?,
    votes: Int,
    onUpVote: () -> Unit = {},
    onDownVote: () -> Unit = {},
) {
    val countColor = when (myVote) {
        true -> upvote
        false -> downvote
        null -> MaterialTheme.colorScheme.onSurface
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconToggleButton(
            checked = myVote == true,
            onCheckedChange = { onUpVote() },
        ) {
            Icon(
                Icons.Filled.ArrowUpward,
                contentDescription = "UpVote",
                tint = if (myVote == true) upvote else LocalContentColor.current,
            )
        }
        Text(
            text = votes.toString().replace('-', '−'),
            style = MaterialTheme.typography.labelLarge,
            color = countColor,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        FilledTonalIconToggleButton(
            checked = myVote == false,
            onCheckedChange = { onDownVote() },
        ) {
            Icon(
                Icons.Filled.ArrowDownward,
                contentDescription = "DownVote",
                tint = if (myVote == false) downvote else LocalContentColor.current,
            )
        }
    }
}

@Composable
private fun MaterialCommentChip(commentCount: Int, onClick: () -> Unit = {}) {
    AssistChip(
        onClick = onClick,
        label = { Text("$commentCount") },
        leadingIcon = {
            Icon(
                Icons.AutoMirrored.Outlined.Comment,
                contentDescription = "Comments",
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
    )
}

@Composable
private fun MaterialBookmarkButton(initiallyBookmarked: Boolean) {
    var bookmarked by remember { mutableStateOf(initiallyBookmarked) }
    FilledTonalIconToggleButton(checked = bookmarked, onCheckedChange = { bookmarked = it }) {
        Icon(
            if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = stringResource(R.string.to_bookmark),
        )
    }
}

@Composable
private fun MaterialShareButton() {
    IconButton(onClick = {}) {
        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share))
    }
}

// before: features/post/components/PostVoteButton.kt#Preview
@Preview(name = "PostVoteButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostVoteButton() {
    PreviewFrame { MaterialVoteGroup(myVote = true, votes = 2) }
}

// before: features/post/components/PostVoteButton.kt#Preview (extra states: none / downvoted / negative)
@Preview(name = "PostVoteButton_States", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostVoteButton_States() {
    PreviewFrame {
        MaterialVoteGroup(myVote = null, votes = 0)
        MaterialVoteGroup(myVote = false, votes = -3)
    }
}

// before: features/post/components/PostCommentButton.kt#Preview
@Preview(name = "PostCommentButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostCommentButton() {
    var comment by remember { mutableStateOf(1) }
    PreviewFrame {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MaterialCommentChip(comment) { comment += 1 }
            MaterialCommentChip(comment) { comment += 1 }
        }
    }
}

// before: features/post/components/PostBookmarkButton.kt#Preview
@Preview(name = "PostBookmarkButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostBookmarkButton() {
    PreviewFrame { MaterialBookmarkButton(initiallyBookmarked = true) }
}

// before: features/post/components/PostShareButton.kt#Preview
@Preview(name = "PostShareButton", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostShareButton() {
    PreviewFrame { MaterialShareButton() }
}

// before: (composite, no single original preview) PostVoteButton + PostCommentButton + PostBookmarkButton + PostShareButton as used in the post view footer
@Preview(name = "PostActionRow", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostActionRow() {
    PreviewFrame {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MaterialVoteGroup(myVote = true, votes = 2)
            MaterialCommentChip(1)
            Spacer(Modifier.weight(1f))
            MaterialBookmarkButton(initiallyBookmarked = true)
            MaterialShareButton()
        }
    }
}

// endregion

// region Taxi

@Composable
private fun MaterialTaxiParticipantsIndicator(participants: Int, capacity: Int) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (capacity - participants) {
        0 -> scheme.errorContainer to scheme.onErrorContainer
        1 -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        else -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
    TonalLabel("$participants/$capacity", container, content, icon = Icons.Filled.Group)
}

@Composable
private fun MaterialTaxiRoomStatusIndicator(
    settlementType: TaxiParticipant.SettlementType,
    settlementCount: Int,
    participantsCount: Int,
) {
    val scheme = MaterialTheme.colorScheme
    val complete = settlementCount >= participantsCount
    val (container, content) = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) scheme.secondaryContainer to scheme.onSecondaryContainer
            else scheme.primaryContainer to scheme.onPrimaryContainer
        TaxiParticipant.SettlementType.PaymentRequired -> scheme.errorContainer to scheme.onErrorContainer
        TaxiParticipant.SettlementType.PaymentSent -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
    val text = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> stringResource(R.string.request_settlement)
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) stringResource(R.string.settlement_completed) else stringResource(R.string.request_settlement)
        TaxiParticipant.SettlementType.PaymentRequired -> stringResource(R.string.payment_required)
        TaxiParticipant.SettlementType.PaymentSent -> stringResource(R.string.payment_settled)
    }
    TonalLabel(text, container, content)
}

@Composable
private fun RouteLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MaterialTaxiRoomContent(
    room: TaxiRoom,
    taxiUser: TaxiUser?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RouteLine(Icons.Filled.NearMe, room.source.title.localized())
                RouteLine(Icons.Filled.WhereToVote, room.destination.title.localized())
            }
            if (room.isDeparted) {
                MaterialTaxiRoomStatusIndicator(
                    settlementType = room.participants.firstOrNull { it.id == taxiUser?.oid }?.isSettlement
                        ?: TaxiParticipant.SettlementType.NotDeparted,
                    settlementCount = room.settlementTotal ?: 0,
                    participantsCount = room.participants.size,
                )
            } else {
                MaterialTaxiParticipantsIndicator(room.participants.size, room.capacity)
            }
        }
        Text(
            text = "${room.departAt.relativeTimeString()} • ${room.title}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MaterialTaxiRoomCell(room: TaxiRoom, taxiUser: TaxiUser? = null) {
    ElevatedCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        MaterialTaxiRoomContent(room, taxiUser, Modifier.padding(16.dp))
    }
}

// before: shared/views/taxiRoomCell/TaxiRoomCell.kt#Preview
@Preview(name = "TaxiRoomCell", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiRoomCell() {
    PreviewFrame { MaterialTaxiRoomCell(room = TaxiRoom.mockList()[0], taxiUser = TaxiUser.mock()) }
}

// before: shared/views/taxiRoomCell/components/TaxiParticipantsIndicator.kt#Preview
@Preview(name = "TaxiParticipantsIndicator", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiParticipantsIndicator() {
    PreviewFrame { MaterialTaxiParticipantsIndicator(2, 3) }
}

// before: shared/views/taxiRoomCell/components/TaxiRoomStatusIndicator.kt#Preview
@Preview(name = "TaxiRoomStatusIndicator", showBackground = true, widthDp = 411)
@Composable
private fun Material_TaxiRoomStatusIndicator() {
    PreviewFrame {
        MaterialTaxiRoomStatusIndicator(TaxiParticipant.SettlementType.NotDeparted, 0, 4)
        MaterialTaxiRoomStatusIndicator(TaxiParticipant.SettlementType.PaymentRequired, 0, 4)
        MaterialTaxiRoomStatusIndicator(TaxiParticipant.SettlementType.PaymentSent, 0, 4)
        MaterialTaxiRoomStatusIndicator(TaxiParticipant.SettlementType.RequestedSettlement, 4, 4)
        MaterialTaxiRoomStatusIndicator(TaxiParticipant.SettlementType.RequestedSettlement, 0, 4)
    }
}

// endregion

// region Timetable summary

@Composable
private fun SummaryStat(value: String, label: String) {
    val muted = value == "0" || value == "?"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (muted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryMini(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = if (value != "0") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun MaterialTimetableSummary(viewModel: TimetableViewModelProtocol, compact: Boolean = false) {
    val selectedTimetable by viewModel.selectedTimetable.collectAsState()
    val candidateLecture by viewModel.candidateLecture.collectAsState()

    val totalCredits = (selectedTimetable?.credits ?: 0) + (candidateLecture?.credit ?: 0)
    val totalAUs = (selectedTimetable?.creditAUs ?: 0) + (candidateLecture?.creditAU ?: 0)

    if (compact) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMini(stringResource(R.string.filter_basic_required), "${selectedTimetable?.getCreditsFor(LectureType.BR) ?: 0}")
                    SummaryMini(stringResource(R.string.filter_major_required), "${selectedTimetable?.getCreditsFor(LectureType.MR) ?: 0}")
                    SummaryMini(stringResource(R.string.hse), "${selectedTimetable?.getCreditsFor(LectureType.HSE) ?: 0}")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMini(stringResource(R.string.filter_basic_elective), "${selectedTimetable?.getCreditsFor(LectureType.BE) ?: 0}")
                    SummaryMini(stringResource(R.string.filter_major_elective), "${selectedTimetable?.getCreditsFor(LectureType.ME) ?: 0}")
                    SummaryMini(stringResource(R.string.etc), "${selectedTimetable?.getCreditsFor(LectureType.ETC) ?: 0}")
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SummaryStat("$totalCredits", stringResource(R.string.credit))
                SummaryStat("$totalAUs", stringResource(R.string.au))
                SummaryStat(selectedTimetable?.gradeLetter ?: "?", stringResource(R.string.grade))
                SummaryStat(selectedTimetable?.loadLetter ?: "?", stringResource(R.string.load))
                SummaryStat(selectedTimetable?.speechLetter ?: "?", stringResource(R.string.speech))
            }
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SummaryStat("${selectedTimetable?.credits ?: 0}", stringResource(R.string.credit))
                SummaryStat("${selectedTimetable?.creditAUs ?: 0}", stringResource(R.string.au))
                SummaryStat(selectedTimetable?.gradeLetter ?: "?", stringResource(R.string.grade))
                SummaryStat(selectedTimetable?.loadLetter ?: "?", stringResource(R.string.load))
                SummaryStat(selectedTimetable?.speechLetter ?: "?", stringResource(R.string.speech))
            }
        }
    }
}

// before: features/timetable/components/TimetableSummary.kt#Preview
@Preview(name = "TimetableSummary", showBackground = true, widthDp = 411)
@Composable
private fun Material_TimetableSummary() {
    PreviewFrame { MaterialTimetableSummary(PreviewTimetableViewModel()) }
}

// before: features/timetable/components/TimetableSummary.kt#CompactPreview
@Preview(name = "TimetableSummary_Compact", showBackground = true, widthDp = 411)
@Composable
private fun Material_TimetableSummary_Compact() {
    PreviewFrame(padding = 0.dp) { MaterialTimetableSummary(PreviewTimetableViewModel(), compact = true) }
}

// endregion

// region Credits summary

@Composable
private fun GpaValue(title: String, value: String, total: String, alignment: Alignment.Horizontal) {
    Column(horizontalAlignment = alignment, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Text("/$total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MaterialCreditsSummaryCard(state: CreditCalculationViewState) {
    ElevatedCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.credit_calculation),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            when {
                state.isLoading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                state.error != null -> Text(
                    stringResource(R.string.credit_load_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )

                else -> {
                    val summary = state.overallSummary
                    val graduation = state.requirements.graduation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GpaValue(stringResource(R.string.credit_gpa_label), formatGPA(summary.gpa), "4.3", Alignment.Start)
                        Spacer(Modifier.weight(1f))
                        GpaValue(
                            stringResource(R.string.credit_units_label),
                            "${summary.earnedCredits}",
                            "$graduation",
                            Alignment.End,
                        )
                        if (summary.earnedCredits >= graduation) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = stringResource(R.string.credit_requirement_met),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { creditProgress(summary.earnedCredits, graduation) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// before: features/timetable/creditCalculation/CreditsSummaryCard.kt#CreditsSummaryCardPreview
@Preview(name = "CreditsSummaryCard", showBackground = true, widthDp = 411)
@Composable
private fun Material_CreditsSummaryCard() {
    PreviewFrame { MaterialCreditsSummaryCard(creditPreviewState()) }
}

// before: features/timetable/creditCalculation/CreditsSummaryCard.kt#CreditsSummaryLoadingPreview
@Preview(name = "CreditsSummaryCard_Loading", showBackground = true, widthDp = 411)
@Composable
private fun Material_CreditsSummaryCard_Loading() {
    PreviewFrame { MaterialCreditsSummaryCard(CreditCalculationViewState()) }
}

// endregion

// region Tip

@Composable
private fun MaterialTipCard(tip: BuddyTip, message: String, actions: List<String> = emptyList()) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(tip.icon, contentDescription = null, modifier = Modifier.padding(top = 12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(tip.title), style = MaterialTheme.typography.titleSmall)
                Text(message, style = MaterialTheme.typography.bodyMedium)
                if (actions.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        actions.forEach { label -> TextButton(onClick = {}) { Text(label) } }
                    }
                }
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
            }
        }
    }
}

// before: shared/tips/TipView.kt#TipCardPreview
@Preview(name = "TipCard", showBackground = true, widthDp = 411)
@Composable
private fun Material_TipCard() {
    PreviewFrame {
        MaterialTipCard(BuddyTip.ResizePreview, stringResource(BuddyTip.ResizePreview.message))
    }
}

// before: shared/tips/TipView.kt#TipCardActionsPreview
@Preview(name = "TipCard_Actions", showBackground = true, widthDp = 411)
@Composable
private fun Material_TipCard_Actions() {
    PreviewFrame {
        MaterialTipCard(
            BuddyTip.StartEditing,
            stringResource(BuddyTip.StartEditing.message),
            actions = listOf(
                stringResource(R.string.tip_new_table),
                stringResource(R.string.tip_duplicate_my_table),
            ),
        )
    }
}

// endregion

// region Lecture review

@Composable
private fun RatingLetter(title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MaterialLectureReviewCell(review: LectureReview, subtitle: String? = null, isMine: Boolean = false) {
    val unknown = stringResource(R.string.unknown)
    val label = SectionLabel.of(subtitle.orEmpty(), review.professors)
    var liked by remember { mutableStateOf(review.likedByUser) }
    val likeCount = review.like + (if (liked != review.likedByUser) (if (liked) 1 else -1) else 0)

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = label.title ?: unknown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = "${review.year.toString().takeLast(2)}${review.semester.shortCode}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            supportingContent = if (label.professors != null) {
                { Text(label.professors.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            } else null,
            trailingContent = if (!isMine) {
                {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
                    }
                }
            } else null,
            colors = transparentListItemColors(),
        )
        Text(
            text = review.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RatingLetter(stringResource(R.string.grade), review.grade)
            RatingLetter(stringResource(R.string.load), review.load)
            RatingLetter(stringResource(R.string.speech), review.speech)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$likeCount", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                IconToggleButton(checked = liked, onCheckedChange = { liked = it }) {
                    Icon(
                        if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Vote",
                        tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// before: features/lectureDetail/components/LectureReviewCell.kt#Preview
@Preview(name = "LectureReviewCell", showBackground = true, widthDp = 411)
@Composable
private fun Material_LectureReviewCell() {
    PreviewFrame { MaterialLectureReviewCell(LectureReview.mock()) }
}

// before: features/lectureDetail/components/LectureReviewCell.kt#SubtitlePreview
@Preview(name = "LectureReviewCell_Subtitle", showBackground = true, widthDp = 411)
@Composable
private fun Material_LectureReviewCell_Subtitle() {
    PreviewFrame { MaterialLectureReviewCell(LectureReview.mock(), subtitle = "<디자인 과제로부터 살아남기>") }
}

// endregion

// region Search section

@Composable
private fun MaterialCourseItem(course: CourseSummary) {
    ListItem(
        headlineContent = { Text(course.name, maxLines = 2) },
        overlineContent = if (course.completed) {
            { Text(stringResource(R.string.course_taken), color = MaterialTheme.colorScheme.primary) }
        } else null,
        supportingContent = if (course.summary.isNotEmpty()) {
            { Text(course.summary, maxLines = 3, overflow = TextOverflow.Ellipsis) }
        } else null,
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(course.code)
                Text(stringResource(course.type.labelRes))
            }
        },
        modifier = Modifier.clickable {},
        colors = transparentListItemColors(),
    )
}

@Composable
private fun MaterialSearchSection(
    title: String,
    showMore: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (showMore) {
                TextButton(onClick = {}) { Text(stringResource(R.string.more)) }
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

// before: features/search/components/SearchSection.kt#Preview
@Preview(name = "SearchSection", showBackground = true, widthDp = 411)
@Composable
private fun Material_SearchSection() {
    PreviewFrame {
        MaterialSearchSection(title = "Search", showMore = true) {
            MaterialCourseItem(CourseSummary.mock())
        }
        MaterialSearchSection(title = "Search", showMore = true) {
            MaterialTaxiRoomContent(TaxiRoom.mock(), taxiUser = null, modifier = Modifier.padding(16.dp))
        }
    }
}

// endregion

// region Review digest

private val previewDigest = LectureReviewDigest(
    overview = "Clear lectures and fair exams, but weekly assignments take time.",
    liked = listOf("Clear explanations", "Helpful TAs"),
    disliked = listOf("Heavy weekly homework"),
    workload = "Weekly problem sets and two exams.",
    grading = "Generous if you submit everything.",
)

@Composable
private fun DigestCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun DigestHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DigestPointList(title: String, icon: ImageVector, accent: Color, points: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = accent)
            Text(title, style = MaterialTheme.typography.titleSmall, color = accent)
        }
        points.forEach { point ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(point, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun DigestAspect(title: String, icon: ImageVector, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// before: features/reviewDigest/ReviewDigestCard.kt#ReviewDigestFinishedPreview
@Preview(name = "ReviewDigestCard", showBackground = true, widthDp = 411)
@Composable
private fun Material_ReviewDigestCard() {
    PreviewFrame {
        DigestCard {
            DigestHeader(stringResource(R.string.review_digest_title))
            Text(previewDigest.overview.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            DigestPointList(
                stringResource(R.string.review_digest_liked),
                Icons.Filled.ThumbUp,
                MaterialTheme.colorScheme.primary,
                previewDigest.liked,
            )
            DigestPointList(
                stringResource(R.string.review_digest_disliked),
                Icons.Filled.Warning,
                MaterialTheme.colorScheme.error,
                previewDigest.disliked,
            )
            DigestAspect(stringResource(R.string.review_digest_workload), Icons.Outlined.Schedule, previewDigest.workload.orEmpty())
            DigestAspect(stringResource(R.string.review_digest_grading), Icons.Outlined.School, previewDigest.grading.orEmpty())
            Text(
                stringResource(R.string.review_digest_note, 12),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// before: features/reviewDigest/ReviewDigestCard.kt#ReviewDigestReadingPreview
@Preview(name = "ReviewDigestCard_Reading", showBackground = true, widthDp = 411)
@Composable
private fun Material_ReviewDigestCard_Reading() {
    PreviewFrame {
        DigestCard {
            DigestHeader(stringResource(R.string.review_digest_summarising))
            LinearProgressIndicator(
                progress = { (1 + 0.9f) / (3 + 1) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.review_digest_reading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// before: features/reviewDigest/ReviewDigestCard.kt#ReviewDigestButtonPreview
@Preview(name = "ReviewDigestCard_Button", showBackground = true, widthDp = 411)
@Composable
private fun Material_ReviewDigestCard_Button() {
    PreviewFrame {
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.review_digest_summarise))
        }
    }
}

// before: features/reviewDigest/ReviewDigestCard.kt#ReviewDigestFailedPreview
@Preview(name = "ReviewDigestCard_Failed", showBackground = true, widthDp = 411)
@Composable
private fun Material_ReviewDigestCard_Failed() {
    PreviewFrame {
        DigestCard {
            DigestHeader(stringResource(R.string.review_digest_title))
            Text(
                stringResource(R.string.review_digest_unsafe),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = {}, contentPadding = ButtonDefaults.TextButtonWithIconContentPadding) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.review_digest_try_again))
            }
        }
    }
}

// endregion

// region Board list

@Composable
private fun MaterialBoardList(title: String, icon: ImageVector, items: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider()
                ListItem(
                    headlineContent = { Text(item) },
                    modifier = Modifier.clickable {},
                    colors = transparentListItemColors(),
                )
            }
        }
    }
}

// before: features/boardList/components/BoardList.kt#Preview
@Preview(name = "BoardList", showBackground = true, widthDp = 411)
@Composable
private fun Material_BoardList() {
    PreviewFrame { MaterialBoardList(title = "title", icon = Icons.Filled.Drafts, items = listOf("board")) }
}

// endregion

// region Timetable offline status

@Composable
private fun MaterialTimetableOfflineStatus(state: TimetableLoadState) {
    val locale = LocalConfiguration.current.locales[0]
    val updated = state.lastUpdated?.let {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(it)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { Icon(Icons.Filled.CloudOff, contentDescription = null) },
            headlineContent = { Text(stringResource(R.string.timetable_offline)) },
            supportingContent = if (updated != null) {
                { Text(stringResource(R.string.timetable_last_updated, updated)) }
            } else null,
            colors = transparentListItemColors(),
        )
    }
}

// before: features/timetable/components/TimetableOfflineStatus.kt#TimetableOfflineStatusPreview
@Preview(name = "TimetableOfflineStatus", showBackground = true, widthDp = 411)
@Composable
private fun Material_TimetableOfflineStatus() {
    PreviewFrame {
        MaterialTimetableOfflineStatus(TimetableLoadState(isOffline = true, isShowingSavedData = true, lastUpdated = Date(0)))
    }
}

// before: features/timetable/components/TimetableOfflineStatus.kt#TimetableOfflineCacheMissPreview
@Preview(name = "TimetableOfflineStatus_CacheMiss", showBackground = true, widthDp = 411)
@Composable
private fun Material_TimetableOfflineStatus_CacheMiss() {
    PreviewFrame { MaterialTimetableOfflineStatus(TimetableLoadState(isOffline = true)) }
}

// endregion

// region Feed post row

@Composable
private fun FeedAvatar(post: FeedPost) {
    val avatarModifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant)
    if (post.profileImageURL != null) {
        AsyncImage(
            model = post.profileImageURL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = avatarModifier,
        )
    } else {
        Box(modifier = avatarModifier, contentAlignment = Alignment.Center) {
            Text("😀", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun MaterialFeedPostRow(post: FeedPost) {
    val authorName =
        if (post.authorName == "Anonymous") stringResource(R.string.anonymous) else post.authorName

    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { FeedAvatar(post) },
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        authorName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (post.isKaistIP) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = stringResource(R.string.kaist_ip_verified),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            },
            supportingContent = { Text(post.createdAt.timeAgoDisplay()) },
        )
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (post.images.isNotEmpty()) {
            // Image placeholders (network images are not loaded in screenshot tests).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                post.images.take(3).forEach { _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MaterialVoteGroup(
                myVote = when (post.myVote) {
                    FeedVoteType.UP -> true
                    FeedVoteType.DOWN -> false
                    else -> null
                },
                votes = post.upVotes - post.downVotes,
            )
            MaterialCommentChip(post.commentCount)
        }
        HorizontalDivider()
    }
}

// before: features/feed/components/FeedPostRow.kt#PreviewWithActions
@Preview(name = "FeedPostRow", showBackground = true, widthDp = 411)
@Composable
private fun Material_FeedPostRow() {
    PreviewFrame(padding = 0.dp) { MaterialFeedPostRow(FeedPost.mock()) }
}

// before: features/feed/components/FeedPostRow.kt#PreviewAnonymous
@Preview(name = "FeedPostRow_Anonymous", showBackground = true, widthDp = 411)
@Composable
private fun Material_FeedPostRow_Anonymous() {
    PreviewFrame(padding = 0.dp) { MaterialFeedPostRow(FeedPost.mockList()[4]) }
}

// endregion

// region Post list row

@Composable
private fun PostMeta(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, color = color)
    }
}

@Composable
private fun MaterialPostListRow(post: AraPost) {
    val voteCount = post.upVotes - post.downVotes
    val hasImage = post.attachmentType == AraPost.AttachmentType.IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH
    val hasFile = post.attachmentType == AraPost.AttachmentType.NON_IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                post.topic?.let { topic ->
                    Text(
                        "[" + topic.name.localized() + "]",
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = title(post) { id -> stringResource(id) },
                    color = if (post.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (voteCount > 0) PostMeta(Icons.Filled.ArrowUpward, "$voteCount", upvote)
                if (voteCount < 0) PostMeta(Icons.Filled.ArrowDownward, "−${-voteCount}", downvote)
                if (post.commentCount > 0) {
                    PostMeta(Icons.Outlined.ChatBubbleOutline, "${post.commentCount}", MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(post.author.profile.nickname, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.views, post.views))
                Text(post.createdAt.timeAgoDisplay())
            }
        },
        trailingContent = if (hasImage || hasFile) {
            {
                Row {
                    if (hasImage) Icon(Icons.Filled.Image, contentDescription = null)
                    if (hasFile) Icon(Icons.Filled.AttachFile, contentDescription = null)
                }
            }
        } else null,
        modifier = Modifier.clickable(enabled = !post.isHidden) {},
    )
}

// before: features/postList/components/postListRow/PostListRow.kt#Preview
@Preview(name = "PostListRow", showBackground = true, widthDp = 411)
@Composable
private fun Material_PostListRow() {
    Theme {
        Surface {
            Column {
                AraPost.mockList().forEachIndexed { index, post ->
                    if (index > 0) HorizontalDivider()
                    MaterialPostListRow(post)
                }
            }
        }
    }
}

// endregion
