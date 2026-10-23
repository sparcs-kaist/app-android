package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipAction
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.shared.tips.rememberPreviewTipStore
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TimetableOnboardingTips(
    isMyTable: Boolean,
    isEditable: Boolean,
    onNewTable: () -> Unit,
    onDuplicateMyTable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val store = LocalTipStore.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TipView(
            tip = BuddyTip.StartEditing,
            enabled = isMyTable,
            actions = listOf(
                TipAction(stringResource(R.string.tip_new_table)) {
                    store?.invalidate(BuddyTip.StartEditing)
                    onNewTable()
                },
                TipAction(stringResource(R.string.tip_duplicate_my_table)) {
                    store?.invalidate(BuddyTip.StartEditing)
                    onDuplicateMyTable()
                },
            ),
        )
        TipView(tip = BuddyTip.AddEvent, enabled = isEditable)
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableOnboardingTipsPreview() {
    Theme {
        CompositionLocalProvider(LocalTipStore provides rememberPreviewTipStore()) {
            TimetableOnboardingTips(
                isMyTable = true,
                isEditable = false,
                onNewTable = {},
                onDuplicateMyTable = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
