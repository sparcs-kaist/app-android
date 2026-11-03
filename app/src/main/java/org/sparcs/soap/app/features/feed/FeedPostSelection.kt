package org.sparcs.soap.app.features.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

/** Navigation state, independent of the selected post's data and draft. */
data class FeedPostSelection(
    val postID: String? = null,
    val commentID: String? = null,
    val isPanel: Boolean = false,
)

@Composable
fun rememberFeedPostSelection(): MutableState<FeedPostSelection> = rememberSaveable(
    stateSaver = listSaver(
        save = { listOf(it.postID.orEmpty(), it.commentID.orEmpty(), it.isPanel) },
        restore = {
            FeedPostSelection(
                postID = (it[0] as String).ifEmpty { null },
                commentID = (it[1] as String).ifEmpty { null },
                isPanel = it[2] as Boolean,
            )
        },
    ),
) { mutableStateOf(FeedPostSelection()) }
