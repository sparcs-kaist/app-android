package org.sparcs.soap.app.features.postCompose

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.features.postCompose.components.PostComposeNavigationBar
import org.sparcs.soap.app.features.postCompose.components.TopicSelector
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.noRippleClickable
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostComposeView(
    viewModel: PostComposeViewModelProtocol = hiltViewModel<PostComposeViewModel>(),
    navController: NavController,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }

    var isUploading by remember { mutableStateOf(false) }

    val isDoneEnabled =
        viewModel.title.isNotBlank() && viewModel.content.isNotBlank() && !isUploading

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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uri: List<Uri> ->
        uri.let {
            viewModel.selectedItems += it
            coroutineScope.launch { viewModel.updateSelectedImages(context) }
        }
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

                        val isSuccess = viewModel.writePost()
                        isUploading = false

                        if (isSuccess) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("listNeedsRefresh", true)
                            navController.popBackStack()
                        }
                    }
                },
                isUploading = isUploading
            )
        },
        modifier = Modifier.analyticsScreen(name = "Ara Post Compose")
    ) { innerPadding ->
        // The text scrolls above a toolbar that sits on the keyboard, like the feed compose screen.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .fillMaxWidth()
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.padding(4.dp))

                    if (viewModel.selectedImages.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(viewModel.selectedImages) { index, bitmap ->
                                Box {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )

                                    IconButton(
                                        onClick = { viewModel.removeImage(index) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .background(
                                                Color.Black.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove Image",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Box(Modifier.align(Alignment.End)) {
                        TermsOfUseButton()
                    }
                }
            }

            PostComposeToolbar(
                writeAsAnonymous = viewModel.writeAsAnonymous,
                onAnonymousChange = {
                    viewModel.writeAsAnonymous = !viewModel.writeAsAnonymous
                },
                isNSFW = viewModel.isNSFW,
                onNSFWChange = { viewModel.isNSFW = !viewModel.isNSFW },
                isPolitical = viewModel.isPolitical,
                onPoliticalChange = { viewModel.isPolitical = !viewModel.isPolitical },
                isUploading = isUploading,
                onPhotoButton = { showPhotosPicker = true }
            )
        }
    }
    if (showPhotosPicker) {
        launcher.launch("image/*")
        showPhotosPicker = false
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        onDismiss = { viewModel.isAlertPresented = false },
        state = viewModel.alertState
    )
}

/** Add photo, and one menu for the post options. */
@Composable
private fun PostComposeToolbar(
    writeAsAnonymous: Boolean,
    onAnonymousChange: () -> Unit,
    isNSFW: Boolean,
    onNSFWChange: () -> Unit,
    isPolitical: Boolean,
    onPoliticalChange: () -> Unit,
    isUploading: Boolean,
    onPhotoButton: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onPhotoButton, enabled = !isUploading) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = stringResource(R.string.add_photo)
                )
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.post_options)
                    )
                }
                // Stays open while options are toggled, since they're independent of each other.
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    PostOptionItem(stringResource(R.string.post_option_anonymous), writeAsAnonymous, onAnonymousChange)
                    PostOptionItem(stringResource(R.string.nsfw), isNSFW, onNSFWChange)
                    PostOptionItem(stringResource(R.string.political), isPolitical, onPoliticalChange)
                }
            }
        }
    }
}

@Composable
private fun PostOptionItem(label: String, checked: Boolean, onToggle: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        trailingIcon = { Checkbox(checked = checked, onCheckedChange = null) },
        onClick = onToggle,
        modifier = Modifier.semantics { role = Role.Checkbox; toggleableState = ToggleableState(checked) }
    )
}

@Composable
fun TermsOfUseButton() {
    val context = LocalContext.current
    TextButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Constants.TERMS_OF_USE_URL.toUri())
            context.startActivity(intent)
        }
    ) {
        Text(
            text = stringResource(R.string.terms_of_use),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline
        )
    }
}


@Composable
@Preview
fun PostComposeViewPreview() {
    Theme { PostComposeView(MockPostComposeViewModel(), rememberNavController()) }
}
