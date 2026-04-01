package org.sparcs.soap.App.Features.ReviewCompose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
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
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModel
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.App.Features.ReviewCompose.Components.ReviewComposeNavigationBar
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.ratingToString
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Extensions.noRippleClickable
import org.sparcs.soap.App.Shared.Views.ContentViews.GlobalAlertDialog
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.BuddyPreviewSupport.OTL.PreviewLectureDetailViewModel
import org.sparcs.soap.BuddyPreviewSupport.OTL.PreviewReviewComposeViewModel
import org.sparcs.soap.R

@Composable
fun ReviewComposeView(
    viewModel: ReviewComposeViewModelProtocol = hiltViewModel(),
    lectureDetailViewModel: LectureDetailViewModelProtocol = hiltViewModel(),
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
                        val updatedReview = viewModel.submitReview(contentField.text, grade, load, speech)
                        if (viewModel.alertState == null) {
                            if (updatedReview != null) {
                                lectureDetailViewModel.updateWrittenReview(updatedReview)
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
                                text = stringResource(R.string.share_lecture_thoughts, viewModel.lecture.name),
                                color = MaterialTheme.colorScheme.grayBB,
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

@Composable
private fun RatingPicker(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var previousValue by remember { mutableIntStateOf(value) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.grayBB
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box {
            Row(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        if (targetState > previousValue) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> height } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    }
                ) { targetValue ->
                    Text(
                        text = ratingToString(targetValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    Icons.Rounded.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf(5, 4, 3, 2, 1).forEach { itValue ->
                    DropdownMenuItem(
                        text = { Text(ratingToString(itValue)) },
                        onClick = {
                            previousValue = value
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
