package com.sparcs.soap.Features.ReviewCompose

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sparcs.soap.Domain.Repositories.OTL.OTLLectureRepositoryProtocol
import com.sparcs.soap.Features.LectureDetail.LectureDetailViewModel
import com.sparcs.soap.Features.ReviewCompose.Components.ReviewComposeNavigationBar
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Extensions.noRippleClickable
import com.sparcs.soap.ui.theme.grayBB
import kotlinx.coroutines.launch

@Composable
fun ReviewComposeView(
    reviewComposeViewModel: ReviewComposeViewModelProtocol = hiltViewModel(),
    lectureDetailViewModel: LectureDetailViewModel = hiltViewModel(),
    navController: NavController,
) {
    val repo: OTLLectureRepositoryProtocol =
        hiltViewModel<ReviewComposeViewModel>().otlLectureRepository
    val lecture = reviewComposeViewModel.lecture
    var grade by remember { mutableStateOf(5) }
    var load by remember { mutableStateOf(5) }
    var speech by remember { mutableStateOf(5) }
    var contentField by remember { mutableStateOf(TextFieldValue("")) }

    var isUploading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorLine by remember { derivedStateOf { textLayoutResult?.getLineForOffset(contentField.selection.start) } }
    val keyboardPaddingPx = with(LocalDensity.current) { 250.dp.toPx() }

    val contentFocusRequester = remember { FocusRequester() }
    val submittedMessage = stringResource(R.string.review_submitted)

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
                        isUploading = true
                        try {
                            val review = repo.writeReview(
                                lectureID = lecture.id,
                                content = contentField.text,
                                grade = grade,
                                load = load,
                                speech = speech
                            )
                            lectureDetailViewModel.writeReview(review)
                            navController.popBackStack()
                        } catch (e: Exception) {
                            showErrorDialog = true
                        } finally {
                            isUploading = false
                            Toast.makeText(
                                context,
                                submittedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                isUploading = isUploading,
                isDoneEnabled = contentField.text.isNotBlank() && !isUploading
            )
        }
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
                RatingPicker(title = stringResource(R.string.grade), value = grade, onValueChange = { grade = it })
                RatingPicker(title = stringResource(R.string.load), value = load, onValueChange = { load = it })
                RatingPicker(title = stringResource(R.string.speech), value = speech, onValueChange = { speech = it })
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
                                text = stringResource(R.string.share_lecture_thoughts),
                                color = MaterialTheme.colorScheme.grayBB,
                                style = MaterialTheme.typography.titleMedium
                            )
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text(stringResource(R.string.error)) },
                text = { Text(stringResource(R.string.error_try_again)) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
private fun RatingPicker(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var previousValue by remember { mutableStateOf(value) }

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
                        text = letterFromValue(targetValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    painterResource(R.drawable.baseline_arrow_drop_down),
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf(5, 4, 3, 2, 1).forEach { itValue ->
                    DropdownMenuItem(
                        text = { Text(letterFromValue(itValue)) },
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


private fun letterFromValue(value: Int): String = when (value) {
    5 -> "A"
    4 -> "B"
    3 -> "C"
    2 -> "D"
    else -> "F"
}

//@Composable
//@Preview
//private fun Preview() {
//    Theme {
//        ReviewComposeView(MockReviewComposeViewModel(), rememberNavController())
//    }
//}
