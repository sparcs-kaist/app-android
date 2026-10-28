package org.sparcs.soap.app.features.reviewCompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModel
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.app.features.reviewCompose.components.ReviewComposeNavigationBar
import org.sparcs.soap.app.networking.responseDTO.otl.ratingToString
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.noRippleClickable
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewLectureDetailViewModel
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewReviewComposeViewModel

@Composable
fun ReviewComposeView(
    viewModel: ReviewComposeViewModelProtocol = hiltViewModel<ReviewComposeViewModel>(),
    lectureDetailViewModel: LectureDetailViewModelProtocol = hiltViewModel<LectureDetailViewModel>(),
    navController: NavController,
) {
    val writtenReview by lectureDetailViewModel.writtenReview.collectAsState()

    var grade by remember { mutableIntStateOf(5) }
    var load by remember { mutableIntStateOf(5) }
    var speech by remember { mutableIntStateOf(5) }
    var contentField by remember { mutableStateOf(TextFieldValue("")) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val scope = rememberCoroutineScope()

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorLine by remember { derivedStateOf { textLayoutResult?.getLineForOffset(contentField.selection.start) } }
    val keyboardPaddingPx = with(LocalDensity.current) { 250.dp.toPx() }

    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(writtenReview) {
        writtenReview?.let { review ->
            grade = stringToScore(review.grade)
            load = stringToScore(review.load)
            speech = stringToScore(review.speech)
            contentField = TextFieldValue(review.content)
        }
    }

    LaunchedEffect(cursorLine) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        val line = cursorLine ?: return@LaunchedEffect
        val lineTopPx = layout.getLineTop(line)
        val scrollOffset = maxOf(lineTopPx - keyboardPaddingPx, 0f)
        coroutineScope.launch { scrollState.animateScrollTo(scrollOffset.toInt()) }
    }

    Scaffold(
        topBar = {
            ReviewComposeNavigationBar(
                navController = navController,
                onDoneClick = {
                    scope.launch {
                        val isSuccess = viewModel.submitReview(
                            content = contentField.text,
                            grade = grade,
                            load = load,
                            speech = speech
                        )

                        if (isSuccess) {
                            val review = viewModel.writtenReview.value
                            if (review != null) {
                                lectureDetailViewModel.updateWrittenReview(review)
                            } else {
                                lectureDetailViewModel.fetchReviews(lecture = viewModel.lecture)
                            }

                            navController.popBackStack()
                        }
                    }
                },
                isUploading = viewModel.isUploading,
                isDoneEnabled = contentField.text.isNotBlank() && !viewModel.isUploading,
                isEditing = writtenReview != null
            )
        },
        modifier = Modifier.analyticsScreen("Review Compose")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingPicker(
                    title = stringResource(R.string.grade),
                    value = grade,
                    onValueChange = { grade = it })
                RatingPicker(
                    title = stringResource(R.string.load),
                    value = load,
                    onValueChange = { load = it })
                RatingPicker(
                    title = stringResource(R.string.speech),
                    value = speech,
                    onValueChange = { speech = it })
            }

            Spacer(Modifier.padding(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .focusRequester(contentFocusRequester)
                    .noRippleClickable { contentFocusRequester.requestFocus() }
            ) {
                BasicTextField(
                    value = contentField,
                    onValueChange = {
                        contentField = it
                    },
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions.Default,
                    onTextLayout = { textLayoutResult = it },
                    decorationBox = { inner ->
                        if (contentField.text.isEmpty())
                            Text(
                                text = stringResource(
                                    R.string.share_lecture_thoughts,
                                    viewModel.lecture.name
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium
                            )
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

/** One rating as a label and an elevated chip that opens the grades, like the other pickers. */
@Composable
private fun RatingPicker(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box {
            ElevatedFilterChip(
                selected = false,
                onClick = { expanded = true },
                label = { Text(ratingToString(value)) },
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            )

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf(5, 4, 3, 2, 1).forEach { itValue ->
                    DropdownMenuItem(
                        text = { Text(ratingToString(itValue)) },
                        trailingIcon = if (itValue == value) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null,
                        onClick = {
                            onValueChange(itValue)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun stringToScore(grade: String): Int = when (grade) {
    "A" -> 5
    "B" -> 4
    "C" -> 3
    "D" -> 2
    "F" -> 1
    else -> 5
}

@Composable
@Preview
private fun Preview() {
    Theme {
        ReviewComposeView(
            PreviewReviewComposeViewModel(),
            PreviewLectureDetailViewModel(LectureDetailViewModel.ViewState.Loaded),
            rememberNavController()
        )
    }
}
