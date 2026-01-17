package org.sparcs.App.Features.FeedPostCompose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.sparcs.App.Features.FeedPostCompose.Components.FeedPostComposeNavigationBar
import org.sparcs.App.Features.PostCompose.Components.AnimatedAlphabetText
import org.sparcs.App.Features.PostCompose.TermsOfUseButton
import org.sparcs.App.Shared.Extensions.isNetworkError
import org.sparcs.App.Shared.Extensions.noRippleClickable
import org.sparcs.App.Shared.ViewModelMocks.Feed.MockFeedPostComposeViewModel
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.grayBB
import org.sparcs.R

@Composable
fun FeedPostComposeView(
    viewModel: FeedPostComposeViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    var showPhotosPicker by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val contentFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    //KeyBoard
    var contentField by remember { mutableStateOf(TextFieldValue(viewModel.text)) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorLine by remember { derivedStateOf { textLayoutResult?.getLineForOffset(contentField.selection.start) } }
    val keyboardPaddingPx = with(LocalDensity.current) { 250.dp.toPx() }

    var showAlert by remember { mutableStateOf(false) }
    @StringRes var alertTitle: Int by remember { mutableStateOf(0) }
    @StringRes var alertMessage: Int by remember { mutableStateOf(0) }

    fun showAlert(@StringRes title: Int, @StringRes message: Int) {
        alertTitle = title
        alertMessage = message
        showAlert = true
    }

    LaunchedEffect(cursorLine) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        val line = cursorLine ?: return@LaunchedEffect
        val lineTopPx = layout.getLineTop(line)
        val scrollOffset = maxOf(lineTopPx - keyboardPaddingPx, 0f)
        coroutineScope.launch { scrollState.animateScrollTo(scrollOffset.toInt()) }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchFeedUser()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uri: List<Uri> ->
        uri.let {
            viewModel.selectedItems += it
            coroutineScope.launch { viewModel.loadImagesAndReconcile(context) }
        }
    }

    Scaffold(
        topBar = {
            FeedPostComposeNavigationBar(
                navController = navController,
                isDoneEnabled = viewModel.text.isNotEmpty() && viewModel.text.length <= 280,
                onDoneClick = {
                    coroutineScope.launch {
                        isUploading = true
                        try {
                            viewModel.writePost()

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("listNeedsRefresh", true)
                            navController.popBackStack()

                        } catch (e: Exception) {
                            val message = if (e.isNetworkError()) {
                                R.string.network_connection_error
                            } else {
                                viewModel.handleException(e)
                                R.string.unexpected_error_uploading_post
                            }
                            showAlert(
                                title = R.string.error,
                                message = message
                            )
                        } finally {
                            isUploading = false
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
            ) {
                FeedPostOptionsRow(
                    isUploading = isUploading,
                    onPhotoButton = { showPhotosPicker = true }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text("${viewModel.text.length}/280", style = MaterialTheme.typography.bodySmall)
            }
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
                                    if (isSpoiler) painterResource(R.drawable.baseline_visibility_off) else painterResource(
                                        R.drawable.baseline_visibility
                                    ),
                                    contentDescription = stringResource(R.string.show_spoiler),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showPhotosPicker) {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            showPhotosPicker = false
        }
    }
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                Button(onClick = { showAlert = false }) { Text(stringResource(R.string.ok)) }
            },
            title = { Text(stringResource(alertTitle)) },
            text = { Text(stringResource(alertMessage)) }
        )
    }
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
        ComposeTypePicker(viewModel)
        Spacer(modifier = Modifier.weight(1f))
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
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text("😀", fontSize = 14.sp)
        }
    }
}

@Composable
private fun ComposeTypePicker(viewModel: FeedPostComposeViewModelProtocol) {
    var expanded by remember { mutableStateOf(false) }
    var previousType by remember { mutableStateOf(viewModel.selectedComposeType) }

    val selectedType = viewModel.selectedComposeType
    val typeLabels = mapOf(
        FeedPostComposeViewModel.ComposeType.Publicly to viewModel.feedUser?.nickname,
        FeedPostComposeViewModel.ComposeType.Anonymously to stringResource(R.string.anonymous)
    )

    if (previousType != selectedType) {
        previousType = selectedType
    }

    val displayText = typeLabels[selectedType] ?: stringResource(R.string.select_type)
    val previousText = typeLabels[previousType] ?: stringResource(R.string.select_type)

    Box {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                AnimatedAlphabetText(
                    from = previousText,
                    to = displayText,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_drop_down),
                    contentDescription = stringResource(R.string.change_type)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            typeLabels.forEach { (type, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label ?: stringResource(R.string.unknown),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedType == type)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        viewModel.selectedComposeType = type
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FeedPostOptionsRow(
    isUploading: Boolean,
    onPhotoButton: () -> Unit,
) {
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
                    painter = painterResource(R.drawable.outline_photo_library),
                    contentDescription = stringResource(R.string.add_photo),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

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