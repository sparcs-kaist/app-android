package org.sparcs.soap.app.domain.error.feed

import androidx.annotation.StringRes
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.error.ErrorSource
import org.sparcs.soap.app.domain.error.SourcedError
import java.io.Serializable

sealed class FeedImageUseCaseError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.UseCase

    data class ImageCompressionFailed(val serverMessage: String?) : FeedImageUseCaseError()
    data class Unknown(val underlying: Throwable?) : FeedImageUseCaseError()

    @get:StringRes
    val messageRes: Int
        get() = when (this) {
            is ImageCompressionFailed -> R.string.error_feed_image_compression_failed
            is Unknown -> R.string.error_unknown_try_again
        }
}