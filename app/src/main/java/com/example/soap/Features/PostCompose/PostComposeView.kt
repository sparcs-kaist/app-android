package com.example.soap.Features.PostCompose
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostCompose.Components.CheckBoxText
import com.example.soap.Features.PostCompose.Components.PostComposeNavigationBar
import com.example.soap.R
import com.example.soap.Shared.Extensions.LocalizedText
import com.example.soap.Shared.Extensions.noRippleClickable
import com.example.soap.Shared.ViewModelMocks.MockPostComposeViewModel
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import kotlinx.coroutines.launch

@Composable
fun PostComposeView(
    viewModel: PostComposeViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }

    var isUploading by remember { mutableStateOf(false) }

    val isDoneEnabled = viewModel.title.isNotBlank() && viewModel.content.isNotBlank() && !isUploading
    val isBackEnabled =  viewModel.title.isBlank() &&  viewModel.content.isBlank()

    val context = LocalContext.current

    //KeyBoard
    var titleField by remember { mutableStateOf(TextFieldValue(viewModel.title)) }
    var contentField by remember { mutableStateOf(TextFieldValue(viewModel.content)) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorLine by remember { derivedStateOf { textLayoutResult?.getLineForOffset(contentField.selection.start) } }
    val keyboardPaddingPx = with(LocalDensity.current) { 250.dp.toPx() }

    LaunchedEffect(cursorLine) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        val line = cursorLine ?: return@LaunchedEffect
        val lineTopPx = layout.getLineTop(line)
        val scrollOffset = maxOf(lineTopPx - keyboardPaddingPx, 0f)
        coroutineScope.launch { scrollState.animateScrollTo(scrollOffset.toInt()) }
    }

    LaunchedEffect(viewModel.selectedItems) { viewModel.updateSelectedImages(context) }

    //Image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }
    var showPhotosPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PostComposeNavigationBar(
                navController = navController,
                isDoneEnabled = isDoneEnabled,
                isBackEnabled = !isBackEnabled,
                onDoneClick = {
                    coroutineScope.launch {
                        isUploading = true
                        try {
                            viewModel.writePost()
                        } finally {
                            isUploading = false
                            navController.navigate(Channel.TrendingBoard.name)
                        }
                    }
                              },
                isUploading = isUploading
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TextButton(
                    onClick = { showPhotosPicker = true },
                    enabled = !isUploading
                ) {
                    Text("Photo Library")
                }
                PostOptionsRow(
                    writeAsAnonymous = viewModel.writeAsAnonymous,
                    onAnonymousChange = { viewModel.writeAsAnonymous = it },
                    isNSFW = viewModel.isNSFW,
                    onNSFWChange = { viewModel.isNSFW = it },
                    isPolitical = viewModel.isPolitical,
                    onPoliticalChange = { viewModel.isPolitical = it },
                    isUploading = isUploading
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TopicDropdown(
                topics = viewModel.board.topics.map { it.name },
                selectedTopic = viewModel.selectedTopic?.name,
                onTopicSelected = { topicName ->
                    coroutineScope.launch {
                        val topic = viewModel.board.topics.firstOrNull { it.name == topicName }
                        if (topic != null) viewModel.selectedTopic = topic
                        else viewModel.selectedTopic = null
                    }
                },
                enabled = !isUploading
            )

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = titleField,
                onValueChange = {
                    titleField = it
                    viewModel.title = it.text
                                },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { contentFocusRequester.requestFocus() }
                ),
                decorationBox = { inner ->
                    if (viewModel.title.isEmpty())
                        Text(
                            text = stringResource(R.string.enter_the_title),
                            color = MaterialTheme.colorScheme.grayBB,
                            style = MaterialTheme.typography.titleLarge
                        )
                    inner()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
            )

            Spacer(Modifier.padding(2.dp))

            HorizontalDivider()

            Spacer(Modifier.padding(4.dp))

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
                        viewModel.content = it.text
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions.Default,
                    onTextLayout = { textLayoutResult = it },
                    decorationBox = { inner ->
                        if (contentField.text.isEmpty())
                            Text(
                                text = stringResource(R.string.enter_the_description),
                                color = MaterialTheme.colorScheme.grayBB,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.padding(4.dp))

                Box(Modifier.align(Alignment.End)) {
                    TextButton(onClick = { /* TODO: Terms click */ }) {
                        Text("terms of use", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

        }
    }
    if (showPhotosPicker) {
        launcher.launch("image/*")
        showPhotosPicker = false
    }
}
@Composable
fun TopicDropdown(
    topics: List<LocalizedString>,
    selectedTopic: LocalizedString?,
    onTopicSelected: (LocalizedString?) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val noTopic = LocalizedString(mapOf("en" to "No topic", "ko" to "주제 없음"))

    Box {
        TextButton(onClick = { expanded = true }, enabled = enabled) {
            LocalizedText(selectedTopic ?: noTopic)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { LocalizedText(noTopic) },
                onClick = {
                    onTopicSelected(null)
                    expanded = false
                }
            )
            topics.forEach { topic ->
                DropdownMenuItem(
                    text = { LocalizedText(topic) },
                    onClick = {
                        onTopicSelected(topic)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PostOptionsRow(
    writeAsAnonymous: Boolean,
    onAnonymousChange: (Boolean) -> Unit,
    isNSFW: Boolean,
    onNSFWChange: (Boolean) -> Unit,
    isPolitical: Boolean,
    onPoliticalChange: (Boolean) -> Unit,
    isUploading: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        CheckBoxText(
            text = stringResource(R.string.anonymous),
            isChecked = writeAsAnonymous,
            onCheckedChange = { if (!isUploading) onAnonymousChange(!writeAsAnonymous) },
            enabled = !isUploading
        )

        Spacer(Modifier.width(8.dp))

        CheckBoxText(
            text = stringResource(R.string.nsfw),
            isChecked = isNSFW,
            onCheckedChange = { if (!isUploading) onNSFWChange(!isNSFW) },
            enabled = !isUploading
        )

        Spacer(Modifier.width(8.dp))

        CheckBoxText(
            text = stringResource(R.string.political),
            isChecked = isPolitical,
            onCheckedChange = { if (!isUploading) onPoliticalChange(!isPolitical) },
            enabled = !isUploading
        )
    }
}


@Composable
@Preview
fun PostComposeViewPreview() {
    Theme { PostComposeView( MockPostComposeViewModel(), rememberNavController()) }
}