package com.example.soap.Features.PostCompose
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.PostCompose.Components.PostComposeNavigationBar
import com.example.soap.Features.PostCompose.Components.TopicSelector
import com.example.soap.R
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
                onDoneClick = {
                    coroutineScope.launch {
                        isUploading = true
                        try {
                            viewModel.writePost()
                        } finally {
                            isUploading = false
                            navController.popBackStack()
                        }
                    }
                              },
                isUploading = isUploading
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ){
                PostOptionsRow(
                    writeAsAnonymous = viewModel.writeAsAnonymous,
                    onAnonymousChange = { viewModel.writeAsAnonymous = !viewModel.writeAsAnonymous },
                    isNSFW = viewModel.isNSFW,
                    onNSFWChange = { viewModel.isNSFW = !viewModel.isNSFW },
                    isPolitical = viewModel.isPolitical,
                    onPoliticalChange = { viewModel.isPolitical = !viewModel.isPolitical },
                    isUploading = isUploading,
                    onPhotoButton = { showPhotosPicker = true }
                )
            }
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
            TopicSelector(
                viewModel = viewModel
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
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
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
                    TermsOfUseButton()
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
fun PostOptionsRow(
    writeAsAnonymous: Boolean,
    onAnonymousChange: () -> Unit,
    isNSFW: Boolean,
    onNSFWChange: () -> Unit,
    isPolitical: Boolean,
    onPoliticalChange: () -> Unit,
    isUploading: Boolean,
    onPhotoButton: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            IconButton(
                onClick = onPhotoButton,
                enabled = !isUploading
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_photo_alternate),
                    contentDescription = "add Photo",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.more_horiz),
                        contentDescription = "More Options",
                        modifier = Modifier.size(28.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.political)) },
                        trailingIcon = { if (isPolitical) Icons.Default.Check },
                        onClick = onPoliticalChange
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.nsfw)) },
                        trailingIcon = { if (isNSFW) Icons.Default.Check },
                        onClick = onNSFWChange
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.anonymous)) },
                        trailingIcon = { if (writeAsAnonymous) Icons.Default.Check },
                        onClick = onAnonymousChange
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsOfUseButton(){
    TextButton(onClick = { /* TODO: Terms click */ }) {
        Text(
            text = "terms of use",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.grayBB,
            textDecoration = TextDecoration.Underline
        )
    }
}


@Composable
@Preview
fun PostComposeViewPreview() {
    Theme { PostComposeView( MockPostComposeViewModel(), rememberNavController()) }
}