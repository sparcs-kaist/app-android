package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.LectureSearchStyleStore
import org.sparcs.soap.app.domain.helpers.ReviewSummarySettingsStore
import org.sparcs.soap.app.domain.helpers.rememberLectureSearchStyle
import org.sparcs.soap.app.domain.helpers.rememberSummarisesReviewsAutomatically
import org.sparcs.soap.app.domain.models.otl.LectureSearchStyle
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestViewModel
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestViewModelProtocol
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.timetable.components.TimetableSilhouetteView
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewReviewDigestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableSettingsView(
    navController: NavController,
) {
    val topBarScrollBehavior = topAppBarScrollBehavior()

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                stringResource(R.string.otl_settings),
                { navController.popBackStack() },
                scrollBehavior = topBarScrollBehavior,
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen("Timetable Settings")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                item {
                    LoadedView(navController = navController)
                }
            }
        }
    }
}

@Composable
private fun LoadedView(
    navController: NavController,
) {
    val context = LocalContext.current
    val style by rememberLectureSearchStyle()
    val store = remember(context) { LectureSearchStyleStore(context) }

    Column {
        SettingsSectionHeader(stringResource(R.string.adding_lectures))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LectureSearchStyle.entries.forEach { option ->
                SearchStylePreviewCard(
                    modifier = Modifier.weight(1f),
                    selected = style == option,
                    title = stringResource(option.titleRes),
                    onClick = { store.lectureSearchStyle = option }
                ) {
                    SearchStylePreview(option)
                }
            }
        }

        SettingsSectionHeader(stringResource(R.string.interested_departments))

        SettingsNavigationItem(
            text = stringResource(R.string.interested_departments),
            onClick = { navController.navigate(Channel.InterestedDepartments.name) },
            leadingContent = { Icon(Icons.Outlined.School, contentDescription = null) }
        )

        ReviewSummarySetting()
    }
}

@Composable
private fun ReviewSummarySetting(
    viewModel: ReviewDigestViewModelProtocol = if (LocalInspectionMode.current) {
        remember { PreviewReviewDigestViewModel() }
    } else {
        hiltViewModel<ReviewDigestViewModel>()
    },
) {
    val context = LocalContext.current
    val store = remember(context) { ReviewSummarySettingsStore(context) }
    val isAutomatic by rememberSummarisesReviewsAutomatically()
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) { viewModel.checkAvailability() }
    if (!state.isAvailable) return

    Column {
        SettingsSectionHeader(stringResource(R.string.review_digest_settings_title))

        SettingsSwitchItem(
            text = stringResource(R.string.review_digest_settings_automatic),
            checked = isAutomatic,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                store.summarisesAutomatically = it
            },
            supportingText = stringResource(R.string.review_digest_settings_footer)
        )
    }
}

@Composable
private fun SearchStylePreviewCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
    preview: @Composable () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected; role = Role.RadioButton },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                preview()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun SearchStylePreview(style: LectureSearchStyle) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (style == LectureSearchStyle.Flexible) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.height(12.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Box(
            Modifier.fillMaxWidth(if (style == LectureSearchStyle.Fixed) 1f else 0.55f)
                .height(if (style == LectureSearchStyle.Fixed) 45.dp else 33.dp)
                .align(Alignment.End)
        ) {
            TimetableSilhouetteView(
                remember { Timetable.mock() },
                Modifier.fillMaxSize(),
            )
        }
        if (style == LectureSearchStyle.Fixed) {
            Text(
                stringResource(R.string.lecture_search_credit_preview),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Box(
                Modifier.align(Alignment.End).fillMaxWidth(0.2f).height(3.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            )
        }
        repeat(2) {
            Box(
                Modifier.fillMaxWidth().height(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableSettingsViewPreview() {
    Theme { TimetableSettingsView(rememberNavController()) }
}

@Preview(showBackground = true)
@Composable
private fun ReviewSummarySettingPreview() {
    Theme { ReviewSummarySetting(PreviewReviewDigestViewModel()) }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun LectureSearchStylesPreview() {
    Theme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LectureSearchStyle.entries.forEach { style ->
                SearchStylePreviewCard(
                    modifier = Modifier.weight(1f), selected = true,
                    title = stringResource(style.titleRes), onClick = {},
                ) { SearchStylePreview(style) }
            }
        }
    }
}
