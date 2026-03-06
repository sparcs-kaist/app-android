package org.sparcs.soap.App.Domain.Error.Feed

import androidx.annotation.StringRes
import org.sparcs.soap.App.Domain.Error.ErrorSource
import org.sparcs.soap.App.Domain.Error.SourcedError
import org.sparcs.soap.R
import java.io.Serializable

sealed class FeedProfileUseCaseError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.UseCase

    object ImageCompressionFailed : FeedProfileUseCaseError() {
        private fun readResolve(): Any = ImageCompressionFailed
    }
    object NicknameReserved : FeedProfileUseCaseError() {
        private fun readResolve(): Any = NicknameReserved
    }
    object NicknameConflict : FeedProfileUseCaseError() {
        private fun readResolve(): Any = NicknameConflict
    }
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