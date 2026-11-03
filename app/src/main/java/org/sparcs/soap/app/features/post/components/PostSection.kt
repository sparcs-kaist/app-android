package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.isHiddenForBlockedAuthor
import org.sparcs.soap.app.domain.models.summarization.PostSummaryPhase
import org.sparcs.soap.app.features.post.PostBody
import org.sparcs.soap.app.features.post.PostTranslationState
import org.sparcs.soap.app.features.post.attachedImageUrls
import org.sparcs.soap.app.shared.mocks.ara.mock
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.theme.ui.Theme

data class PostSectionState(
    val post: AraPost?,
    val body: PostBody?,
    val translationState: PostTranslationState,
    val summaryPhase: PostSummaryPhase?,
    val showsToolsTip: Boolean,
)

data class PostSectionActions(
    val onAuthorClick: () -> Unit = {},
    val onLinkTapped: (String) -> Unit = {},
    val onShowOriginal: () -> Unit = {},
    val onDownloadTranslation: () -> Unit = {},
    val onRetrySummary: () -> Unit = {},
    val onDismissSummary: () -> Unit = {},
    val onUpVote: () -> Unit = {},
    val onDownVote: () -> Unit = {},
    val onToggleBookmark: () -> Unit = {},
    val onCommentClick: () -> Unit = {},
)

fun LazyListScope.postSection(
    state: PostSectionState,
    actions: PostSectionActions,
    snackbarHostState: SnackbarHostState,
) {
    val post = state.post
    val translation = (state.translationState as? PostTranslationState.Translated)?.translation

    item(key = "post-header") {
        if (post == null) HeaderSkeleton()
        else PostHeader(post = post, translatedTitle = translation?.title, onAuthorClick = actions.onAuthorClick)
    }

    item(key = "post-tip") {
        TipView(BuddyTip.PostTools, enabled = state.showsToolsTip, modifier = Modifier.padding(vertical = 8.dp))
    }

    item(key = "post-translation") {
        PostTranslationBanner(
            state = state.translationState,
            onShowOriginal = actions.onShowOriginal,
            onDownload = actions.onDownloadTranslation,
        )
    }

    item(key = "post-content") {
        val attachedImages = remember(post?.content, post?.attachments) { post?.attachedImageUrls.orEmpty() }
        PostContent(
            body = translation?.let { PostBody.Tiptap(it.layout, plainText = "") } ?: state.body.takeIf { post != null },
            contentKey = translation?.targetLanguage ?: ORIGINAL_CONTENT,
            attachedImages = attachedImages,
            isTranslating = state.translationState is PostTranslationState.Translating,
            isHiddenForBlockedAuthor = post?.isHiddenForBlockedAuthor == true,
            summaryPhase = state.summaryPhase,
            onRetrySummary = actions.onRetrySummary,
            onDismissSummary = actions.onDismissSummary,
            onLinkTapped = actions.onLinkTapped,
        )
    }

    if (post != null && !post.attachments.isNullOrEmpty()) {
        item(key = "post-attachments") {
            PostAttachmentsSection(attachments = post.attachments, snackbarHostState = snackbarHostState)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    item(key = "post-footer") {
        if (post == null) FooterSkeleton()
        else PostFooter(
            post = post,
            onUpVote = actions.onUpVote,
            onDownVote = actions.onDownVote,
            onToggleBookmark = actions.onToggleBookmark,
            onCommentClick = actions.onCommentClick,
        )
    }
}

private const val ORIGINAL_CONTENT = "original"

@Preview(showBackground = true)
@Composable
private fun PostSectionPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    Theme {
        LazyColumn {
            postSection(
                state = PostSectionState(
                    post = AraPost.mock(),
                    body = PostBody.from(AraPost.mock().content.orEmpty(), null),
                    translationState = PostTranslationState.Translating("en"),
                    summaryPhase = null,
                    showsToolsTip = false,
                ),
                actions = PostSectionActions(),
                snackbarHostState = snackbarHostState,
            )
        }
    }
}
