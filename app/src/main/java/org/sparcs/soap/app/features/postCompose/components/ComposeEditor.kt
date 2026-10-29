package org.sparcs.soap.app.features.postCompose.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.postCompose.ComposeBlock
import org.sparcs.soap.app.features.postCompose.ComposeBlockStyle
import org.sparcs.soap.app.features.postCompose.ComposeClipboard
import org.sparcs.soap.app.features.postCompose.ComposeColor
import org.sparcs.soap.app.features.postCompose.ComposeColors
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.ComposeLink
import org.sparcs.soap.app.features.postCompose.ComposeList
import org.sparcs.soap.app.features.postCompose.ComposeListMarkers
import org.sparcs.soap.app.features.postCompose.ComposeSpan
import org.sparcs.soap.app.features.postCompose.ComposeStyle
import org.sparcs.soap.app.theme.ui.Theme

private val LIST_INDENT = 24.dp
private val MAX_IMAGE_HEIGHT = 480.dp

@Composable
fun ComposeEditor(
    state: ComposeEditorState,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.enter_the_description),
) {
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    val markers = remember(state.blocks) { ComposeListMarkers.of(state.blocks) }
    val imagePaste = rememberImagePaste(state::insertImages)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { state.isEditing = it.hasFocus }
    ) {
        state.blocks.forEachIndexed { index, block ->
            when (block) {
                is ComposeBlock.Text -> TextBlock(
                    block = block,
                    marker = markers[block.id],
                    placeholder = placeholder.takeIf { index == 0 && state.isBlank },
                    selection = if (state.focusedId == block.id) state.selection else null,
                    focusRequester = focusRequesters.getOrPut(block.id) { FocusRequester() },
                    onValueChange = { value -> state.updateText(block.id, value.text, value.selection) },
                    onSelect = { range -> state.select(block.id, range) },
                    onKey = { event -> handleKey(event, state, block, imagePaste) },
                    imagePaste = imagePaste,
                )
                is ComposeBlock.Image -> ImageBlock(block, onRemove = { state.remove(block.id) })
            }
        }
    }

    LaunchedEffect(state.focusRequest) {
        if (state.focusRequest > 0) state.focusedId?.let { focusRequesters[it]?.requestFocus() }
    }
}

private class ImagePaste(private val context: Context, private val scope: CoroutineScope, private val insert: (List<Uri>) -> Unit) {
    val isAvailable: Boolean
        get() = ComposeClipboard.hasImages(context)

    val replacesTextPaste: Boolean
        get() = ComposeClipboard.hasOnlyImages(context)

    fun paste() {
        scope.launch { insert(ComposeClipboard.pasteImages(context)) }
    }
}

@Composable
private fun rememberImagePaste(insert: (List<Uri>) -> Unit): ImagePaste {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope) { ImagePaste(context, scope, insert) }
}

private fun handleKey(event: KeyEvent, state: ComposeEditorState, block: ComposeBlock.Text, imagePaste: ImagePaste): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when {
        event.key == Key.Backspace && state.selection == TextRange.Zero -> state.mergeWithPrevious(block.id)
        event.key == Key.Enter && event.isShiftPressed -> true.also { state.insertLineBreak(block.id) }
        event.key == Key.Tab && block.lists.isNotEmpty() -> true.also { if (event.isShiftPressed) state.outdent() else state.indent() }
        event.key == Key.V && event.isCtrlPressed && imagePaste.replacesTextPaste -> true.also { imagePaste.paste() }
        else -> false
    }
}

@Composable
private fun TextBlock(
    block: ComposeBlock.Text,
    marker: String?,
    placeholder: String?,
    selection: TextRange?,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    onSelect: (TextRange) -> Unit,
    onKey: (KeyEvent) -> Boolean,
    imagePaste: ImagePaste,
) {
    val length = block.text.length
    val value = TextFieldValue(
        annotatedString = annotated(block, linkColor = MaterialTheme.colorScheme.primary),
        selection = selection?.let { TextRange(it.start.coerceIn(0, length), it.end.coerceIn(0, length)) } ?: TextRange(length),
    )
    val textStyle = blockTextStyle(block.style)
    val pasteLabel = stringResource(R.string.compose_paste_image)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(start = LIST_INDENT * (block.lists.size - 1).coerceAtLeast(0))
    ) {
        BlockLeading(block.style, marker, textStyle)
        Box(
            Modifier
                .weight(1f)
                .then(if (block.style == ComposeBlockStyle.MONOSPACED) Modifier.codeBackground() else Modifier)
        ) {
            if (placeholder != null && block.text.isEmpty()) {
                Text(placeholder, style = textStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.text == block.text) onSelect(newValue.selection) else onValueChange(newValue)
                },
                textStyle = textStyle.copy(color = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onSurface }),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (it.isFocused) onSelect(value.selection) }
                    .onPreviewKeyEvent(onKey)
                    .appendTextContextMenuComponents {
                        if (imagePaste.isAvailable) {
                            item(key = PasteImageKey, label = pasteLabel) {
                                imagePaste.paste()
                                close()
                            }
                        }
                    }
            )
        }
    }
}

private object PasteImageKey

@Composable
private fun Modifier.codeBackground(): Modifier = this
    .clip(MaterialTheme.shapes.small)
    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    .padding(horizontal = 8.dp, vertical = 2.dp)

@Composable
private fun BlockLeading(style: ComposeBlockStyle, marker: String?, textStyle: TextStyle) {
    when {
        marker != null -> Text(
            text = marker,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 16.dp)
        )
        style == ComposeBlockStyle.QUOTE -> Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun ImageBlock(block: ComposeBlock.Image, onRemove: () -> Unit) {
    val model: Any = when (val source = block.source) {
        is ComposeBlock.Image.Source.Picked -> source.uri
        is ComposeBlock.Image.Source.Existing -> source.url
    }
    Box(Modifier.fillMaxWidth()) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            loading = { ImagePlaceholder(failed = false) },
            error = { ImagePlaceholder(failed = true) },
            modifier = Modifier
                .heightIn(max = MAX_IMAGE_HEIGHT)
                .clip(MaterialTheme.shapes.medium)
        )
        FilledTonalIconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_image))
        }
    }
}

@Composable
private fun ImagePlaceholder(failed: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        if (failed) {
            Icon(Icons.Outlined.BrokenImage, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun blockTextStyle(style: ComposeBlockStyle): TextStyle = when (style) {
    ComposeBlockStyle.TITLE -> MaterialTheme.typography.titleLarge
    ComposeBlockStyle.HEADING -> MaterialTheme.typography.titleMedium
    ComposeBlockStyle.SUBHEADING -> MaterialTheme.typography.titleSmall
    ComposeBlockStyle.MONOSPACED -> MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    ComposeBlockStyle.QUOTE -> MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    ComposeBlockStyle.PARAGRAPH -> MaterialTheme.typography.bodyLarge
}

private fun annotated(block: ComposeBlock.Text, linkColor: Color): AnnotatedString = buildAnnotatedString {
    val length = block.text.length
    fun range(start: Int, end: Int) = start.coerceIn(0, length) to end.coerceIn(0, length)

    append(block.text)
    block.spans.forEach { span ->
        val (start, end) = range(span.start, span.end)
        addStyle(spanStyle(span.style), start, end)
    }
    block.colors.forEach { color ->
        val (start, end) = range(color.start, color.end)
        ComposeColors.color(color.hex)?.let { addStyle(SpanStyle(color = it), start, end) }
    }
    block.links.forEach { link ->
        val (start, end) = range(link.start, link.end)
        addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), start, end)
    }
}

private fun spanStyle(style: ComposeStyle): SpanStyle = when (style) {
    ComposeStyle.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
    ComposeStyle.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
    ComposeStyle.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
    ComposeStyle.STRIKE -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    ComposeStyle.CODE -> SpanStyle(fontFamily = FontFamily.Monospace)
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        ComposeEditor(
            ComposeEditorState(
                listOf(
                    ComposeBlock.Text("동아리 모집", style = ComposeBlockStyle.TITLE),
                    ComposeBlock.Text(
                        "지원은 금요일까지 받습니다.",
                        spans = listOf(ComposeSpan(4, 9, ComposeStyle.BOLD)),
                        colors = listOf(ComposeColor(0, 2, ComposeColors.PALETTE.first())),
                    ),
                    ComposeBlock.Text("모임 안내", style = ComposeBlockStyle.SUBHEADING),
                    ComposeBlock.Text("매주 목요일", lists = listOf(ComposeList.BULLET)),
                    ComposeBlock.Text("오후 7시", lists = listOf(ComposeList.BULLET, ComposeList.NUMBERED)),
                    ComposeBlock.Text("오후 9시", lists = listOf(ComposeList.BULLET, ComposeList.NUMBERED)),
                    ComposeBlock.Text("신입 교육 제공", lists = listOf(ComposeList.BULLET)),
                    ComposeBlock.Text("val club = \"SPARCS\"", style = ComposeBlockStyle.MONOSPACED),
                    ComposeBlock.Text("누구나 환영합니다.", style = ComposeBlockStyle.QUOTE, links = listOf(ComposeLink(0, 3, "https://sparcs.org"))),
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreview() {
    Theme {
        ComposeEditor(ComposeEditorState(), placeholder = stringResource(R.string.compose_market_description_placeholder), modifier = Modifier.padding(16.dp))
    }
}
