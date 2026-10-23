package org.sparcs.soap.app.shared.tips

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import org.sparcs.soap.R

enum class BuddyTip(
    val id: String,
    @param:StringRes val title: Int,
    @param:StringRes val message: Int,
    val icon: ImageVector,
    val maxDisplayCount: Int? = null,
    val onboardingStep: TimetableOnboarding.Step? = null,
) {
    StartEditing(
        "timetable.startEditing", R.string.tip_start_editing_title, R.string.tip_start_editing_message,
        Icons.Outlined.EditNote, onboardingStep = TimetableOnboarding.Step.StartEditing,
    ),
    AddEvent(
        "timetable.addEvent", R.string.tip_add_event_title, R.string.tip_add_event_message,
        Icons.Outlined.AddBox, onboardingStep = TimetableOnboarding.Step.AddButton,
    ),
    ResizePreview(
        "lectureSearch.resizePreview", R.string.tip_resize_preview_title, R.string.tip_resize_preview_message,
        Icons.Outlined.Height, onboardingStep = TimetableOnboarding.Step.ResizePreview,
    ),
    TogglePreview(
        "lectureSearch.togglePreview", R.string.tip_toggle_preview_title, R.string.tip_toggle_preview_message,
        Icons.Outlined.CalendarMonth, onboardingStep = TimetableOnboarding.Step.TogglePreview,
    ),
    AddFromDetail(
        "lectureDetail.addFromDetail", R.string.tip_add_from_detail_title, R.string.tip_add_from_detail_message,
        Icons.Outlined.AddCircleOutline, onboardingStep = TimetableOnboarding.Step.AddLecture,
    ),
    PeekLecture(
        "lectureSearch.peekLecture", R.string.tip_peek_lecture_title, R.string.tip_peek_lecture_message,
        Icons.Outlined.TouchApp, maxDisplayCount = 3,
    ),
    ShareTimetable(
        "timetable.share", R.string.tip_share_timetable_title, R.string.tip_share_timetable_message,
        Icons.Outlined.Share, maxDisplayCount = 3,
    ),
    QuickFill(
        "activityCreation.quickFill", R.string.tip_quick_fill_title, R.string.tip_quick_fill_message,
        Icons.Outlined.AutoAwesome, maxDisplayCount = 3,
    ),
    SummariseReview(
        "lectureReview.summarise", R.string.tip_summarise_review_title, R.string.tip_summarise_review_message,
        Icons.Outlined.Summarize, maxDisplayCount = 3,
    ),
    PostTools(
        "post.tools", R.string.tip_post_tools_title, R.string.tip_post_tools_message,
        Icons.Outlined.Translate, maxDisplayCount = 3,
    ),
    ThemeActions(
        "settings.themeActions", R.string.tip_theme_actions_title, R.string.tip_theme_actions_message,
        Icons.Outlined.Palette, maxDisplayCount = 3,
    );

    val isOnboarding: Boolean get() = onboardingStep != null
}

enum class TipEvent(val id: String) {
    LectureOpened("lectureSearch.lectureOpened"),
    ActivityCreationOpened("activityCreation.opened"),
    PostOpened("post.opened"),
    ThemeSettingsOpened("settings.themeOpened"),
}
