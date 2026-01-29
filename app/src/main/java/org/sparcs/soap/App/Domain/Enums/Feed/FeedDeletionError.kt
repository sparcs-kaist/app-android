package org.sparcs.soap.App.Domain.Enums.Feed

import androidx.annotation.StringRes
import org.sparcs.soap.R

sealed class FeedDeletionError(@StringRes val messageResId: Int) : Exception() {
    data object PostHasComments : FeedDeletionError(R.string.error_post_has_comments) {
        private fun readResolve(): Any = PostHasComments
    }

    data object PostHasVotes : FeedDeletionError(R.string.error_post_has_votes) {
        private fun readResolve(): Any = PostHasVotes
    }

    data object CommentHasReplies : FeedDeletionError(R.string.error_comment_has_replies) {
        private fun readResolve(): Any = CommentHasReplies
    }

    data object CommentHasVotes : FeedDeletionError(R.string.error_comment_has_votes) {
        private fun readResolve(): Any = CommentHasVotes
    }

    data class Unknown(val rawMessage: String) : FeedDeletionError(R.string.error_unknown_conflict)

    @StringRes
    fun errorDescription(): Int {
        return this.messageResId
    }
}