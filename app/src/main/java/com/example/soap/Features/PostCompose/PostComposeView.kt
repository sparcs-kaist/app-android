package com.example.soap.Features.PostCompose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soap.Features.PostCompose.Components.CheckBoxText
import com.example.soap.Features.PostCompose.Components.FlairSelector
import com.example.soap.Features.PostList.PostListViewModel
import com.example.soap.R
import com.example.soap.Utilities.Extensions.noRippleClickable
import com.example.soap.ui.theme.SoapTheme
import kotlinx.coroutines.launch

@Composable
fun PostComposeView(postListViewModel: PostListViewModel) {
    val coroutineScope = rememberCoroutineScope()

    val descriptionFocusRequester = remember { FocusRequester() }
    val descriptionBringIntoViewRequester = remember { BringIntoViewRequester() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(TextFieldValue("")) }

    var writeAsAnonymous by remember { mutableStateOf(true) }
    var isNSFW by remember { mutableStateOf(false) }
    var isPolitical by remember { mutableStateOf(false) }
    val isDoneEnabled = title.isNotBlank() && description.toString().isNotBlank()

    val internalScrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorLine by remember {
        derivedStateOf { textLayoutResult?.getLineForOffset(description.selection.start) }
    }
    val keyboardPaddingPx = with(LocalDensity.current) { 250.dp.toPx() }

    LaunchedEffect(cursorLine) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        val line = cursorLine ?: return@LaunchedEffect

        val lineTopPx = layout.getLineTop(line)
        val scrollOffset = maxOf(lineTopPx - keyboardPaddingPx, 0f)

        coroutineScope.launch {
            internalScrollState.animateScrollTo(scrollOffset.toInt())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Write",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal,
                    color = if (isDoneEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .semantics { contentDescription = "Post Button" }
                        .clickable {
                            if (isDoneEnabled) {
                                //Todo: Dismiss
                            }
                        }
                )
            }

            FlairSelector(postListViewModel)

            Spacer(Modifier.padding(8.dp))

            TitleTextField(
                value = title,
                onValueChange = { title = it},
                placeholderText = "Please enter the title",
                nextFocus = { descriptionFocusRequester.requestFocus() }
            )

            HorizontalDivider()

            Spacer(Modifier.padding(4.dp))

            Column(
                modifier = Modifier
                    .bringIntoViewRequester(descriptionBringIntoViewRequester)
                    .verticalScroll(internalScrollState)
                    .focusRequester(descriptionFocusRequester)
                    .weight(1f)
                    .fillMaxWidth()
                    .noRippleClickable { descriptionFocusRequester.requestFocus() }
            ) {
                DescriptionTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholderText = "What's happening",
                    modifier = Modifier,
                    onTextLayout = { textLayoutResult = it },
                    )
                Spacer(Modifier.padding(4.dp))

                Box(Modifier.align(Alignment.End)) {
                    TermsOfUseButton()
                }
            }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End)
        ) {

            Icon(
                painter = painterResource(R.drawable.add_photo_alternate),
                contentDescription = "Add photo",
                modifier = Modifier.clickable { }
            )

            Spacer(Modifier.padding(horizontal = 4.dp))

            Icon(
                painter = painterResource(R.drawable.attach_file),
                contentDescription = "Add file",
                modifier = Modifier.clickable { }
            )

            Spacer(Modifier.weight(1f))

            CheckBoxText(
                text = "Anonymous",
                isChecked = writeAsAnonymous,
                onCheckedChange = { writeAsAnonymous = !writeAsAnonymous }
            )

            CheckBoxText(
                text = "NSFW",
                isChecked = isNSFW,
                onCheckedChange = { isNSFW = !isNSFW }
            )

            CheckBoxText(
                text = "Political",
                isChecked = isPolitical,
                onCheckedChange = { isPolitical = !isPolitical }
            )
        }
    }
    }
}


@Composable
private fun TermsOfUseButton(){
    Text(
        text = "terms of use",
        style = MaterialTheme.typography.bodySmall,
        textDecoration = TextDecoration.Underline,
        color = MaterialTheme.colorScheme.outline
    )
}


@Composable
fun TitleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    nextFocus: (KeyboardActionScope.() -> Unit)?
){
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = nextFocus
        ),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholderText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            innerTextField()
        }
    )
}

@Composable
fun DescriptionTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholderText: String,
    modifier: Modifier,
    onTextLayout: (TextLayoutResult) -> Unit
){
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = false,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default),
        modifier = modifier,
        onTextLayout = onTextLayout,
        decorationBox = { innerTextField ->
            if (value.text.isEmpty()) {
                Text(
                    text = placeholderText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            innerTextField()
        }
    )
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { PostComposeView(viewModel()) }
}
