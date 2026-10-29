package org.sparcs.soap.app.features.postCompose

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
import org.sparcs.soap.app.features.postCompose.components.ComposeEditor
import org.sparcs.soap.app.features.postCompose.components.ComposeFileList
import org.sparcs.soap.app.features.postCompose.components.DiscardPostDialog
import org.sparcs.soap.app.features.postCompose.components.MarketPriceSection
import org.sparcs.soap.app.features.postCompose.components.PostComposeNavigationBar
import org.sparcs.soap.app.features.postCompose.components.PostComposeToolbar
import org.sparcs.soap.app.features.postCompose.components.TopicSelector
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.viewModelMocks.ara.MockPostComposeViewModel
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostComposeView(
    viewModel: PostComposeViewModelProtocol = hiltViewModel<PostComposeViewModel>(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val titleFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }
    var isUploading by remember { mutableStateOf(false) }
    var showsDiscardDialog by remember { mutableStateOf(false) }
    val isEditing = viewModel.editingPost != null
    val isMarket = viewModel.board.isMarket

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        viewModel.editor.insertImages(uris)
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        viewModel.addFiles(uris)
    }

    fun close() {
        if (viewModel.hasChanges) showsDiscardDialog = true else navController.popBackStack()
    }

    BackHandler(enabled = viewModel.hasChanges && !isUploading) { showsDiscardDialog = true }

    Scaffold(
        topBar = {
            PostComposeNavigationBar(
                onClose = { close() },
                isEditing = isEditing,
                isDoneEnabled = viewModel.canSubmit && !isUploading,
                onDoneClick = {
                    coroutineScope.launch {
                        isUploading = true
                        val isSuccess = viewModel.submit()
                        isUploading = false
                        if (isSuccess) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("listNeedsRefresh", true)
                            navController.popBackStack()
                        }
                    }
                },
                isUploading = isUploading
            )
        },
        modifier = Modifier.analyticsScreen(name = "Ara Post Compose")
    ) { innerPadding ->
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (!isEditing) {
                    TopicSelector(viewModel = viewModel)
                    Spacer(Modifier.height(16.dp))
                }
                TitleField(
                    title = viewModel.title,
                    onTitleChange = { viewModel.title = it },
                    focusRequester = titleFocusRequester,
                    onNext = { if (isMarket) priceFocusRequester.requestFocus() else viewModel.editor.focus() }
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                if (isMarket) {
                    MarketPriceSection(
                        price = viewModel.price,
                        onPriceChange = { viewModel.price = it },
                        saleState = viewModel.saleState,
                        onSaleStateChange = { viewModel.saleState = it },
                        showsSaleState = isEditing,
                        focusRequester = priceFocusRequester,
                        onNext = { viewModel.editor.focus() },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                ComposeEditor(
                    state = viewModel.editor,
                    placeholder = stringResource(if (isMarket) R.string.compose_market_description_placeholder else R.string.enter_the_description)
                )
                ComposeFileList(
                    files = viewModel.files,
                    onRemove = viewModel::removeFile,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Box(Modifier.align(Alignment.End)) {
                    TermsOfUseButton()
                }
            }

            PostComposeToolbar(
                viewModel = viewModel,
                isUploading = isUploading,
                onAddPhoto = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onAttachFile = { filePicker.launch(ComposeFile.ALLOWED_MIME_TYPES) }
            )
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        onDismiss = { viewModel.isAlertPresented = false },
        state = viewModel.alertState
    )

    if (showsDiscardDialog) {
        DiscardPostDialog(
            isEditing = isEditing,
            onDiscard = {
                showsDiscardDialog = false
                navController.popBackStack()
            },
            onDismiss = { showsDiscardDialog = false }
        )
    }
}

@Composable
private fun TitleField(title: String, onTitleChange: (String) -> Unit, focusRequester: FocusRequester, onNext: () -> Unit) {
    BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        decorationBox = { inner ->
            if (title.isEmpty()) {
                Text(
                    text = stringResource(R.string.enter_the_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            inner()
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    )
}

@Composable
fun TermsOfUseButton() {
    val context = LocalContext.current
    TextButton(
        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Constants.TERMS_OF_USE_URL.toUri())) }
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
