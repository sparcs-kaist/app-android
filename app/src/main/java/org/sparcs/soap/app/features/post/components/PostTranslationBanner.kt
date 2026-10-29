package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.post.PostTranslation
import org.sparcs.soap.app.features.post.PostTranslationState
import org.sparcs.soap.app.shared.views.tiptap.TiptapLayout
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostTranslationBanner(
    state: PostTranslationState,
    onShowOriginal: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it::class },
        modifier = modifier,
        label = "translationBanner",
    ) { current ->
        when (current) {
            PostTranslationState.Original -> Unit
            is PostTranslationState.Translating -> BannerRow {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                BannerText(stringResource(R.string.post_translating_to, languageName(current.targetLanguage)), Modifier.weight(1f))
            }

            is PostTranslationState.DownloadRequired -> Column {
                BannerRow {
                    TranslateIcon()
                    BannerText(
                        stringResource(
                            R.string.translation_download_required,
                            languageName(current.sourceLanguage),
                            languageName(current.targetLanguage),
                        ),
                        Modifier.weight(1f),
                        maxLines = Int.MAX_VALUE,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onShowOriginal) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = onDownload) { Text(stringResource(R.string.translation_download)) }
                }
            }

            is PostTranslationState.Translated -> BannerRow {
                TranslateIcon()
                BannerText(description(current.translation), Modifier.weight(1f))
                TextButton(onClick = onShowOriginal) { Text(stringResource(R.string.show_original)) }
            }
        }
    }
}

@Composable
private fun description(translation: PostTranslation): String {
    val target = languageName(translation.targetLanguage)
    val source = translation.sourceLanguage ?: return stringResource(R.string.post_translated_to, target)
    return stringResource(R.string.post_translated_from_to, languageName(source), target)
}

@Composable
private fun BannerRow(content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        content = content,
    )
}

@Composable
private fun TranslateIcon() {
    Icon(
        imageVector = Icons.Outlined.Translate,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
    )
}

@Composable
private fun BannerText(text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun PostTranslationBannerPreview() {
    Theme {
        Column(Modifier.padding(16.dp)) {
            PostTranslationBanner(PostTranslationState.Translating("en"), onShowOriginal = {}, onDownload = {})
            PostTranslationBanner(
                PostTranslationState.Translated(PostTranslation("Title", TiptapLayout(emptyList()), "ko", "en")),
                onShowOriginal = {},
                onDownload = {},
            )
            PostTranslationBanner(PostTranslationState.DownloadRequired("ko", "ja"), onShowOriginal = {}, onDownload = {})
        }
    }
}
