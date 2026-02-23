package org.sparcs.soap.App.Domain.Error.Feed

import androidx.annotation.StringRes
import org.sparcs.soap.App.Domain.Error.ErrorSource
import org.sparcs.soap.App.Domain.Error.SourcedError
import org.sparcs.soap.R
import java.io.Serializable

sealed class FeedPostUseCaseError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.UseCase

    data class CannotDeletePostWithVoteOrComment(val serverMessage: String?) : FeedPostUseCaseError()

    data class Unknown(val underlying: Throwable?) : FeedPostUseCaseError()

    @get:StringRes
    val messageRes: Int
        get() = when (this) {
            is CannotDeletePostWithVoteOrComment -> R.string.error_feed_post_cannot_delete_with_vote_or_comment
            is Unknown -> R.string.error_unknown_try_again
        }
}