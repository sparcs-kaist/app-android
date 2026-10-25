package org.sparcs.soap.app.features.feedPostCompose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.feedPostCompose.components.FeedPostComposeNavigationBar
import org.sparcs.soap.app.features.postCompose.TermsOfUseButton
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.noRippleClickable
import org.sparcs.soap.app.shared.viewModelMocks.feed.MockFeedPostComposeViewModel
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB

@Composable
fun FeedPostComposeView(
    viewModel: FeedPostComposeViewModelProtocol = hiltViewModel<FeedPostComposeViewModel>(),
    navController: NavController,
) {
    var showPhotosPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val contentFocusRequester = remember { FocusRequester() }

    //KeyBoard
    var contentField by remember { mutableStateOf(TextFieldValue(viewModel.text)) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorLine by remember { derivedStateOf { textLayoutResult?.getLineForOffset(contentField.selection.start) } }
    val keyboardPaddingPx = with(LocalDensity.current) { 250.dp.toPx() }

    LaunchedEffect(cursorLine) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        val line = cursorLine ?: return@LaunchedEffect
        val lineTopPx = layout.getLineTop(line)
        val scrollOffset = maxOf(lineTopPx - keyboardPaddingPx, 0f)
        scope.launch { scrollState.animateScrollTo(scrollOffset.toInt()) }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchFeedUser()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uri: List<Uri> ->
        uri.let {
            viewModel.selectedItems += it
        }
    }

    Scaffold(
        topBar = {
            FeedPostComposeNavigationBar(
                navController = navController,
                isDoneEnabled = viewModel.text.isNotEmpty() && viewModel.text.length <= MAX_CHARACTERS,
                onDoneClick = {
                    scope.launch {
                        val success = viewModel.submitPost()
                        if (success) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("listNeedsRefresh", true)
                            navController.popBackStack()
                        }
                    }
                },
                isUploading = viewModel.isUploading
            )
        },
        modifier = Modifier
            .analyticsScreen(name = "Feed Compose"),
    ) { innerPadding ->
        // The text scrolls above a toolbar that sits on the keyboard, as in other Android compose screens.
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
                    .focusRequester(contentFocusRequester)
                    .noRippleClickable { contentFocusRequester.requestFocus() }
            ) {
                Header(viewModel)
                Spacer(Modifier.padding(4.dp))
                BasicTextField(
                    value = contentField,
                    onValueChange = {
                        contentField = it
                        viewModel.text = it.text
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions.Default,
                    onTextLayout = { textLayoutResult = it },
                    decorationBox = { inner ->
                        if (viewModel.text.isEmpty())
                            Text(
                                text = stringResource(R.string.enter_the_description),
                                color = MaterialTheme.colorScheme.grayBB,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (viewModel.selectedImages.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(viewModel.selectedImages) { index, bitmap ->
                            var isSpoiler by remember(bitmap.id) { mutableStateOf(bitmap.spoiler) }
                            Box {
                                Image(
                                    bitmap = bitmap.image.asImageBitmap(),
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
                                        contentDescription = stringResource(R.string.remove_image),
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { isSpoiler = !isSpoiler; bitmap.spoiler = isSpoiler },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(
                                            Color.Black.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        if (isSpoiler) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = stringResource(R.string.show_spoiler),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FeedComposeToolbar(
                characterCount = viewModel.text.length,
                isUploading = viewModel.isUploading,
                onPhotoButton = { showPhotosPicker = true }
            )
        }
        if (showPhotosPicker) {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            showPhotosPicker = false
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

@Composable
private fun Header(
    viewModel: FeedPostComposeViewModelProtocol,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileImage(viewModel)
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.weight(1f)
        ) {
            ComposeTypePicker(viewModel)
        }
        TermsOfUseButton()
    }
}

@Composable
private fun ProfileImage(viewModel: FeedPostComposeViewModelProtocol) {
    val imageUrl = viewModel.feedUser?.profileImageURL
    val composeType by remember { derivedStateOf { viewModel.selectedComposeType } }

    if (composeType == FeedPostComposeViewModel.ComposeType.Publicly && imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text("😀", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ComposeTypePicker(viewModel: FeedPostComposeViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }

    val selectedType = viewModel.selectedComposeType
    val typeLabels = mapOf(
        FeedPostComposeViewModel.ComposeType.Publicly to viewModel.feedUser?.nickname,
        FeedPostComposeViewModel.ComposeType.Anonymously to stringResource(R.string.anonymous)
    )

    val displayText = typeLabels[selectedType] ?: stringResource(R.string.select_type)

    Box {
        // The same elevated chip as the course screen's professor filter: no outline, menu arrow.
        ElevatedFilterChip(
            selected = false,
            onClick = { expanded = !expanded },
            label = {
                Text(displayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.change_type),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            typeLabels.forEach { (type, label) ->
                DropdownMenuItem(
                    text = { Text(label ?: stringResource(R.string.unknown)) },
                    trailingIcon = if (selectedType == type) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else null,
                    onClick = {
                        viewModel.selectedComposeType = type
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Attachment actions on the left and the character count on the right, above the keyboard. */
@Composable
private fun FeedComposeToolbar(
    characterCount: Int,
    isUploading: Boolean,
    onPhotoButton: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = onPhotoButton, enabled = !isUploading) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = stringResource(R.string.add_photo)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.feed_character_count, characterCount, MAX_CHARACTERS),
                style = MaterialTheme.typography.labelMedium,
                color = if (characterCount > MAX_CHARACTERS) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private const val MAX_CHARACTERS = 280

@Composable
@Preview
private fun Preview() {
    Theme {
        FeedPostComposeView(
            navController = rememberNavController(),
            viewModel = MockFeedPostComposeViewModel()
        )
    }
}
