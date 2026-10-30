@file:OptIn(ExperimentalMaterial3Api::class)

package org.sparcs.soap.materialpreview

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Feed
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.feed.FeedVoteType
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostComment
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.post.contentPlainText
import org.sparcs.soap.app.shared.extensions.ceilToNextTenMinutes
import org.sparcs.soap.app.shared.extensions.formattedString
import org.sparcs.soap.app.shared.extensions.postfixEuroRo
import org.sparcs.soap.app.shared.extensions.relativeTimeString
import org.sparcs.soap.app.shared.extensions.timeAgoDisplay
import org.sparcs.soap.app.shared.extensions.weekdaySymbol
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.mocks.feed.mockList
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/*
 * Full-screen mockups of the main screens rebuilt from default Material 3 components.
 * Static mock data only (the same `.mock()` / `.mockList()` data the original previews use).
 */

// region Shared helpers

private data class ScreenTab(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
)

@Composable
private fun ScreenMainNavigationBar(selectedIndex: Int) {
    val tabs = listOf(
        ScreenTab(R.string.start, Icons.AutoMirrored.Filled.Feed, Icons.AutoMirrored.Outlined.Feed),
        ScreenTab(
            R.string.boards,
            Icons.AutoMirrored.Filled.FormatListBulleted,
            Icons.AutoMirrored.Outlined.FormatListBulleted
        ),
        ScreenTab(R.string.timetable, Icons.Filled.TableChart, Icons.Outlined.TableChart),
        ScreenTab(R.string.taxi, Icons.Filled.LocalTaxi, Icons.Outlined.LocalTaxi),
        ScreenTab(R.string.search, Icons.Filled.Search, Icons.Outlined.Search),
    )
    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(tab.labelRes)) }
            )
        }
    }
}

@Composable
private fun ScreenBackButton() {
    IconButton(onClick = {}) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

@Composable
private fun ScreenCloseButton() {
    IconButton(onClick = {}) {
        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
    }
}

@Composable
private fun ScreenOverflowButton() {
    IconButton(onClick = {}) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
    }
}

@Composable
private fun ScreenSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

/** Network images are not loaded in previews; every avatar is a tonal person placeholder. */
@Composable
private fun ScreenAvatar(size: Dp = 40.dp) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

/** Up/down vote control as a tonal pill. */
@Composable
private fun ScreenVoteControl(myVote: Boolean?, votes: Int) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = if (myVote == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "UpVote",
                    tint = if (myVote == true) upvote else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = votes.toString().replace('-', '−'),
                style = MaterialTheme.typography.labelLarge,
                color = when (myVote) {
                    true -> upvote
                    false -> downvote
                    null -> MaterialTheme.colorScheme.onSurface
                }
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = if (myVote == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = "DownVote",
                    tint = if (myVote == false) downvote else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ScreenCommentChip(commentCount: Int) {
    AssistChip(
        onClick = {},
        label = { Text(commentCount.toString()) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
private fun ScreenPostTitle(post: AraPost): AnnotatedString {
    val hiddenTitle = when {
        post.isHidden && post.isNSFW -> stringResource(R.string.this_post_contains_nsfw_content)
        post.isHidden && post.isPolitical -> stringResource(R.string.this_post_contains_political_content)
        post.isHidden -> stringResource(R.string.this_post_is_hidden)
        else -> null
    }
    val title = hiddenTitle ?: post.title ?: stringResource(R.string.untitled)
    val primary = MaterialTheme.colorScheme.primary
    return buildAnnotatedString {
        post.topic?.name?.localized()?.let { topic ->
            withStyle(SpanStyle(color = primary)) { append("[$topic] ") }
        }
        append(title)
    }
}

/** Board-post row: replaces PostListRow (used by the board list and search). */
@Composable
private fun ScreenPostListItem(post: AraPost) {
    val voteCount = post.upVotes - post.downVotes
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ScreenPostTitle(post),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (post.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (post.attachmentType == AraPost.AttachmentType.IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (post.attachmentType == AraPost.AttachmentType.NON_IMAGE || post.attachmentType == AraPost.AttachmentType.BOTH) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
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
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (voteCount != 0) {
                        ScreenCountLabel(
                            icon = if (voteCount > 0) Icons.Outlined.ThumbUp else Icons.Outlined.ThumbDown,
                            text = voteCount.toString().replace('-', '−'),
                            color = if (voteCount > 0) upvote else downvote
                        )
                    }
                    if (post.commentCount > 0) {
                        ScreenCountLabel(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            text = post.commentCount.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else null
    )
}

@Composable
private fun ScreenCountLabel(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** Taxi room row: replaces TaxiRoomCell (used by the taxi list and search). */
@Composable
private fun ScreenTaxiRoomListItem(room: TaxiRoom) {
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
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${room.participants.size}/${room.capacity}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    )
}

@Composable
private fun ScreenLocationField(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

/** Replaces TaxiDestinationPicker + FavoriteRouteDropdown. */
@Composable
private fun ScreenRoutePicker(locations: List<TaxiLocation>) {
    val options = locations.map { it.title.localized() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScreenLocationField(
                    label = stringResource(R.string.meeting_point),
                    value = "",
                    options = options
                )
                ScreenLocationField(
                    label = stringResource(R.string.where_to),
                    value = "",
                    options = options
                )
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Swap")
            }
        }
        AssistChip(
            onClick = {},
            label = { Text(stringResource(R.string.favorite_routes)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
    }
}

// endregion

// region 1. Settings

@Composable
private fun ScreenSettingsRow(
    headline: String,
    icon: @Composable () -> Unit,
    supporting: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(headline) },
        supportingContent = if (supporting != null) {
            { Text(supporting) }
        } else null,
        leadingContent = icon,
        trailingContent = trailing
    )
}

@Composable
private fun ScreenSettingsVectorIcon(imageVector: ImageVector) {
    Icon(imageVector, contentDescription = null)
}

@Composable
private fun ScreenSettingsLogoIcon(resId: Int) {
    // Service logos keep their own brand colours.
    Icon(
        painter = painterResource(resId),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun ScreenSettings() {
    var crashReports by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { ScreenBackButton() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            item {
                ScreenSectionHeader(stringResource(R.string.widget_miscellaneous))
                ScreenSettingsRow(
                    headline = stringResource(R.string.change_language),
                    supporting = Locale.getDefault().displayLanguage,
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Language) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.notifications_title),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Notifications) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.widget_dark_mode),
                    supporting = stringResource(R.string.widget_system_default),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.DarkMode) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.send_crash_reports),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Lightbulb) },
                    trailing = {
                        Switch(checked = crashReports, onCheckedChange = { crashReports = it })
                    }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.chat_with_us),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Feedback) }
                )
            }

            item {
                ScreenSectionHeader(stringResource(R.string.services))
                ScreenSettingsRow(
                    headline = stringResource(R.string.feed),
                    icon = { ScreenSettingsLogoIcon(R.drawable.sparcs_logo) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.ara),
                    icon = { ScreenSettingsLogoIcon(R.drawable.ara_logo) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.otl),
                    icon = { ScreenSettingsLogoIcon(R.drawable.otl_logo) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.taxi),
                    icon = { ScreenSettingsLogoIcon(R.drawable.taxi_logo) }
                )
            }

            item {
                ScreenSectionHeader(stringResource(R.string.screen_style))
                ScreenSettingsRow(
                    headline = stringResource(R.string.timetable_theme),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Palette) }
                )
            }

            item {
                ScreenSectionHeader(stringResource(R.string.information))
                ScreenSettingsRow(
                    headline = stringResource(R.string.privacy_policy),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Policy) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.terms_of_use),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Description) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.acknowledgements),
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.VolunteerActivism) }
                )
                ScreenSettingsRow(
                    headline = stringResource(R.string.app_version),
                    supporting = BuildConfig.VERSION_NAME,
                    icon = { ScreenSettingsVectorIcon(Icons.Outlined.Info) }
                )
            }

            item {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.sign_out),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }

            if (BuildConfig.DEBUG) {
                item {
                    ScreenSectionHeader("Debug Menu")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedButton(onClick = {}) { Text("Force Crash") }
                        OutlinedButton(onClick = {}) { Text("Invoke Exception") }
                    }
                }
            }
        }
    }
}

// endregion

// region 2. Board post list

@Composable
private fun ScreenPostList() {
    val board = remember { AraBoard.mock() }
    val posts = remember { AraPost.mockList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(board.name.localized())
                        Text(
                            text = board.group.name.localized(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = { ScreenBackButton() },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                    }
                }
            )
        },
        floatingActionButton = {
            if (!board.isReadOnly && board.userWritable == true) {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.write))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            items(posts) { post ->
                ScreenPostListItem(post)
                HorizontalDivider()
            }
        }
    }
}

// endregion

// region 3. Post detail

@Composable
private fun ScreenCommentItem(comment: AraPostComment, isThreaded: Boolean) {
    val isDeleted = comment.content == null
    Column(Modifier.padding(start = if (isThreaded) 40.dp else 0.dp)) {
        ListItem(
            leadingContent = { ScreenAvatar(if (isThreaded) 32.dp else 40.dp) },
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.author.profile.nickname,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " · " + comment.createdAt.timeAgoDisplay(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            supportingContent = {
                Text(comment.content ?: stringResource(R.string.this_comment_has_been_deleted))
            },
            trailingContent = if (!isDeleted) {
                { ScreenOverflowButton() }
            } else null
        )
        if (!isDeleted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = if (isThreaded) 64.dp else 72.dp, end = 16.dp)
            ) {
                if (!isThreaded) {
                    TextButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.feed_reply_action))
                    }
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.weight(1f))
                }
                ScreenVoteControl(myVote = comment.myVote, votes = comment.upVotes - comment.downVotes)
            }
        }
    }
}

@Composable
private fun ScreenPostDetail() {
    val post = remember { AraPost.mock() }
    var comment by remember { mutableStateOf("") }
    val myNickname = post.myCommentProfile?.profile?.nickname ?: stringResource(R.string.anonymous)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(post.board?.group?.name?.localized() ?: "") },
                navigationIcon = { ScreenBackButton() },
                actions = { ScreenOverflowButton() }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ScreenAvatar(32.dp)
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text(stringResource(R.string.reply_as, myNickname.postfixEuroRo())) },
                        maxLines = 4,
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(onClick = {}, enabled = comment.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            item {
                Text(
                    text = ScreenPostTitle(post),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                )
                ListItem(
                    leadingContent = { ScreenAvatar() },
                    headlineContent = { Text(post.author.profile.nickname) },
                    supportingContent = {
                        Text("${post.createdAt.formattedString()} · ${stringResource(R.string.views, post.views)}")
                    }
                )
                HorizontalDivider()
            }

            item {
                // The original renders the body in a WebView; plain text stands in here.
                Text(
                    text = post.contentPlainText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ScreenVoteControl(myVote = post.myVote, votes = post.upVotes - post.downVotes)
                    ScreenCommentChip(post.commentCount)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = stringResource(R.string.to_bookmark))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.share))
                    }
                }
                HorizontalDivider()
            }

            items(post.comments) { c ->
                ScreenCommentItem(c, isThreaded = false)
                c.comments.forEach { reply -> ScreenCommentItem(reply, isThreaded = true) }
                HorizontalDivider()
            }
        }
    }
}

// endregion

// region 4. Post compose

@Composable
private fun ScreenPostCompose() {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(true) }
    var nsfw by remember { mutableStateOf(false) }
    var political by remember { mutableStateOf(false) }
    var topicExpanded by remember { mutableStateOf(false) }
    // Same topics as MockPostComposeViewModel.
    val topics = listOf("Topic", "Topic")
    val noTopic = stringResource(R.string.no_topic)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.write)) },
                navigationIcon = { ScreenCloseButton() },
                actions = {
                    TextButton(
                        onClick = {},
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text(stringResource(R.string.submit))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = stringResource(R.string.add_photo))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {}) { Text(stringResource(R.string.terms_of_use)) }
                }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = topicExpanded,
                onExpandedChange = { topicExpanded = it }
            ) {
                OutlinedTextField(
                    value = noTopic,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = topicExpanded,
                    onDismissRequest = { topicExpanded = false }
                ) {
                    (listOf(noTopic) + topics).forEach { topic ->
                        DropdownMenuItem(
                            text = { Text(topic) },
                            onClick = { topicExpanded = false },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text(stringResource(R.string.enter_the_title)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(stringResource(R.string.enter_the_description)) },
                minLines = 12,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                ScreenToggleChip(stringResource(R.string.anonymous), anonymous) { anonymous = it }
                ScreenToggleChip(stringResource(R.string.nsfw), nsfw) { nsfw = it }
                ScreenToggleChip(stringResource(R.string.political), political) { political = it }
            }
        }
    }
}

@Composable
private fun ScreenToggleChip(label: String, selected: Boolean, onChange: (Boolean) -> Unit) {
    FilterChip(
        selected = selected,
        onClick = { onChange(!selected) },
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null
    )
}

// endregion

// region 5. Feed

@Composable
private fun ScreenFeedPostItem(post: FeedPost) {
    val authorName =
        if (post.authorName == "Anonymous") stringResource(R.string.anonymous) else post.authorName
    Column(Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { ScreenAvatar() },
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = authorName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (post.isKaistIP) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = stringResource(R.string.kaist_ip_verified),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            supportingContent = { Text(post.createdAt.timeAgoDisplay()) },
            trailingContent = { ScreenOverflowButton() }
        )

        Text(
            text = post.content.replace("\\n", "\n"),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (post.images.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            // Remote images are not loaded in previews; tonal placeholders keep the layout.
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(post.images) { _ ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ScreenVoteControl(
                myVote = when (post.myVote) {
                    FeedVoteType.UP -> true
                    FeedVoteType.DOWN -> false
                    else -> null
                },
                votes = post.upVotes - post.downVotes
            )
            ScreenCommentChip(post.commentCount)
        }
    }
}

@Composable
private fun ScreenFeed() {
    val posts = remember { FeedPost.mockList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.start)) },
                actions = { ScreenOverflowButton() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                text = { Text(stringResource(R.string.write)) }
            )
        },
        bottomBar = { ScreenMainNavigationBar(selectedIndex = 0) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            items(posts) { post ->
                ScreenFeedPostItem(post)
                HorizontalDivider()
            }
        }
    }
}

// endregion

// region 6. Taxi list

@Composable
private fun ScreenWeekDayChips() {
    val week = remember { (0 until 7).map { LocalDate.now().plusDays(it.toLong()) } }
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ScreenToggleChip(stringResource(R.string.all), selected == null) { selected = null }
        }
        items(week) { day ->
            val label = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) +
                    " " + day.dayOfMonth
            ScreenToggleChip(label, selected == day) { selected = day }
        }
    }
}

@Composable
private fun ScreenTaxiList() {
    val locations = remember { TaxiLocation.mockList() }
    // Mock rooms depart one per day; the list shows the current week grouped by weekday.
    val roomsByDay = remember { TaxiRoom.mockList().take(7).groupBy { it.departAt.weekdaySymbol() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.taxi)) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = stringResource(R.string.taxichatlistview)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_room)) }
            )
        },
        bottomBar = { ScreenMainNavigationBar(selectedIndex = 3) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(Modifier.padding(16.dp)) {
                        ScreenRoutePicker(locations)
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                ScreenWeekDayChips()
            }

            roomsByDay.forEach { (day, rooms) ->
                item {
                    ScreenSectionHeader(day)
                }
                items(rooms) { room ->
                    ScreenTaxiRoomListItem(room)
                    HorizontalDivider(Modifier.padding(start = 56.dp))
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// endregion

// region 7. Search

@Composable
private fun ScreenSearchSection(
    title: String,
    showMore: Boolean,
    content: @Composable () -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (showMore) {
                TextButton(onClick = {}) { Text(stringResource(R.string.more)) }
            }
        }
        OutlinedCard(Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun ScreenCourseListItem(course: CourseSummary) {
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
        } else null
    )
}

@Composable
private fun ScreenSearch() {
    // Same data as MockSearchViewModel(Loaded): query "asd", scope All, first 3 of each result type.
    var query by remember { mutableStateOf("asd") }
    var scope by remember { mutableStateOf(SearchScope.All) }
    val courses = remember { CourseSummary.mockList().take(3) }
    val posts = remember { AraPost.mockList().take(3) }
    val rooms = remember { TaxiRoom.mockList().take(3) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.search)) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
                                    }
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {}
            }
        },
        bottomBar = { ScreenMainNavigationBar(selectedIndex = 4) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SearchScope.entries) { s ->
                        ScreenToggleChip(stringResource(s.labelRes), scope == s) { scope = s }
                    }
                }
            }

            if (scope == SearchScope.All || scope == SearchScope.Courses) {
                item {
                    ScreenSearchSection(stringResource(R.string.courses), showMore = scope == SearchScope.All) {
                        courses.forEachIndexed { index, course ->
                            if (index > 0) HorizontalDivider()
                            ScreenCourseListItem(course)
                        }
                    }
                }
            }

            if (scope == SearchScope.All || scope == SearchScope.Posts) {
                item {
                    ScreenSearchSection(stringResource(R.string.posts), showMore = scope == SearchScope.All) {
                        posts.forEachIndexed { index, post ->
                            if (index > 0) HorizontalDivider()
                            ScreenPostListItem(post)
                        }
                    }
                }
            }

            if (scope == SearchScope.All || scope == SearchScope.Rides) {
                item {
                    ScreenSearchSection(stringResource(R.string.rides), showMore = scope == SearchScope.All) {
                        rooms.forEachIndexed { index, room ->
                            if (index > 0) HorizontalDivider()
                            ScreenTaxiRoomListItem(room)
                        }
                    }
                }
            }
        }
    }
}

// endregion

// region 8. Taxi room creation

@Composable
private fun ScreenTaxiRoomCreation() {
    val locations = remember { TaxiLocation.mockList() }
    val randomRoomName = remember { Constants.taxiDefaultRoomNames.first() }
    val departure = remember { Date().ceilToNextTenMinutes() }
    val dateText = remember(departure) { SimpleDateFormat("MM/dd", Locale.getDefault()).format(departure) }
    val timeText = remember(departure) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(departure) }

    var title by remember { mutableStateOf("") }
    var capacity by remember { mutableIntStateOf(4) }
    var hasCarrier by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_room)) },
                navigationIcon = { ScreenCloseButton() },
                actions = {
                    // Disabled: source/destination are unset, as in the original preview.
                    TextButton(onClick = {}, enabled = false) {
                        Text(stringResource(R.string.submit))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ScreenRoutePicker(locations)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title)) },
                placeholder = { Text(randomRoomName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(R.string.departure_time)) },
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = timeText,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.capacity),
                    style = MaterialTheme.typography.titleSmall
                )
                val options = (2..4).toList()
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, count ->
                        SegmentedButton(
                            selected = capacity == count,
                            onClick = { capacity = count },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            label = { Text(pluralStringResource(R.plurals.people_count, count, count)) }
                        )
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                ListItem(
                    leadingContent = { Icon(Icons.Outlined.Luggage, contentDescription = null) },
                    headlineContent = { Text(stringResource(R.string.carrier_status_title)) },
                    supportingContent = {
                        Text(
                            stringResource(
                                if (hasCarrier) R.string.carrier_status_yes else R.string.carrier_status_no
                            )
                        )
                    },
                    trailingContent = {
                        Switch(checked = hasCarrier, onCheckedChange = { hasCarrier = it })
                    }
                )
            }
        }
    }
}

// endregion

// region Previews

// before: features/settings/SettingsView.kt#Preview
@Preview(name = "SettingsView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_SettingsView() {
    Theme { ScreenSettings() }
}

// before: features/postList/PostListView.kt#PreviewPostListLoaded
@Preview(name = "PostListView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_PostListView() {
    Theme { ScreenPostList() }
}

// before: features/post/PostView.kt#LoadedPreview
@Preview(name = "PostView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_PostView() {
    Theme { ScreenPostDetail() }
}

// before: features/postCompose/PostComposeView.kt#PostComposeViewPreview
@Preview(name = "PostComposeView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_PostComposeView() {
    Theme { ScreenPostCompose() }
}

// before: features/feed/FeedView.kt#PreviewLoaded
@Preview(name = "FeedView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_FeedView() {
    Theme { ScreenFeed() }
}

// before: features/taxiList/TaxiListView.kt#TaxiListScreenLoadedPreview
@Preview(name = "TaxiListView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_TaxiListView() {
    Theme { ScreenTaxiList() }
}

// before: features/search/SearchView.kt#LoadedPreview
@Preview(name = "SearchView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_SearchView() {
    Theme { ScreenSearch() }
}

// before: features/taxiRoomCreation/TaxiRoomCreationView.kt#Preview
@Preview(name = "TaxiRoomCreationView", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun Material_TaxiRoomCreationView() {
    Theme { ScreenTaxiRoomCreation() }
}

// endregion
