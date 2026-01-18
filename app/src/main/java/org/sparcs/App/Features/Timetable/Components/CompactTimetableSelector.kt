package org.sparcs.App.Features.Timetable.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.Features.NavigationBar.Animation.AnimatedText
import org.sparcs.App.Features.Timetable.TimetableViewModelProtocol
import org.sparcs.App.Shared.ViewModelMocks.OTL.MockTimetableViewModel
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.R

@Composable
fun CompactTimetableSelector(
    viewModel: TimetableViewModelProtocol,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SemesterSelector(viewModel = viewModel)

        Spacer(Modifier.weight(1f))

        TableSelector(viewModel = viewModel)
    }
}

@Composable
fun SemesterSelector(
    viewModel: TimetableViewModelProtocol,
) {
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val isEnabledPreviousButton =
        viewModel.semesters.collectAsState().value.firstOrNull() != selectedSemester
    val isEnabledNextButton =
        viewModel.semesters.collectAsState().value.lastOrNull() != selectedSemester
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_back_ios),
            contentDescription = "Select Previous Semester",
            tint = if (isEnabledPreviousButton) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .then(
                    if (isEnabledPreviousButton) Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.selectPreviousSemester()
                    } else Modifier
                )
        )

        AnimatedText(
            text = selectedSemester?.description ?: stringResource(R.string.unknown),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        Icon(
            painter = painterResource(R.drawable.arrow_forward_ios),
            contentDescription = "Select Next Semester",
            tint = if (isEnabledNextButton) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .then(
                    if (isEnabledNextButton) Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        viewModel.selectNextSemester()
                    } else Modifier
                )
        )
    }
}

@Composable
fun TableSelector(viewModel: TimetableViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }
    val selectedTimetableDisplayName by viewModel.selectedTimetableDisplayName.collectAsState()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedTimetableDisplayName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Icon(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = "Menu",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clickable { expanded = true }
        )

        TimetableDropDownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            viewModel = viewModel
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { CompactTimetableSelector(viewModel = MockTimetableViewModel()) }
}