package org.sparcs.App.Domain.Enums.Feed

import androidx.annotation.StringRes
import org.sparcs.R

sealed class FeedDeletionError(@StringRes val messageResId: Int) : Exception() {

    class HasComments : FeedDeletionError(
        messageResId = R.string.error_post_has_comments
    )

    @StringRes
    fun errorDescription(): Int {
        return this.messageResId
    }
}