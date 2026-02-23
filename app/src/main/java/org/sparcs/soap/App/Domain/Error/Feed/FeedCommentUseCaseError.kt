package org.sparcs.soap.App.Domain.Error.Feed

import androidx.annotation.StringRes
import org.sparcs.soap.App.Domain.Error.ErrorSource
import org.sparcs.soap.App.Domain.Error.SourcedError
import org.sparcs.soap.R
import java.io.Serializable

sealed class FeedCommentUseCaseError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.UseCase

    data class CannotDeleteCommentWithVote(val serverMessage: String?) : FeedCommentUseCaseError()

    data class Unknown(val underlying: Throwable?) : FeedCommentUseCaseError()

    @get:StringRes
    val messageRes: Int
        get() = when (this) {
            is CannotDeleteCommentWithVote -> R.string.error_feed_comment_cannot_delete_with_vote
            is Unknown -> R.string.error_unknown_try_again
        }
}