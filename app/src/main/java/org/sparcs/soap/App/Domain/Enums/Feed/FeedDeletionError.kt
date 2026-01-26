package org.sparcs.soap.App.Domain.Enums.Feed

import androidx.annotation.StringRes
import org.sparcs.soap.R

sealed class FeedDeletionError(@StringRes val messageResId: Int) : Exception() {

    class HasComments : FeedDeletionError(
        messageResId = R.string.error_post_has_comments
    )

    class HasReplies : FeedDeletionError(
        messageResId = R.string.error_comment_has_replies
    )

    @StringRes
    fun errorDescription(): Int {
        return this.messageResId
    }
}