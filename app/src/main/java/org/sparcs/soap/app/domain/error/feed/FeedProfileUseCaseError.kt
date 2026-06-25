package org.sparcs.soap.app.domain.error.feed

import androidx.annotation.StringRes
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.error.ErrorSource
import org.sparcs.soap.app.domain.error.SourcedError
import java.io.Serializable

sealed class FeedProfileUseCaseError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.UseCase

    class ImageCompressionFailed : FeedProfileUseCaseError()
    class NicknameReserved : FeedProfileUseCaseError()
    class NicknameConflict : FeedProfileUseCaseError()
    data class Unknown(val underlying: Throwable?) : FeedProfileUseCaseError()

    @get:StringRes
    val messageRes: Int
        get() = when (this) {
            is ImageCompressionFailed -> R.string.error_feed_image_compression_failed
            is NicknameReserved -> R.string.error_feed_nickname_reserved
            is NicknameConflict -> R.string.nickname_error_conflict
            is Unknown -> R.string.error_unknown_try_again
        }
}